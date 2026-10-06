package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.EmailVerificationDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmailVerificationTokenDto(

        @NotNull
        @Size(max = 64)
        String token
                                        ) implements EmailVerificationDto {
}
