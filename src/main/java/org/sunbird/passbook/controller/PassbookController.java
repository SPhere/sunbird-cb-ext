package org.sunbird.passbook.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.passbook.service.PassbookService;

/**
 * Provides REST APIs for Passbook feature
 * 
 * @author karthik
 *
 */
@RestController
public class PassbookController {
	@Autowired
	PassbookService passbookService;

	@PatchMapping("/user/v1/passbook")
	public ResponseEntity<SBApiResponse> updatePassbook(@RequestHeader(Constants.X_AUTH_USER_ID) String requestedUserId,
			@RequestBody Map<String, Object> request) {
		SBApiResponse response = passbookService.updatePassbook(requestedUserId, request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/user/v1/passbook")
	public ResponseEntity<SBApiResponse> getUserPassbook(
			@RequestHeader(Constants.X_AUTH_USER_ID) String requestedUserId, @RequestBody Map<String, Object> request)
			throws Exception {
		SBApiResponse response = passbookService.getPassbook(requestedUserId, request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}

	@PostMapping("/admin/user/v1/passbook")
	public ResponseEntity<SBApiResponse> getU(@RequestHeader(Constants.X_AUTH_USER_ID) String requestedUserId,
			@RequestBody Map<String, Object> request) throws Exception {
		SBApiResponse response = passbookService.getPassbookByAdmin(requestedUserId, request);
		return new ResponseEntity<>(response, response.getResponseCode());
	}
}
