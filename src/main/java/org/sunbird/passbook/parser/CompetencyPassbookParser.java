package org.sunbird.passbook.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.passbook.competency.model.CompetencyInfo;
import org.sunbird.passbook.competency.model.CompetencyPassbookInfo;
import org.sunbird.passbook.model.PassbookDBInfo;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CompetencyPassbookParser implements PassbookParser {

	@Autowired
	CbExtServerProperties serverProperties;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void parseDBInfo(List<Map<String, Object>> passbookList, SBApiResponse response) {
		if (CollectionUtils.isEmpty(passbookList)) {
			response.getResult().put(Constants.COUNT, 0);
			response.getResult().put(Constants.CONTENT, CollectionUtils.EMPTY_COLLECTION);
			return;
		}

		Map<String, CompetencyPassbookInfo> competencyMap = new HashMap<String, CompetencyPassbookInfo>();
		// Parse the read values from DB and add it into response.result object
		for (Map<String, Object> competencyObj : passbookList) {
			String userId = (String) competencyObj.get(Constants.USER_ID);
			CompetencyPassbookInfo competencyPassbookInfo = null;
			if (competencyMap.containsKey(userId)) {
				competencyPassbookInfo = competencyMap.get(userId);
			} else {
				competencyPassbookInfo = new CompetencyPassbookInfo(userId);
				competencyMap.put(userId, competencyPassbookInfo);
			}
			String competencyId = (String) competencyObj.get(Constants.TYPE_ID);
			CompetencyInfo competencyInfo = competencyPassbookInfo.getCompetencies().get(competencyId);
			if (competencyInfo == null) {
				competencyInfo = new CompetencyInfo(competencyId);
			}

			if (ObjectUtils.isEmpty(competencyInfo.getAdditionalParams())) {
				competencyInfo.setAdditionalParams((Map<String, String>) competencyObj.get(Constants.ADDITIONAL_PARAM));
			}
			Map<String, Object> acquiredDetail = new HashMap<String, Object>();
			acquiredDetail.put(Constants.ACQUIRED_CHANNEL, (String) competencyObj.get(Constants.ACQUIRED_CHANNEL));
			acquiredDetail.put(Constants.COMPETENCY_LEVEL_ID, (String) competencyObj.get(Constants.CONTEXT_ID));
			acquiredDetail.put(Constants.EFFECTIVE_DATE,
					ProjectUtil.getTimestampFromUUID((UUID) competencyObj.get(Constants.EFFECTIVE_DATE)));
			acquiredDetail.put(Constants.ADDITIONAL_PARAM,
					(Map<String, Object>) competencyObj.get(Constants.ACQUIRED_DETAILS));
			Map<String, Object> acquiredDetailAdditionalParam = (Map<String, Object>) competencyObj
					.get(Constants.ACQUIRED_DETAILS);

			Iterator<Entry<String, Object>> iterator = acquiredDetailAdditionalParam.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				if (entry.getValue() instanceof String) {
					acquiredDetail.put(entry.getKey(), (String) entry.getValue());
				} else {
					// TODO - We need JSON schema config to determine the type of value.
				}
			}

			competencyInfo.getAcquiredDetails().add(acquiredDetail);
			competencyPassbookInfo.getCompetencies().put(competencyId, competencyInfo);
		}
		response.getResult().put(Constants.COUNT, competencyMap.size());
		response.getResult().put(Constants.CONTENT, competencyMap.values());
	}

	@Override
	public String validateUpdateReqeust(Map<String, Object> request, String requestedUserId,
			List<Map<String, Object>> dbModel) {
		StringBuilder errMsg = new StringBuilder();
		PassbookDBInfo passbookDbInfo = null;
		Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
		List<String> missingAttributes = new ArrayList<String>();
		List<String> errList = new ArrayList<String>();
		Map<String, Map<String, Object>> competencyMap = new HashMap<String, Map<String, Object>>();

		String compUserId = (String) requestBody.get(Constants.USER_ID);
		if (StringUtils.isEmpty(compUserId)) {
			missingAttributes.add(Constants.USER_ID);
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

		List<Map<String, Object>> competencyList = (List<Map<String, Object>>) requestBody
				.get(Constants.COMPETENCY_DETAILS);
		if (CollectionUtils.isEmpty(competencyList)) {
			missingAttributes.add(Constants.COMPETENCY_DETAILS);
		} else if (errMsg.length() == 0) {
			for (Map<String, Object> competency : competencyList) {
				String err = validateCompetencyObject(requestedUserId, compUserId, competency, competencyMap);
				if (!StringUtils.isEmpty(err)) {
					errMsg.append(err);
					break;
				}
			}
		}

		if (errMsg.length() == 0) {
			dbModel.addAll(competencyMap.values());
		}
		return errMsg.toString();
	}

	private String validateCompetencyObject(String requestedUserId, String compUserId,
			Map<String, Object> competencyRequest, Map<String, Map<String, Object>> competencyMap) {
		if (ObjectUtils.isEmpty(competencyRequest)) {
			return "Invalid CompetencyDetail object";
		}
		StringBuilder errMsg = new StringBuilder();
		List<String> missingAttributes = new ArrayList<String>();
		List<String> errList = new ArrayList<String>();

		String competencyId = (String) competencyRequest.get(Constants.COMPETENCY_ID);
		if (StringUtils.isBlank(competencyId)) {
			missingAttributes.add(Constants.COMPETENCY_ID);
		}

		if (competencyMap.containsKey(competencyId)) {
			return String.format("Invalid Request. Competency %s is provided twice.", competencyId);
		}

		Map<String, Object> competency = new HashMap<String, Object>();
		competency.put(Constants.USER_ID, compUserId);
		competency.put(Constants.TYPE_ID, competencyId);
		competency.put(Constants.TYPE_NAME, Constants.COMPETENCY);

		Map<String, Object> acquiredDetailsMap = (Map<String, Object>) competencyRequest
				.get(Constants.ACQUIRED_DETAILS);
		if (ObjectUtils.isEmpty(acquiredDetailsMap)) {
			missingAttributes.add(Constants.ACQUIRED_DETAILS);
		} else {
			String acquiredChannel = (String) acquiredDetailsMap.get(Constants.ACQUIRED_CHANNEL);
			if (StringUtils.isBlank(acquiredChannel)) {
				missingAttributes.add(Constants.ACQUIRED_CHANNEL);
			} else {
				competency.put(Constants.ACQUIRED_CHANNEL, acquiredChannel);
				// Parse additionalParams from request
				Map<String, Object> acquiredDetailAdditionalParam = (Map<String, Object>) acquiredDetailsMap
						.get(Constants.ADDITIONAL_PARAM);
				Map<String, String> acquiredDetails = new HashMap<String, String>();
				if (!ObjectUtils.isEmpty(acquiredDetailAdditionalParam)) {
					Iterator<Entry<String, Object>> iterator = acquiredDetailAdditionalParam.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<String, Object> entry = iterator.next();
						if (entry.getValue() instanceof String) {
							acquiredDetails.put(entry.getKey(), (String) entry.getValue());
						} else {
							try {
								acquiredDetails.put(entry.getKey(), mapper.writeValueAsString(entry.getValue()));
							} catch (JsonProcessingException e) {
								errList.add("Failed to parse acquiredDetails for competency : " + competencyId);
								break;
							}
						}
					}
				}
				acquiredDetails.put(Constants.CREATED_BY, requestedUserId);
				acquiredDetails.put(Constants.CREATED_DATE, DateTime.now().toString());
				competency.put(Constants.ACQUIRED_DETAILS, acquiredDetails);
			}

			String competencyLevelId = (String) acquiredDetailsMap.get(Constants.COMPETENCY_LEVEL_ID);
			if (StringUtils.isBlank(competencyLevelId)) {
				missingAttributes.add(Constants.COMPETENCY_LEVEL_ID);
			} else {
				competency.put(Constants.CONTEXT_ID, competencyLevelId);
			}

			String effectiveDate = (String) acquiredDetailsMap.get(Constants.EFFECTIVE_DATE);
			if (StringUtils.isBlank(effectiveDate)) {
				competency.put(Constants.EFFECTIVE_DATE, UUIDs.timeBased());
				// missingAttributes.add(Constants.EFFECTIVE_DATE);
			} else {
				UUID effectiveDateUUID = ProjectUtil.getUUIDFromTimeStamp(effectiveDate);
				if (effectiveDateUUID == null) {
					errList.add("Invalid effectiveDate format.");
				} else {
					competency.put(Constants.EFFECTIVE_DATE, ProjectUtil.getUUIDFromTimeStamp(effectiveDate));
				}
			}
		}

		Map<String, String> additionalParams = (Map<String, String>) competencyRequest.get(Constants.ADDITIONAL_PARAM);
		if (!ObjectUtils.isEmpty(additionalParams)) {
			competency.put(Constants.ADDITIONAL_PARAM, additionalParams);
		}

		if (!missingAttributes.isEmpty()) {
			errMsg.append("Request doesn't have mandatory parameters - [").append(missingAttributes.toString())
					.append("]. ");
		}

		if (!errList.isEmpty()) {
			errMsg.append(errList.toString());
		}

		if (errMsg.length() == 0) {
			competencyMap.put(competencyId, competency);
		}

		return errMsg.toString();
	}
}
