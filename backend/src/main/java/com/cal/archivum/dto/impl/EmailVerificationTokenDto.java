package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.EmailVerificationDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailVerificationTokenDto(

        @NotBlank(message ="Verification token is required")
        @Size(max = 64, message = "Verification token must not exceed 64 characters")
        String token

) implements EmailVerificationDto {}