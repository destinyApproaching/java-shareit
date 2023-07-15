package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_IdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByBooker_IdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                                 LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                                 LocalDateTime end, Sort sort);

    List<Booking> findByBooker_Id(Long bookerId);

    List<Booking> findBookingByItem_Id(Long itemId);

    List<Booking> findBookingByItem_IdAndStatus(Long itemId, Status status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);
}
