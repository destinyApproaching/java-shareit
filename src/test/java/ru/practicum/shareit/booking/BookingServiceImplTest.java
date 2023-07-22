package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceImplTest {
    private final BookingService bookingService;

    private final UserService userService;

    private final ItemService itemService;

    private ItemDto item;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void beforeEach() {
        UserDto user = userService.createUser(UserDto.builder()
                .name("name")
                .email("e@mail.com")
                .build());
        item = itemService.createItem(ItemDto.builder()
                .name("item")
                .description("desc")
                .available(true)
                .build(), user.getId());
    }

    @Test
    void createBookingTest() {
        userService.createUser(UserDto.builder()
                .name("name1")
                .email("a@mail.com")
                .build());

        BookingDto bookingDto = getBookingDto(LocalDateTime.now().plusMinutes(2), LocalDateTime.now().plusMinutes(4));

        BookingDto createdBooking = bookingService.createBooking(2L, bookingDto);

        assertEquals(1L, createdBooking.getId());
        assertEquals(1L, createdBooking.getItem().getId());
        assertEquals(Status.WAITING, createdBooking.getStatus());
        assertEquals(2L, createdBooking.getBooker().getId());
    }


    @Test
    void createBookingNotValidTimeTest() {
        userService.createUser(UserDto.builder()
                .name("name1")
                .email("a@mail.com")
                .build());

        BookingDto bookingDto = getBookingDto(LocalDateTime.now().plusMinutes(3), LocalDateTime.now().plusMinutes(1));

        assertThrows(TimeException.class, () -> bookingService.createBooking(2L, bookingDto));
    }

    @Test
    void createBookingOwnerTest() {
        BookingDto bookingDto = getBookingDto(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2));

        assertThrows(UserNotFoundException.class, () -> bookingService.createBooking(1L, bookingDto));
    }

    @Test
    void createBookingItemNotAvailableTest() {
        createBookingTest();

        item.setAvailable(false);
        itemService.updateItem(item,1L , 1L);

        BookingDto bookingDto = getBookingDto(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(6));
        assertThrows(ItemNotAvailable.class, () -> bookingService.createBooking(2L, bookingDto));
    }

    @Test
    void changeStatusNoOwnerTest() {
        createBookingTest();
        assertThrows(UserNotFoundException.class, () -> bookingService.changeStatus(2L, 1L, true));
    }

    @Test
    void changeStatusTest() {
        createBookingTest();
        bookingService.changeStatus(1L, 1L, true);
        assertThrows(BookingException.class, () -> bookingService.changeStatus(1L, 1L, true));
    }

    @Test
    void getBookingTest() {
        createBookingTest();
        BookingDto bookingDto = bookingService.getBooking(1L, 1L);
        assertNotNull(bookingDto);
        assertEquals(1L, bookingDto.getId());
        assertEquals(1L, bookingDto.getItem().getId());
        assertEquals(2L, bookingDto.getBooker().getId());
        assertEquals(Status.WAITING, bookingDto.getStatus());

        bookingService.changeStatus(1L, 1L, false);
        bookingDto = bookingService.getBooking(1L, 1L);
        assertEquals(Status.REJECTED, bookingDto.getStatus());
    }

    @Test
    void getBookingNotExistsTest() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(1L, 1L));
    }

    @Test
    void getBookingsOfUserStateAllTest() {
        createBookingTest();
        List<BookingDto> bookings = bookingService.getBookingsOfUser(2L, "ALL", pageable);
        assertEquals(1, bookings.size());
    }

    @Test
    void getBookingsOfUserStateFutureTest() {
        userService.createUser(UserDto.builder()
                .name("name")
                .email("@")
                .build());
        List<BookingDto> bookings = bookingService.getBookingsOfUser(2L, "FUTURE", pageable);
        assertEquals(0, bookings.size());
        bookings = bookingService.getBookingsOfUser(2L, "FUTURE", pageable);
        assertEquals(0, bookings.size());
    }

    @Test
    void getBookingsOfUserStateWaitingTest() {
        createBookingTest();
        List<BookingDto> bookings = bookingService.getBookingsOfUser(2L, "WAITING", pageable);
        assertEquals(1, bookings.size());
    }

    @Test
    void getBookingsOfUserStateRejectedTest() {
        createBookingTest();
        List<BookingDto> bookings = bookingService.getBookingsOfUser(2L, "REJECTED", pageable);
        assertEquals(0, bookings.size());

        bookingService.changeStatus(1L, 1L, false);
        bookings = bookingService.getBookingsOfUser(2L, "REJECTED", pageable);
        assertEquals(1, bookings.size());
    }

    @Test
    void getBookingsOfUserStateUnknownTest() {
        createBookingTest();
        assertThrows(StateException.class, () -> bookingService.getBookingsOfUser(2L, "Unknown", pageable));
    }

    @Test
    void getBookingsByItemOwnerStateAllTest() {
        createBookingTest();
        List<BookingDto> bookings = bookingService.getBookingsByItemOwner(1L, "ALL", pageable);
        assertEquals(1, bookings.size());
    }

    @Test
    void getBookingsByItemOwnerStateCurrentTest() {
        createBookingTest();
        List<BookingDto> bookings = bookingService.getBookingsByItemOwner(1L, "CURRENT", pageable);
        assertEquals(0, bookings.size());
    }

    @Test
    void getBookingsByItemOwnerStateFutureTest() {
        getBookingsOfUserStateFutureTest();
        List<BookingDto> bookings = bookingService.getBookingsByItemOwner(1L, "FUTURE", pageable);
        assertEquals(0, bookings.size());
    }

    @Test
    void getBookingsByItemOwnerStateWaitingTest() {
        createBookingTest();
        List<BookingDto> bookings = bookingService.getBookingsByItemOwner(1L, "WAITING", pageable);
        assertEquals(1, bookings.size());

        bookings = bookingService.getBookingsByItemOwner(2L, "WAITING", pageable);
        assertEquals(0, bookings.size());
    }

    @Test
    void getBookingsByItemOwnerStateRejectedTest() {
        createBookingTest();
        List<BookingDto> bookings = bookingService.getBookingsByItemOwner(1L, "REJECTED", pageable);
        assertEquals(0, bookings.size());

        bookingService.changeStatus(1L, 1L, false);

        bookings = bookingService.getBookingsByItemOwner(1L, "REJECTED", pageable);
        assertEquals(1, bookings.size());
    }

    @Test
    void getBookingsByItemOwnerStateUnknownTest() {
        createBookingTest();
        assertThrows(StateException.class, () -> bookingService.getBookingsByItemOwner(1L, "", pageable));
    }

    private BookingDto getBookingDto(LocalDateTime start, LocalDateTime end) {
        return BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(item.getId())
                .build();
    }
}
