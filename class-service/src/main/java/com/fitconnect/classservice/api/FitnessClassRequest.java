package com.fitconnect.classservice.api;

import com.fitconnect.classservice.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FitnessClassRequest(@NotBlank @Size(min=3) String name, @NotBlank String description, @NotBlank String instructor, @NotBlank String gymLocation, @NotNull ClassCategory category, @NotNull ClassLevel level, @NotNull @Min(30) Integer durationMinutes, @NotNull @Min(5) @Max(30) Integer maxParticipants, @Min(0) Integer currentParticipants, @NotNull @DecimalMin("5.00") BigDecimal price, @NotNull @Future LocalDateTime dateTime) {}
