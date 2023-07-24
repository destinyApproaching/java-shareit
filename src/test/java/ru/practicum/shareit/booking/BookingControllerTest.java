package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.StateException;
import ru.practicum.shareit.exception.TimeException;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;

    private User booker;

    @BeforeEach
    void setUp() {
        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(1))
                .bookerId(1L)
                .itemId(1L)
                .build();
        booker = User.builder()
                .id(1L)
                .name("booker")
                .email("booker@yandex.ru")
                .build();
    }

    @Test
    void createBookingTest() throws Exception {
        when(bookingService.createBooking(anyLong(), any(BookingDto.class))).thenReturn(bookingDto);
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createBookingWrongEndTest() throws Exception {
        bookingDto.setEnd(bookingDto.getStart().minusMinutes(10));
        when(bookingService.createBooking(anyLong(), any(BookingDto.class)))
                .thenThrow(new TimeException("Дата начала бронирования не может быть позднее даты окончания бронирования!"));
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeStatusTest() throws Exception {
        bookingDto.setStatus(Status.APPROVED);
        when(bookingService.changeStatus(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingDto);
        mockMvc.perform(MockMvcRequestBuilders.patch("/bookings/{bookingId}",
                                bookingDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("approved", "true")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId().intValue())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId().intValue())))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId().intValue())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void changeStatusWrongStatusTest() throws Exception {
        bookingDto.setStatus(Status.WAITING);
        when(bookingService.changeStatus(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new BookingException("Статус уже изменён."));
        mockMvc.perform(MockMvcRequestBuilders.patch("/bookings/{bookingId}",
                                bookingDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("approved", "false")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(bookingDto);
        mockMvc.perform(get("/bookings/{bookingId}", bookingDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingWrongBookingIdTest() throws Exception {
        Long bookingId = -7L;
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new BookingNotFoundException(String.format("Бронирование с id = %d не найдено.", bookingId)));
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsOfUserTest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("state", "APPROVED")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsOfUserWithoutParamTest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingByItemOwnerTest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingByItemOwnerWrongStateTest() throws Exception {
        when(bookingService.getBookingsByItemOwner(anyLong(), anyString(), any())).thenThrow(new StateException("Unknown state: UNSUPPORTED_STATUS"));
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("state", "UNSUPPORTED_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
