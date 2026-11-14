package com.hms.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String name;

    private String phone;

    private String email;

    @ToString.Exclude
    private String nic;

    private Integer age;

    private String address;

    private String countryCode;

    private LocalDate dateOfBirth;

    private String identityDocumentType;

    @ToString.Exclude
    private byte[] identityDocumentNumberEncrypted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
