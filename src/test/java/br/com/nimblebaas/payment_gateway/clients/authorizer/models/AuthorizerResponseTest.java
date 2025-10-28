package br.com.nimblebaas.payment_gateway.clients.authorizer.models;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthorizerResponseTest {

    @Test
    void shouldReturnTrueWhenStatusIsSuccess() {
        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("success");

        assertTrue(response.isSuccess());
    }

    @Test
    void shouldReturnTrueWhenStatusIsSuccessIgnoringCase() {
        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("SUCCESS");

        assertTrue(response.isSuccess());
    }

    @Test
    void shouldReturnTrueWhenStatusIsSuccessMixedCase() {
        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("SuCcEsS");

        assertTrue(response.isSuccess());
    }

    @Test
    void shouldReturnFalseWhenStatusIsNotSuccess() {
        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("error");

        assertFalse(response.isSuccess());
    }

    @Test
    void shouldReturnFalseWhenStatusIsNull() {
        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus(null);

        assertFalse(response.isSuccess());
    }

    @Test
    void shouldReturnTrueWhenAuthorized() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(true);

        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("success");
        response.setData(data);

        assertTrue(response.isAuthorized());
    }

    @Test
    void shouldReturnFalseWhenNotAuthorized() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(false);

        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("success");
        response.setData(data);

        assertFalse(response.isAuthorized());
    }

    @Test
    void shouldReturnFalseWhenStatusIsNotSuccessButDataIsAuthorized() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(true);

        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("error");
        response.setData(data);

        assertFalse(response.isAuthorized());
    }

    @Test
    void shouldReturnFalseWhenDataIsNull() {
        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("success");
        response.setData(null);

        assertFalse(response.isAuthorized());
    }

    @Test
    void shouldReturnFalseWhenDataAuthorizedIsNull() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(null);

        AuthorizerResponse response = new AuthorizerResponse();
        response.setStatus("success");
        response.setData(data);

        assertFalse(response.isAuthorized());
    }

    @Test
    void shouldCreateResponseWithConstructor() {
        AuthorizerResponseData data = new AuthorizerResponseData(true);
        AuthorizerResponse response = new AuthorizerResponse("success", data);

        assertTrue(response.isSuccess());
        assertTrue(response.isAuthorized());
    }

    @Test
    void shouldCreateResponseWithNoArgsConstructor() {
        AuthorizerResponse response = new AuthorizerResponse();

        assertFalse(response.isSuccess());
        assertFalse(response.isAuthorized());
    }
}

