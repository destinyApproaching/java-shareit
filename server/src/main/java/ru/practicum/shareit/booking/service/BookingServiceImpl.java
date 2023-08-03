package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        timeChecker(bookingDto);
        Booking booking = BookingMapper.toBooking(bookingDto);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }
        booking.setBooker(userOptional.get());
        Optional<Item> itemOptional = itemRepository.findById(bookingDto.getItemId());
        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException(String.format("Вещи с id = %d не существует", bookingDto.getItemId()));
        }
        if (!itemOptional.get().getAvailable()) {
            throw new ItemNotAvailable(String.format("%s не доступна для аренды.", itemOptional.get().getName()));
        }
        if (Objects.equals(itemOptional.get().getOwner().getId(), userId)) {
            throw new UserNotFoundException("Владелец не может бронировать свою вещь.");
        }
        booking.setItem(itemOptional.get());
        booking.setStatus(Status.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto changeStatus(Long userId, Long bookingId, boolean available) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException(String.format("Бронирование с id = %d не найдено.", bookingId));
        }
        if (!Objects.equals(bookingOptional.get().getItem().getOwner().getId(), userId)) {
            throw new UserNotFoundException("Пользователь не является владельцем вещи.");
        }
        if (bookingOptional.get().getStatus() != Status.WAITING) {
            throw new BookingException("Статус уже изменён.");
        }
        Booking booking = bookingOptional.get();
        if (available) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException(String.format("Бронирование с id = %d не найдено.", bookingId));
        }
        if (!Objects.equals(bookingOptional.get().getBooker().getId(), userId)
                && !Objects.equals(bookingOptional.get().getItem().getOwner().getId(), userId)) {
            throw new UserNotFoundException("Пользователь не является владельцем вещи или автором бронирования.");
        }
        return BookingMapper.toBookingDto(bookingOptional.get());
    }

    @Override
    public List<BookingDto> getBookingsOfUser(Long userId, String state, Pageable pageable) {
        try {
            State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }
        Page<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();
        switch (State.valueOf(state)) {
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId, time, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId, time, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        time, time, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED,pageable);
                break;
            default:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByItemOwner(Long userId, String state, Pageable pageable) {
        try {
            State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }
        Page<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();
        switch (State.valueOf(state)) {
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId, time, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId, time, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        time, time, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING,
                        pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED,
                        pageable);
                break;
            default:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                break;
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void timeChecker(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new TimeException("Поля не могут быть пустыми");
        }
        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new TimeException("Дата начала бронирования не может совпадать с датой окончания!");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new TimeException("Дата начала бронирования не может быть раньше текущего момента!");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new TimeException("Дата начала бронирования не может быть позднее даты окончания бронирования!");
        }
    }
}
