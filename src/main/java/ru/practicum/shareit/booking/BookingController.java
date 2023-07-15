package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.StateException;

import javax.validation.Valid;
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
                                              @RequestParam(defaultValue = "ALL") String state) {
        try {
            State.valueOf(state);

        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookingService.getBookingsOfUser(userId, State.valueOf(state));
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByItemOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        try {
            State.valueOf(state);

        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookingService.getBookingsByItemOwner(userId, State.valueOf(state));
    }
}
