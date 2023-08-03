package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository repository;

    @BeforeEach
    void beforeEach() {
        User user = new User();
        user.setName("name");
        user.setEmail("a@mail.com");

        entityManager.persist(user);

        User user1 = new User();
        user1.setName("name1");
        user1.setEmail("b@mail.com");

        entityManager.persist(user1);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("requestItem");
        itemRequest.setRequester(user1);
        itemRequest.setCreated(LocalDateTime.now());

        entityManager.persist(itemRequest);

        Item item = new Item();
        item.setOwner(user);
        item.setName("item");
        item.setAvailable(true);
        item.setDescription("description");
        item.setRequest(itemRequest);

        entityManager.persist(item);
    }

    @Test
    void findByNameOrDescriptionTest() {
        Page<Item> found = repository.findByNameOrDescription("iTeM", Pageable.ofSize(10));
        assertThat(found).isNotEmpty();
    }

    @Test
    void findByNameOrDescriptionNotFoundTest() {
        Page<Item> found = repository.findByNameOrDescription("nothing", Pageable.ofSize(10));
        assertThat(found).isEmpty();
    }
}
