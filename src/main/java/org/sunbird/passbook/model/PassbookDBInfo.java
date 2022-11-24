package org.sunbird.passbook.model;

import java.util.Map;

public class PassbookDBInfo {
	private String userId;
	private String typeName;
	private String acquiredChannel;
	private String typeId;
	private String contextId;
	private String effectiveDate;
	private Map<String, String> additionalParams;
	private Map<String, String> acquiredDetails;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getAcquiredChannel() {
		return acquiredChannel;
	}

	public void setAcquiredChannel(String acquiredChannel) {
		this.acquiredChannel = acquiredChannel;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
	public Map<String, String> getAdditionalParams() {
		return additionalParams;
	}

	public void setAdditionalParams(Map<String, String> additionalParams) {
		this.additionalParams = additionalParams;
	}

	public Map<String, String> getAcquiredDetails() {
		return acquiredDetails;
	}

	public void setAcquiredDetails(Map<String, String> acquiredDetails) {
		this.acquiredDetails = acquiredDetails;
	}

}
