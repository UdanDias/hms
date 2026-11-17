package com.hms.web;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public interface WebPortalService {
  Object hotel();

  Object roomTypes();

  Object plans();

  Object rooms();

  Object availability(WebRequests.Search input);

  Object quote(WebRequests.Draft input);

  JsonNode create(WebRequests.Create input, String access);

  JsonNode booking(Long id, String access);

  Object bookings(boolean mine);

  JsonNode cancel(Long id, String access);

  JsonNode transition(Long id, String status);

  Object condition(Long id, WebRequests.Condition input);

  Object profile();

  Object profile(WebRequests.Profile input);

  Object customers();

  Object reviews(String scope);

  Object review(WebRequests.Review input);

  Object moderate(Long id, String status);

  Object payments();

  Object cash(WebRequests.Cash input);

  Object users();

  Object role(Long id, String role);

  Object audit();

  Object hotel(WebRequests.Hotel input);

  Object setup(WebRequests.Setup input);
}
