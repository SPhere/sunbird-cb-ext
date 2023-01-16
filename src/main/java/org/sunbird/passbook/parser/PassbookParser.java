package org.sunbird.passbook.parser;

import java.util.List;
import java.util.Map;

import org.sunbird.common.model.SBApiResponse;

public interface PassbookParser {
	public void parseDBInfo(List<Map<String, Object>> passbookList, SBApiResponse response);

	public String validateUpdateReqeust(Map<String, Object> request, String requestedUserId,
			List<Map<String, Object>> dbModel);
}
