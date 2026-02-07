package com.comp5348.store.dto;

import lombok.Data;

@Data
public class RegistrationRequestDTO {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
}
