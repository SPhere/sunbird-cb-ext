package org.sunbird.passbook.competency.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompetencyInfo {
	private String competencyId;
	private Map<String, String> additionalParams;
	private List<Map<String, Object>> acquiredDetails = new ArrayList<Map<String, Object>>();

	public CompetencyInfo(String competencyId) {
		this.competencyId = competencyId;
	}

	public String getCompetencyId() {
		return competencyId;
	}

	public void setCompetencyId(String competencyId) {
		this.competencyId = competencyId;
	}

	public Map<String, String> getAdditionalParams() {
		return additionalParams;
	}

	public void setAdditionalParams(Map<String, String> additionalParams) {
		this.additionalParams = additionalParams;
	}

	public List<Map<String, Object>> getAcquiredDetails() {
		return acquiredDetails;
	}

	public void setAcquiredDetails(List<Map<String, Object>> acquiredDetails) {
		this.acquiredDetails = acquiredDetails;
	}
}
