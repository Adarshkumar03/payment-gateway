package com.example.paymentGateway.dto;

public class AccountListDTO {
    private Long id;
    private String holderName;

    public AccountListDTO(Long id, String holderName) {
        this.id = id;
        this.holderName = holderName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
}
