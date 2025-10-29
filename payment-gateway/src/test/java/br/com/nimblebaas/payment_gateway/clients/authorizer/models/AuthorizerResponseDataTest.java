package br.com.nimblebaas.payment_gateway.clients.authorizer.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthorizerResponseDataTest {

    @Test
    void shouldReturnTrueWhenAuthorizedIsTrue() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(true);

        assertTrue(data.isAuthorized());
    }

    @Test
    void shouldReturnFalseWhenAuthorizedIsFalse() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(false);

        assertFalse(data.isAuthorized());
    }

    @Test
    void shouldReturnFalseWhenAuthorizedIsNull() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(null);

        assertFalse(data.isAuthorized());
    }

    @Test
    void shouldCreateDataWithConstructor() {
        AuthorizerResponseData data = new AuthorizerResponseData(true);

        assertTrue(data.isAuthorized());
        assertEquals(true, data.getAuthorized());
    }

    @Test
    void shouldCreateDataWithNoArgsConstructor() {
        AuthorizerResponseData data = new AuthorizerResponseData();

        assertFalse(data.isAuthorized());
    }

    @Test
    void shouldSetAndGetAuthorized() {
        AuthorizerResponseData data = new AuthorizerResponseData();
        data.setAuthorized(true);

        assertEquals(true, data.getAuthorized());
        assertTrue(data.isAuthorized());
    }
}

