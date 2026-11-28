package com.shoploc.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(@NotBlank @Size(max = 255) String refusalReason) {
}
