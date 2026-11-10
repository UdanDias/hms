package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the customer table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "customer")
public class CustomerEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = true, unique = true)
    private User user;

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "phone", nullable = false, length = 255)
    private String phone;

    @Email
    @Column(name = "email", nullable = true, length = 255)
    private String email;

    @Column(name = "nic", nullable = true, length = 255)
    // Encrypt sensitive NIC values in the service layer before storage.
    private String nic;

    @Min(0)
    @Max(150)
    @Column(name = "age", nullable = true)
    // Optional captured age; dateOfBirth is preferred for calculating current age.
    private Integer age;

    @Column(name = "address", nullable = true, columnDefinition = "TEXT")
    private String address;

    @Column(name = "country_code", nullable = true, length = 2)
    private String countryCode;

    @Column(name = "date_of_birth", nullable = true)
    private LocalDate dateOfBirth;

    @Column(name = "identity_document_type", nullable = true, length = 255)
    private String identityDocumentType;

    @Column(name = "identity_document_number_encrypted", nullable = true, length = 1024)
    private byte[] identityDocumentNumberEncrypted;

    public CustomerEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getIdentityDocumentType() { return identityDocumentType; }
    public void setIdentityDocumentType(String identityDocumentType) { this.identityDocumentType = identityDocumentType; }

    public byte[] getIdentityDocumentNumberEncrypted() { return identityDocumentNumberEncrypted; }
    public void setIdentityDocumentNumberEncrypted(byte[] identityDocumentNumberEncrypted) { this.identityDocumentNumberEncrypted = identityDocumentNumberEncrypted; }

}
