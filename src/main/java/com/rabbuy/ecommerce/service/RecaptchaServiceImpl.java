package com.rabbuy.ecommerce.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RecaptchaServiceImpl implements RecaptchaService {

    @Inject
    @ConfigProperty(name = "recaptcha.secret.key")
    private String secretKey;

    @Inject
    @ConfigProperty(name = "recaptcha.verify.url")
    private String verifyUrl;

    // Django 默认的最小分数
    private final double MIN_SCORE = 0.5;

    @Override
    public boolean verify(String token, String action) throws SecurityException {
        if (token == null || token.isEmpty()) {
            throw new SecurityException("reCAPTCHA token is required");
        }

        Client client = ClientBuilder.newClient();
        Form form = new Form();
        form.param("secret", secretKey);
        form.param("response", token);

        try {
            Response response = client.target(verifyUrl)
                    .request()
                    .post(Entity.form(form));

            if (response.getStatus() != 200) {
                throw new SecurityException("reCAPTCHA verification request failed. Status: " + response.getStatus());
            }

            JsonObject json = response.readEntity(JsonObject.class);
            response.close();

            if (!json.getBoolean("success", false)) {
                // 记录错误 (json.getJsonArray("error-codes").toString())
                throw new SecurityException("reCAPTCHA verification failed.");
            }
            if (json.getJsonNumber("score").doubleValue() < MIN_SCORE) {
                throw new SecurityException("reCAPTCHA score too low.");
            }
            if (!action.equals(json.getString("action", ""))) {
                throw new SecurityException("reCAPTCHA action mismatch.");
            }

            return true;
        } catch (Exception e) {
            throw new SecurityException("reCAPTCHA verification error: " + e.getMessage(), e);
        } finally {
            client.close();
        }
    }
}