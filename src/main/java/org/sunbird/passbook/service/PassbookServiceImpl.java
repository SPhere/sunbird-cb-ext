package org.sunbird.passbook.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.passbook.parser.PassbookParser;
import org.sunbird.passbook.parser.PassbookParserHandler;

@Service
public class PassbookServiceImpl implements PassbookService {

	private Logger logger = LoggerFactory.getLogger(PassbookServiceImpl.class);

	@Autowired
	CassandraOperation cassandraOperation;

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	PassbookParserHandler parserHanlder;

	@Override
	public SBApiResponse getPassbook(String requestedUserId, Map<String, Object> request) {
		return getPassbookDetails(requestedUserId, request, false);
	}

	@Override
	public SBApiResponse getPassbookByAdmin(String requestedId, Map<String, Object> request) {
		return getPassbookDetails(StringUtils.EMPTY, request, true);
	}

	@Override
	public SBApiResponse updatePassbook(String requestedUserId, Map<String, Object> request) {
		SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.PASSBOOK_ADD_API);

		String errMsg = validateUpdateRequest(request);
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}
		try {
			Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
			String typeName = (String) requestBody.get(Constants.TYPE_NAME);
			PassbookParser parser = getPassbookParser(typeName);

			List<Map<String, Object>> passbookDbInfoList = new ArrayList<Map<String, Object>>();
			errMsg = parser.validateUpdateReqeust(request, requestedUserId, passbookDbInfoList);
			if (errMsg.length() == 0) {
				SBApiResponse dbResponse = cassandraOperation.insertBulkRecord(Constants.KEYSPACE_SUNBIRD,
						Constants.USER_PASSBOOK_TABLE, passbookDbInfoList);
				if (!Constants.SUCCESS.equalsIgnoreCase((String) dbResponse.get(Constants.RESPONSE))) {
					errMsg = "Failed to add records into DB";
				}
			}
		} catch (Exception e) {
			errMsg = String.format("Failed to update passbook details. Exception: ", e.getMessage());
			logger.error(errMsg, e);
		}
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}
		return response;
	}

	private SBApiResponse getPassbookDetails(String requestedUserId, Map<String, Object> request, boolean isAdminApi) {
		SBApiResponse response = null;
		if (isAdminApi) {
			response = ProjectUtil.createDefaultResponse(Constants.PASSBOOK_ADMIN_READ_API);
		} else {
			response = ProjectUtil.createDefaultResponse(Constants.PASSBOOK_READ_API);
		}

		// Read request Data and validate it.
		String errMsg = validateReadRequest(request, isAdminApi);
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}

		try {
			Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
			String typeName = (String) requestBody.get(Constants.TYPE_NAME);
			Map<String, Object> propertyMap = new HashMap<>();
			List<String> userIdList = null;
			if (isAdminApi) {
				userIdList = (List<String>) requestBody.get(Constants.USER_ID);
			} else {
				userIdList = Arrays.asList(requestedUserId);
			}

			propertyMap.put(Constants.USER_ID, userIdList);
			propertyMap.put(Constants.TYPE_NAME, typeName);
			List<Map<String, Object>> passbookList = cassandraOperation.getRecordsByProperties(Constants.DATABASE,
					Constants.USER_PASSBOOK_TABLE, propertyMap, null);
			PassbookParser parser = getPassbookParser(typeName);
			parser.parseDBInfo(passbookList, response);
		} catch (Exception e) {
			errMsg = String.format("Failed to read passbook details. Exception: ", e.getMessage());
			logger.error(errMsg, e);
		}
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
			return response;
		}
		return response;
	}

	private String validateReadRequest(Map<String, Object> request, boolean isAdminApi) {
		Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(requestBody)) {
			return "Invalid Passbook Read request.";
		}

		StringBuilder errMsg = new StringBuilder();
		List<String> missingAttributes = new ArrayList<String>();
		List<String> errList = new ArrayList<String>();

		if (isAdminApi) {
			// Need to check at least one userId is available
			List<String> userIdList = (List<String>) requestBody.get(Constants.USER_ID);
			if (CollectionUtils.isEmpty(userIdList)) {
				missingAttributes.add(Constants.USER_ID);
			} else {
				if (userIdList.stream().allMatch(x -> x == null || x.isEmpty())) {
					errList.add(Constants.USER_ID + " contains null or empty. ");
				}
			}
		}

		String typeName = (String) requestBody.get(Constants.TYPE_NAME);
		if (StringUtils.isBlank(typeName)) {
			missingAttributes.add(Constants.TYPE_NAME);
		} else {
			if (!serverProperties.getUserPassbookSupportedTypeName().contains(typeName)) {
				errList.add(String.format("Invalid TypeName value. Supported TypeNames are %s",
						serverProperties.getUserPassbookSupportedTypeName()));
			}
		}

		if (!missingAttributes.isEmpty()) {
			errMsg.append("Request doesn't have mandatory parameters - [").append(missingAttributes.toString())
					.append("]. ");
		}

		if (!errList.isEmpty()) {
			errMsg.append(errList.toString());
		}

		return errMsg.toString();
	}

	private String validateUpdateRequest(Map<String, Object> request) {
		Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
		if (ObjectUtils.isEmpty(requestBody)) {
			return "Invalid Passbook Read request.";
		}

		StringBuilder errMsg = new StringBuilder();
		List<String> missingAttributes = new ArrayList<String>();
		List<String> errList = new ArrayList<String>();

		String typeName = (String) requestBody.get(Constants.TYPE_NAME);
		if (StringUtils.isBlank(typeName)) {
			missingAttributes.add(Constants.TYPE_NAME);
		} else {
			if (!serverProperties.getUserPassbookSupportedTypeName().contains(typeName)) {
				errList.add(String.format("Invalid TypeName value. Supported TypeNames are %s",
						serverProperties.getUserPassbookSupportedTypeName()));
			}
		}

		if (!missingAttributes.isEmpty()) {
			errMsg.append("Request doesn't have mandatory parameters - [").append(missingAttributes.toString())
					.append("]. ");
		}

		if (!errList.isEmpty()) {
			errMsg.append(errList.toString());
		}
		return errMsg.toString();
	}

	private PassbookParser getPassbookParser(String typeName) {
		return parserHanlder.getPassbookParser(typeName);
	}
}
