package com.hms.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class WebRequests {
  public record Identity(@NotNull @Pattern(regexp="NIC|PASSPORT") String documentType,
      @NotBlank @Size(min=3,max=50) String documentNumber) {}
  public record Meal(@NotNull @Pattern(regexp="bb|half-board|full-board") String id,
      @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal adultSupplement,
      @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal childSupplement) {}
  public record Search(
      @NotNull LocalDate checkIn, @NotNull LocalDate checkOut, @Min(1) @Max(6) int rooms) {}

  public record Draft(
      @NotNull LocalDate checkIn,
      @NotNull LocalDate checkOut,
      @Min(1) @Max(6) int rooms,
      @Min(0) @Max(12) int suite,
      @Min(0) @Max(6) int deluxe,
      @Min(1) @Max(12) int adults,
      @Min(0) @Max(12) int children,
      @NotBlank String mealPlan,
      @NotNull @Size(max = 6) List<Long> selectedRoomIds,
      boolean selectRooms,
      @NotNull @Size(min=1, max=6) List<@Valid Allocation> allocations) {}

  public record Allocation(
      @Pattern(regexp="suite|deluxe") @NotNull String category,
      @Min(1) @Max(2) int adults,
      @Min(0) @Max(2) int children) {}

  public record Guest(
      @NotBlank @Size(max = 120) String name,
      @NotBlank @Email @Size(max = 255) String email,
      @NotBlank @Size(min = 7, max = 30) @ValidPhone String phone,
      @Pattern(regexp = "NIC|PASSPORT") String documentType,
      @NotNull @Size(max = 50) String documentNumber,
      @NotNull @Size(max = 1000) String notes) {}

  public record Create(
      @Valid @NotNull Draft draft,
      @Valid @NotNull Guest guest,
      @NotBlank @Size(max = 80) String idempotencyKey,
      @NotNull @DecimalMin("0.00") BigDecimal expectedTotal) {}

  public record Condition(
      @Pattern(regexp = "available|servicing") @NotNull String status,
      @Pattern(regexp = "clean|dirty") @NotNull String housekeeping) {}

  public record Profile(
      @NotBlank @Size(max = 120) String name, @NotBlank @Size(min = 7, max = 30) @ValidPhone String phone) {}

  public record Hotel(
      @NotBlank @Size(max = 255) String name,
      @NotBlank @Size(max = 30) @ValidPhone String phone,
      @NotBlank @Size(max = 1000) String address,
      @NotBlank @Pattern(regexp="([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?") String checkInWindow,
      @NotBlank @Pattern(regexp="([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?") String checkOutWindow) {}

  public record Review(
      @NotNull Long bookingId,
      @Min(1) @Max(5) int rating,
      @NotBlank @Size(max = 120) String title,
      @NotBlank @Size(max = 1500) String comment) {}

  public record Moderation(@Pattern(regexp = "approved|rejected") @NotNull String status) {}

  public record Role(
      @Pattern(regexp = "ADMIN|MANAGER|RECEPTIONIST|HOUSEKEEPING|GUEST") @NotNull
          String role) {}

  public record Cash(
      @NotNull Long bookingId,
      @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) @NotNull BigDecimal amount,
      @NotBlank @Size(max = 80) String requestKey) {}

  public record Setup(
      @NotNull @DecimalMin("0.01") BigDecimal suiteRate,
      @NotNull @DecimalMin("0.01") BigDecimal deluxeRate,
      @NotNull @DecimalMin("0.00") BigDecimal breakfast,
      @NotNull @DecimalMin("0.00") BigDecimal halfBoard,
      @NotNull @DecimalMin("0.00") BigDecimal fullBoard,
      @NotNull LocalDate start,
      @NotNull LocalDate end) {}
}
