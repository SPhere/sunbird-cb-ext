package org.sunbird.common.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.sunbird.common.exceptions.ProjectCommonException;
import org.sunbird.common.exceptions.ResponseCode;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class will contains all the common utility methods.
 *
 * @author Manzarul
 */
public class ProjectUtil {
	private static Logger logger = LoggerFactory.getLogger(ProjectUtil.class);
	public static PropertiesCache propertiesCache;
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		propertiesCache = PropertiesCache.getInstance();
	}

	public static String getConfigValue(String key) {
		if (StringUtils.isNotBlank(System.getenv(key))) {
			return System.getenv(key);
		}
		return propertiesCache.readProperty(key);
	}

	/**
	 * This method will check incoming value is null or empty it will do empty check
	 * by doing trim method. in case of null or empty it will return true else
	 * false.
	 *
	 * @param value
	 * @return
	 */
	public static boolean isStringNullOREmpty(String value) {
		return (value == null || "".equals(value.trim()));
	}

	/**
	 * This method will create and return server exception to caller.
	 *
	 * @param responseCode ResponseCode
	 * @return ProjectCommonException
	 */
	public static ProjectCommonException createServerError(ResponseCode responseCode) {
		return new ProjectCommonException(responseCode.getErrorCode(), responseCode.getErrorMessage(),
				ResponseCode.SERVER_ERROR.getResponseCode());
	}

	public static ProjectCommonException createClientException(ResponseCode responseCode) {
		return new ProjectCommonException(responseCode.getErrorCode(), responseCode.getErrorMessage(),
				ResponseCode.CLIENT_ERROR.getResponseCode());
	}

	public enum Method {
		GET, POST, PUT, DELETE, PATCH
	}

	public static SBApiResponse createDefaultResponse(String api) {
		SBApiResponse response = new SBApiResponse();
		response.setId(api);
		response.setVer(Constants.API_VERSION_1);
		response.setParams(new SunbirdApiRespParam());
		response.getParams().setStatus(Constants.SUCCESS);
		response.setResponseCode(HttpStatus.OK);
		response.setTs(DateTime.now().toString());
		return response;
	}

	public static Map<String, String> getDefaultHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		return headers;
	}

	public static Timestamp getTimestampFromUUID(UUID timeStampUUID) {
		Long timeStamp = (timeStampUUID.timestamp() - Constants.NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000L;
		return new Timestamp(timeStamp);
	}

	public static UUID getUUIDFromTimeStamp(String time) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			Date date = df.parse(time);
			long epoch = date.getTime();

			long tmp = (epoch - Constants.NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
			Random random = new Random();
			return new UUID(UUIDs.startOf(tmp).getMostSignificantBits(), random.nextLong());
		} catch (Exception e) {
			logger.error("Failed to convert String to UUID time. Exception :", e);
		}
		return null;
	}
}