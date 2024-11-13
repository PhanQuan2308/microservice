package org.example.paymentservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayPalService {

    @Value("${paypal.client_id}")
    private String clientId;

    @Value("${paypal.secret}")
    private String clientSecret;

    @Value("${paypal.api_url}")
    private String apiUrl;

    @Value("${paypal.return_url}")
    private String returnUrl;

    @Value("${paypal.cancel_url}")
    private String cancelUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createPayment(Double amount) throws Exception {
        URI uri = new URI(apiUrl + "/v2/checkout/orders");
        System.out.println("Creating PayPal payment with URI: " + uri);

        Map<String, Object> body = new HashMap<>();
        body.put("intent", "CAPTURE");
        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("amount", Map.of("currency_code", "USD", "value", amount.toString()));
        body.put("purchase_units", new Map[]{purchaseUnit});
        body.put("application_context", Map.of("return_url", returnUrl, "cancel_url", cancelUrl));

        String accessToken = getAccessToken();
        System.out.println("Access Token retrieved: " + accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        System.out.println("Sending request to PayPal with body: " + body);

        try {
            String response = restTemplate.postForObject(uri, entity, String.class);
            System.out.println("PayPal Response: " + response);

            JsonNode jsonResponse = objectMapper.readTree(response);
            String approvalLink = jsonResponse.get("links").get(1).get("href").asText();
            System.out.println("Approval Link: " + approvalLink);
            return approvalLink;
        } catch (Exception e) {
            System.err.println("Error in createPayment: " + e.getMessage());
            throw new RuntimeException("Error creating PayPal payment", e);
        }
    }

    private String getAccessToken() throws Exception {
        URI uri = new URI(apiUrl + "/v1/oauth2/token");
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        System.out.println("Base64 Encoded Credentials: " + credentials);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + credentials);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        System.out.println("Sending request to get Access Token");

        try {
            String response = restTemplate.postForObject(uri, entity, String.class);
            System.out.println("Access Token Response: " + response);

            JsonNode jsonResponse = objectMapper.readTree(response);
            String accessToken = jsonResponse.get("access_token").asText();
            System.out.println("Access Token: " + accessToken);
            return accessToken;
        } catch (Exception e) {
            System.err.println("Error in getAccessToken: " + e.getMessage());
            throw new RuntimeException("Error fetching PayPal access token", e);
        }
    }
    public boolean verifyPaymentStatus(String paymentToken, Double amount) {
        try {
            String accessToken = getAccessToken();
            String url = apiUrl + "/v2/checkout/orders/" + paymentToken;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            System.out.println("PayPal Verification Response: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());
                String status = rootNode.path("status").asText();
                Double paidAmount = rootNode.path("purchase_units").get(0).path("amount").path("value").asDouble();

                // Nếu trạng thái là "COMPLETED", thực hiện xác nhận thanh toán
                if ("APPROVED".equalsIgnoreCase(status) && amount.equals(paidAmount)) {
                    String captureUrl = rootNode.path("links").get(2).path("href").asText();
                    HttpEntity<String> captureEntity = new HttpEntity<>(headers);
                    ResponseEntity<String> captureResponse = restTemplate.exchange(captureUrl, HttpMethod.POST, captureEntity, String.class);
                    System.out.println("Capture Response: " + captureResponse.getBody());

                    if (captureResponse.getStatusCode() == HttpStatus.CREATED) {
                        JsonNode captureNode = mapper.readTree(captureResponse.getBody());
                        String captureStatus = captureNode.path("status").asText();
                        return "COMPLETED".equalsIgnoreCase(captureStatus);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error verifying payment status: " + e.getMessage());
        }
        return false;
    }


}
