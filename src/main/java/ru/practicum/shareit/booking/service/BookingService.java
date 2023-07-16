package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(Long userId, BookingDto bookingDto);

    BookingDto changeStatus(Long userId, Long bookingId, boolean available);

    BookingDto getBooking(Long userId, Long bookingId);

    List<BookingDto> getBookingsOfUser(Long userId, String state);
    // Получение списка всех бронирований текущего пользователя.


    List<BookingDto> getBookingsByItemOwner(Long userId, String state);
    // Получение списка бронирований для всех вещей текущего пользователя.
}
