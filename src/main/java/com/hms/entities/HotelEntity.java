package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;

/** Maps the hotels table. Relationships are unidirectional; use DTOs at API boundaries. */
@Entity
@Table(name = "hotels")
public class Hotel extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @NotNull
    @Column(name = "city", nullable = false, length = 255)
    private String city;

    @NotNull
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @NotNull
    @Column(name = "phone", nullable = false, length = 255)
    private String phone;

    @NotNull
    @Email
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotNull
    @Column(name = "timezone", nullable = false, length = 255)
    private String timezone;

    @NotNull
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @NotNull
    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;

    @NotNull
    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Hotel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

}
