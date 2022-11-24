package org.sunbird.passbook.service;

import java.util.Map;

import org.sunbird.common.model.SBApiResponse;

/**
 * Provides the CURD APIs for passbook feature
 * 
 * @author karthik
 *
 */
public interface PassbookService {
	public SBApiResponse getPassbook(String requestedUserId, Map<String, Object> request);

	public SBApiResponse getPassbookByAdmin(String requestedId, Map<String, Object> request);
	
	public SBApiResponse updatePassbook(String requestedUserId, Map<String, Object> request);
}
