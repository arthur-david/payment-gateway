package br.com.nimblebaas.payment_gateway.helpers;

import jakarta.servlet.http.HttpServletRequest;

public class HttpRequestHelper {

    private static final String UNKNOWN = "unknown";

    private HttpRequestHelper() {
        throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada");
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : UNKNOWN;
    }

    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : UNKNOWN;
    }

    public static String formatRequestInfo(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = getClientIp(request);
        String userAgent = getUserAgent(request);
        
        return String.format("IP: %s | User-Agent: %s", ip, userAgent);
    }
}

