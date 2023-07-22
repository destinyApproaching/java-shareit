package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody BookingDto bookingDto,
                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto changeStatus(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId,
                                      @RequestParam(name = "approved") Boolean available) {
        return bookingService.changeStatus(userId, bookingId, available);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {

        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsOfUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(defaultValue = "ALL") String state,
                                              @RequestParam(defaultValue = "0") @Min(0) @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Min(1) @Max(100) @Positive Integer size) {
        if (from < 0 || size < 1) {
            throw new ValidationException("Переданы неверные параметры.");
        }
        return bookingService.getBookingsOfUser(userId, state, PageRequest.of(from / size, size));
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByItemOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(defaultValue = "0") @Min(0) @PositiveOrZero Integer from,
                                                   @RequestParam(defaultValue = "10") @Min(1) @Max(100) @Positive Integer size) {
        if (from < 0 || size < 1) {
            throw new ValidationException("Переданы неверные параметры.");
        }
        return bookingService.getBookingsByItemOwner(userId, state, PageRequest.of(from / size, size));
    }
}
