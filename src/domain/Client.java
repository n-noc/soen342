package domain;

import java.util.UUID;

public class Client {

    private final String clientId;
    private String name;
    private String email;
    private String phoneNumber;

    public Client(String name, String email, String phoneNumber) {
        this.clientId = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return String.format(
                "Client{id=%s, name=%s, email=%s, phone=%s}",
                clientId, name, email, phoneNumber
        );
    }
}