package br.com.nimblebaas.payment_gateway.controllers.authentication;

public enum AuthenticationRoutes {
    BASE("/authentication"),
    LOGIN("/authentication/login"),
    REFRESH_TOKEN("/authentication/refresh-token");

    private final String path;

    AuthenticationRoutes(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

