package ru.practicum.shareit.exception;

public class ItemNotAvailable extends RuntimeException {
    public ItemNotAvailable(String message) {
        super(message);
    }
}
