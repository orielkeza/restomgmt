package com.restomgmt.site.payment.services;

import com.restomgmt.site.payment.config.MomoConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MomoClient {

    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate;

    public String getAccessToken() {
        String credentials = momoConfig.getApiUser() + ":" + momoConfig.getApiKey();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.set("Ocp-Apim-Subscription-Key", momoConfig.getSubscriptionKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            momoConfig.getBaseUrl() + "/collection/token/",
            HttpMethod.POST,
            entity,
            Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("access_token")) {
            throw new IllegalStateException("Failed to get MTN access token");
        }

        log.debug("MTN access token obtained");
        return (String) body.get("access_token");
    }

    public String requestToPay(String accessToken, String payerPhone,
                                String amount, String currency,
                                String externalId, String note) {
        String referenceId = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("X-Reference-Id", referenceId);
        headers.set("X-Target-Environment", momoConfig.getEnvironment());
        headers.set("Ocp-Apim-Subscription-Key", momoConfig.getSubscriptionKey());

        Map<String, Object> body = Map.of(
            "amount", amount,
            "currency", currency,
            "externalId", externalId,
            "payer", Map.of(
                "partyIdType", "MSISDN",
                "partyId", payerPhone
            ),
            "payerMessage", note,
            "payeeNote", note
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
            momoConfig.getBaseUrl() + "/collection/v1_0/requesttopay",
            HttpMethod.POST,
            entity,
            Void.class
        );

        log.info("Payment request sent - referenceId: {}", referenceId);
        return referenceId;
    }

    public String checkPaymentStatus(String accessToken, String referenceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("X-Target-Environment", momoConfig.getEnvironment());
        headers.set("Ocp-Apim-Subscription-Key", momoConfig.getSubscriptionKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            momoConfig.getBaseUrl() + "/collection/v1_0/requesttopay/" + referenceId,
            HttpMethod.GET,
            entity,
            Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Empty response from MTN");
        }

        String status = (String) body.get("status");
        log.debug("Payment status for {}: {}", referenceId, status);
        return status;
    }
}