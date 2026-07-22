package com.hospital.exception;

/** Thrown when a slot is already booked, or was just taken by a race-condition winner. */
public class SlotConflictException extends RuntimeException {
    public SlotConflictException(String message) {
        super(message);
    }
}
