package com.hms.dto.request;

import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReviewReplyRequestDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @NotBlank private String reply;
}
