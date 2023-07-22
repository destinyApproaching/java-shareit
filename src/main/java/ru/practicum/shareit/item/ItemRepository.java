package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item as i where i.available = true " +
            "and (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%')) )")
    Page<Item> findByNameOrDescription(String text, Pageable pageable);

    List<Item> findItemsByRequestId(Long requestId);

    Page<Item> findByOwnerId(Long userId, Pageable pageable);

    List<Item> findAllByRequestId(Long requestId);
}
