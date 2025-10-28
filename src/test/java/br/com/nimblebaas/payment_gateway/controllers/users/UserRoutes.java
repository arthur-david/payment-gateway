package br.com.nimblebaas.payment_gateway.controllers.users;

public enum UserRoutes {
    BASE("/users"),
    REGISTER("/users/register"),
    CHANGE_PASSWORD("/users/password"),
    GET_USER("/users"),
    GET_CURRENT_USER("/users/me");

    private final String path;

    UserRoutes(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

