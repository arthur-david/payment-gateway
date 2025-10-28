package br.com.nimblebaas.payment_gateway.controllers.charge;

public enum ChargeRoutes {
    BASE("/charges"),
    CREATE("/charges"),
    SENT("/charges/sent"),
    RECEIVED("/charges/received"),
    PAY("/charges/pay"),
    CANCEL("/charges/cancel");

    private final String path;

    ChargeRoutes(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

