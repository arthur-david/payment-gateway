package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

class HttpRequestHelperTest {

    @Test
    void shouldThrowExceptionWhenTryingToInstantiate() throws Exception {
        Constructor<HttpRequestHelper> constructor = HttpRequestHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> {
            constructor.newInstance();
        });
    }

    @Test
    void shouldReturnUnknownWhenRequestIsNull() {
        String ip = HttpRequestHelper.getClientIp(null);

        assertEquals("unknown", ip);
    }

    @Test
    void shouldGetClientIpFromXForwardedForHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("192.168.1.1", ip);
    }

    @Test
    void shouldGetFirstIpFromMultipleXForwardedForIps() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1, 172.16.0.1");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("192.168.1.1", ip);
    }

    @Test
    void shouldGetClientIpFromProxyClientIPHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.1");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("10.0.0.1", ip);
    }

    @Test
    void shouldGetClientIpFromRemoteAddrWhenNoHeadersPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("127.0.0.1", ip);
    }

    @Test
    void shouldReturnUnknownWhenNoIpAvailable() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(null);

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("unknown", ip);
    }

    @Test
    void shouldIgnoreUnknownHeaderValue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("127.0.0.1", ip);
    }

    @Test
    void shouldIgnoreEmptyHeaderValue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("127.0.0.1", ip);
    }

    @Test
    void shouldGetUserAgentFromHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        String userAgent = HttpRequestHelper.getUserAgent(request);

        assertEquals("Mozilla/5.0", userAgent);
    }

    @Test
    void shouldReturnUnknownWhenUserAgentIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn(null);

        String userAgent = HttpRequestHelper.getUserAgent(request);

        assertEquals("unknown", userAgent);
    }

    @Test
    void shouldReturnUnknownWhenRequestIsNullForUserAgent() {
        String userAgent = HttpRequestHelper.getUserAgent(null);

        assertEquals("unknown", userAgent);
    }

    @Test
    void shouldFormatRequestInfo() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        String info = HttpRequestHelper.formatRequestInfo(request);

        assertEquals("IP: 192.168.1.1 | User-Agent: Mozilla/5.0", info);
    }

    @Test
    void shouldReturnUnknownWhenRequestIsNullForFormatRequestInfo() {
        String info = HttpRequestHelper.formatRequestInfo(null);

        assertEquals("unknown", info);
    }

    @Test
    void shouldFormatRequestInfoWithUnknownValues() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);

        String info = HttpRequestHelper.formatRequestInfo(request);

        assertEquals("IP: unknown | User-Agent: unknown", info);
    }

    @Test
    void shouldGetIpFromHTTPXForwardedForHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("172.16.0.1");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("172.16.0.1", ip);
    }

    @Test
    void shouldTrimIpWhenMultipleIpsWithSpaces() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("  192.168.1.1  , 10.0.0.1 ");

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals("192.168.1.1", ip);
    }
}

