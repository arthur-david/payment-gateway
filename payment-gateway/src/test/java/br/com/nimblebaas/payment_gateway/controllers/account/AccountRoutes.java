package br.com.nimblebaas.payment_gateway.controllers.account;

public enum AccountRoutes {
    BASE("/accounts"),
    GET_BALANCE("/accounts/balance"),
    DEPOSIT("/accounts/deposit");

    private final String path;

    AccountRoutes(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

