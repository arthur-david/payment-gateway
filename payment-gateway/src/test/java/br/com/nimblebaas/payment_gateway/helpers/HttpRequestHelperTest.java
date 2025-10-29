package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.servlet.http.HttpServletRequest;

class HttpRequestHelperTest {

    @Test
    void shouldThrowExceptionWhenTryingToInstantiate() throws Exception {
        Constructor<HttpRequestHelper> constructor = HttpRequestHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void shouldReturnUnknownWhenRequestIsNull() {
        String ip = HttpRequestHelper.getClientIp(null);

        assertEquals("unknown", ip);
    }

    @ParameterizedTest
    @CsvSource({
        "X-Forwarded-For,192.168.1.1,192.168.1.1",
        "X-Forwarded-For,'192.168.1.1, 10.0.0.1, 172.16.0.1',192.168.1.1",
        "Proxy-Client-IP,10.0.0.1,10.0.0.1"
    })
    void shouldGetClientIpFromHeaders(String headerName, String headerValue, String expectedIp) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(headerName)).thenReturn(headerValue);

        String ip = HttpRequestHelper.getClientIp(request);

        assertEquals(expectedIp, ip);
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

    @ParameterizedTest
    @CsvSource({"unknown", "''"})
    void shouldIgnoreInvalidHeaderValues(String headerValue) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(headerValue);
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

    @ParameterizedTest
    @CsvSource({"true,unknown", "false,"})
    void shouldReturnUnknownWhenUserAgentNotAvailable(boolean isNullRequest, String headerValue) {
        HttpServletRequest request = isNullRequest ? null : mock(HttpServletRequest.class);
        
        if (!isNullRequest) {
            when(request.getHeader("User-Agent")).thenReturn(headerValue);
        }

        String userAgent = HttpRequestHelper.getUserAgent(request);

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

