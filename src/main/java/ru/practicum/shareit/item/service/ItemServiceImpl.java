package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    @Override
    public List<ItemDto> getItems(Long id) {
        List<Item> items = itemRepository.findAll();
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            ItemDto itemDto = ItemMapper.toItemDto(item);
            List<Comment> comments = commentRepository.findCommentsByItem_Id(item.getId());
            itemDto.setComments(comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
            if (Objects.equals(item.getOwner().getId(), id)) {
                List<Booking> bookings = bookingRepository.findBookingByItem_IdAndStatus(item.getId(), Status.APPROVED);
                if (bookings.size() != 0) {
                    bookings = bookings.stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .collect(Collectors.toList());
                    for (Booking booking : bookings) {
                        if (booking.getStart().isBefore(LocalDateTime.now())) {
                            itemDto.setLastBooking(BookingMapper.toBookingDto(booking));
                            break;
                        }
                    }
                    bookings = bookings.stream()
                            .sorted(Comparator.comparing(Booking::getStart))
                            .collect(Collectors.toList());
                    for (Booking booking : bookings) {
                        if (booking.getStart().isAfter(LocalDateTime.now())) {
                            itemDto.setNextBooking(BookingMapper.toBookingDto(booking));
                            break;
                        }
                    }
                }
                itemDtos.add(itemDto);
            }
        }
        return itemDtos.stream()
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        itemChecker(item);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            item.setOwner(userOptional.get());
            return ItemMapper.toItemDto(itemRepository.save(item));
        } else {
            throw new UserNotFoundException(String.format("Пользователь с id = %d не существует", userId));
        }
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long id, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            item.setOwner(userOptional.get());
            Optional<Item> itemOptional = itemRepository.findById(id);
            if (itemOptional.isPresent()) {
                Item oldItem = itemOptional.get();
                item.setId(oldItem.getId());
                if (item.getName() == null || item.getName().equals("")) {
                    item.setName(oldItem.getName());
                }
                if (item.getDescription() == null || item.getDescription().equals("")) {
                    item.setDescription(oldItem.getDescription());
                }
                if (item.getAvailable() == null) {
                    item.setAvailable(oldItem.getAvailable());
                }
                return ItemMapper.toItemDto(itemRepository.save(item));
            } else {
                throw new ItemNotFoundException(String.format("Вещи с id = %d не существует", id));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует", userId));
        }
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            ItemDto itemDto = ItemMapper.toItemDto(item);
            List<Comment> comments = commentRepository.findCommentsByItem_Id(item.getId());
            itemDto.setComments(comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
            if (Objects.equals(item.getOwner().getId(), userId)) {
                List<Booking> bookings = bookingRepository.findBookingByItem_IdAndStatus(item.getId(), Status.APPROVED);
                if (bookings.size() != 0) {
                    bookings = bookings.stream()
                            .sorted(Comparator.comparing(Booking::getId))
                            .collect(Collectors.toList());
                    itemDto.setLastBooking(BookingMapper.toBookingDto(bookings.get(0)));
                    itemDto.setNextBooking(BookingMapper.toBookingDto(bookings.get(3)));
                }
            }
            return itemDto;
        } else {
            throw new ItemNotFoundException(String.format("Вещи с id = %d не существует", id));
        }
    }

    @Override
    public List<ItemDto> search(String text) {
        List<Item> searchResult = new ArrayList<>();
        if (Objects.equals(text, "")) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.search(text);
        for (Item item : items) {
            if (item.getAvailable()) {
                searchResult.add(item);
            }
        }
        return searchResult.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        if (commentDto.getText().isEmpty()) {
            throw new CommentException("Текс не может быть пустым.");
        }
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException(String.format("Вещи с id = %d не существует", itemId));
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует", userId));
        }
        List<Booking> bookings = bookingRepository.findBookingByItem_Id(itemId);
        for (Booking booking : bookings) {
            if (Objects.equals(booking.getBooker().getId(), userId)
                    && booking.getStart().isBefore(LocalDateTime.now())
                    && booking.getStatus().equals(Status.APPROVED)) {
                Comment comment = CommentMapper.toComment(commentDto);
                comment.setAuthor(userOptional.get());
                comment.setItem(itemOptional.get());
                comment.setCreated(LocalDateTime.now());
                return CommentMapper.toCommentDto(commentRepository.save(comment));
            }
        }
        throw new UserException("Пользователь не арендовал вещь.");
    }

    private void itemChecker(Item item) {
        if (item.getName() == null || item.getName().equals("")) {
            throw new ValidationException("Отсутствует название вещи.");
        }
        if (item.getDescription() == null || item.getDescription().equals("")) {
            throw new ValidationException("Отсутствует описание вещи.");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Отсутствует обозначение наличия или отсутсвия вещи.");
        }
    }
}
