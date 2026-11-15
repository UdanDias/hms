package com.hms.dto.secure;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInDTO implements Serializable {
  private static final long serialVersionUID = 1L;
  @NotBlank @Email private String email;
  @NotBlank @ToString.Exclude private String password;
}
