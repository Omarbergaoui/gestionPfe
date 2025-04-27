package com.Application.Gestion.des.PFE.algorithme;

import java.time.LocalDateTime;
import java.util.Objects;

// Data structure for a Time Slot (used in unavailability)
public record TimeSlot(LocalDateTime start, LocalDateTime end) {
    // Constructor with validation
    public TimeSlot {
        Objects.requireNonNull(start, "Start time cannot be null");
        Objects.requireNonNull(end, "End time cannot be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be strictly before end time.");
        }
    }
}
