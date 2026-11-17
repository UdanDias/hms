package com.hms.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/web")
@RequiredArgsConstructor
public class WebPortalController {
  private final WebPortalService service;

  @GetMapping("/public/hotel")
  public Object hotel() {
    return service.hotel();
  }

  @GetMapping("/public/room-types")
  public Object types() {
    return service.roomTypes();
  }

  @GetMapping("/public/plans")
  public Object plans() {
    return service.plans();
  }

  @GetMapping("/public/rooms")
  public Object rooms() {
    return service.rooms();
  }

  @PostMapping("/public/availability")
  public Object availability(@Valid @RequestBody WebRequests.Search request) {
    return service.availability(request);
  }

  @PostMapping("/public/quote")
  public Object quote(@Valid @RequestBody WebRequests.Draft request) {
    return service.quote(request);
  }

  @GetMapping("/public/reviews")
  public Object reviews() {
    return service.reviews("public");
  }

  @PostMapping("/public/bookings")
  @ResponseStatus(HttpStatus.CREATED)
  public Object create(
      @Valid @RequestBody WebRequests.Create request,
      @RequestHeader("X-Booking-Token") String token) {
    return service.create(request, token);
  }

  @GetMapping("/public/bookings/{id}")
  public Object booking(
      @PathVariable Long id,
      @RequestHeader(value = "X-Booking-Token", required = false) String token) {
    return service.booking(id, token);
  }

  @PatchMapping("/public/bookings/{id}/cancel")
  public Object cancel(
      @PathVariable Long id,
      @RequestHeader(value = "X-Booking-Token", required = false) String token) {
    return service.cancel(id, token);
  }

  @GetMapping("/bookings")
  public Object bookings(@RequestParam(defaultValue = "true") boolean mine) {
    return service.bookings(mine);
  }

  @PatchMapping("/bookings/{id}/{status}")
  public Object transition(@PathVariable Long id, @PathVariable String status) {
    return service.transition(id, status);
  }

  @PatchMapping("/rooms/{id}")
  public Object condition(@PathVariable Long id, @Valid @RequestBody WebRequests.Condition input) {
    return service.condition(id, input);
  }

  @GetMapping("/profile")
  public Object profile() {
    return service.profile();
  }

  @PatchMapping("/profile")
  public Object profile(@Valid @RequestBody WebRequests.Profile input) {
    return service.profile(input);
  }

  @GetMapping("/customers")
  public Object customers() {
    return service.customers();
  }

  @GetMapping("/reviews")
  public Object reviews(@RequestParam(defaultValue = "mine") String scope) {
    if (!java.util.Set.of("mine", "all").contains(scope))
      throw new IllegalArgumentException("Unknown scope");
    return service.reviews(scope);
  }

  @PostMapping("/reviews")
  public Object review(@Valid @RequestBody WebRequests.Review input) {
    return service.review(input);
  }

  @PatchMapping("/reviews/{id}")
  public Object moderate(@PathVariable Long id, @Valid @RequestBody WebRequests.Moderation input) {
    return service.moderate(id, input.status());
  }

  @GetMapping("/payments")
  public Object payments() {
    return service.payments();
  }

  @PostMapping("/payments")
  public Object cash(@Valid @RequestBody WebRequests.Cash input) {
    return service.cash(input);
  }

  @GetMapping("/users")
  public Object users() {
    return service.users();
  }

  @PatchMapping("/users/{id}/role")
  public Object role(@PathVariable Long id, @Valid @RequestBody WebRequests.Role input) {
    return service.role(id, input.role());
  }

  @GetMapping("/audit")
  public Object audit() {
    return service.audit();
  }

  @PatchMapping("/hotel")
  public Object hotel(@Valid @RequestBody WebRequests.Hotel input) {
    return service.hotel(input);
  }

  @PostMapping("/setup")
  public Object setup(@Valid @RequestBody WebRequests.Setup input) {
    return service.setup(input);
  }
}
