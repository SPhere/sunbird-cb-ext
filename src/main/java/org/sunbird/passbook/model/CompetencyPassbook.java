package org.sunbird.passbook.model;

import java.util.List;
import java.util.Map;

/**
 * Competency Passbook Object for user representation
 * 
 * @author karthik
 *
 */
public class CompetencyPassbook {
	private String competencyId;
	private Map<String, String> additionalParam;
	private List<Map<String, Object>> acquiredDetails;

	public String getCompetencyId() {
		return competencyId;
	}

	public void setCompetencyId(String competencyId) {
		this.competencyId = competencyId;
	}

	public Map<String, String> getAdditionalParam() {
		return additionalParam;
	}

	public void setAdditionalParam(Map<String, String> additionalParam) {
		this.additionalParam = additionalParam;
	}

	public List<Map<String, Object>> getAcquiredDetails() {
		return acquiredDetails;
	}

	public void setAcquiredDetails(List<Map<String, Object>> acquiredDetails) {
		this.acquiredDetails = acquiredDetails;
	}

}
