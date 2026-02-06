package com.meragroup.getdatatodb.controller;

import com.meragroup.getdatatodb.service.WebhookProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

	private final WebhookProcessingService webhookProcessingService;

	@PostMapping(path = "/orders", consumes = "application/json")
	public ResponseEntity<String> receiveOrderWebhook(@RequestBody String rawPayload) {
		try {
			// quick validation: ensure there's an id field
			if (rawPayload == null || rawPayload.isBlank()) {
				log.warn("Received empty webhook payload");
				System.out.println("Data: " + rawPayload);
				return ResponseEntity.ok("ok");
			}
			// process asynchronously within service (service is transactional)
			webhookProcessingService.processWebhook(rawPayload);
		} catch (Exception e) {
			// Always return 200 to caller per requirement; log for observability
			log.error("Error processing webhook", e);
		}
		return ResponseEntity.ok("ok");
	}

}


