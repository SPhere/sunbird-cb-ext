package org.sunbird.passbook.competency.model;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.util.Constants;

public class CompetencyPassbookInfo {
	private String userId;
	private String typeName = Constants.COMPETENCY;
	private Map<String, CompetencyInfo> competencies = new HashMap<String, CompetencyInfo>();

	public CompetencyPassbookInfo(String userId) {
		this.userId = userId;
	}

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

	public Map<String, CompetencyInfo> getCompetencies() {
		return competencies;
	}

	public void setCompetencies(Map<String, CompetencyInfo> competencies) {
		this.competencies = competencies;
	}
}
