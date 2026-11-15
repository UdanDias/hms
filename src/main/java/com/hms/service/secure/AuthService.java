package com.hms.service.secure;

import com.hms.dto.UserDTO;
import com.hms.dto.secure.*;

public interface AuthService {
  JWTAuthResponseDTO signIn(@jakarta.validation.Valid SignInDTO dto);

  JWTAuthResponseDTO signUp(@jakarta.validation.Valid SignUpDTO dto);

  UserDTO getCurrentUser();
}
