package com.hms.dto.secure;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  @NotBlank
  @Pattern(regexp = "[A-Za-z0-9_.-]{3,80}")
  private String username;

  @NotBlank
  @Email
  @Size(max = 255)
  private String email;

  @NotBlank
  @Size(min = 12, max = 72)
  @ToString.Exclude
  private String password;

  @NotBlank
  @Size(max = 255)
  private String name;

  @NotBlank
  @Size(max = 255)
  @com.hms.web.ValidPhone
  private String phone;
}
