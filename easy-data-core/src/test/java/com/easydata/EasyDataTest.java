package com.easydata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.easydata.testmodel.Address;
import com.easydata.testmodel.Order;
import com.easydata.testmodel.User;

class EasyDataTest {

    @Test
    void generateSimpleUser() {
        User user = EasyData.generate(User.class);

        assertNotNull(user);
        assertNotNull(user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getAge() > 0);
        assertNotNull(user.getBalance());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getBirthDate());
        assertNotNull(user.getLastLogin());
    }

    @Test
    void generateNestedObjects() {
        User user = EasyData.generate(User.class);

        assertNotNull(user.getAddress());
        assertNotNull(user.getAddress().getCity());
        assertNotNull(user.getAddress().getCountry());
    }

    @Test
    void generateCollections() {
        User user = EasyData.generate(User.class);

        assertNotNull(user.getOrders());
        assertEquals(3, user.getOrders().size());
        assertNotNull(user.getTags());
        assertEquals(3, user.getTags().size());
        assertNotNull(user.getMetadata());
        assertEquals(3, user.getMetadata().size());

        for (Order order : user.getOrders()) {
            assertNotNull(order.getOrderNo());
        }
    }

    @Test
    void annotationFixedFieldOverridesBuilderForOtherFields() {
        User user = EasyData.of(User.class)
                .field("name", "张三")
                .generate();

        assertEquals("张三", user.getName());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void builderCustomFieldAndIgnore() {
        User user = EasyData.of(User.class)
                .field("name", "张三")
                .ignore("password")
                .generate();

        assertEquals("张三", user.getName());
        assertNull(user.getPassword());
    }

    @Test
    void builderCustomCollectionSize() {
        User user = EasyData.of(User.class)
                .size("orders", 5)
                .generate();

        assertEquals(5, user.getOrders().size());
    }

    @Test
    void generateBatch() {
        List<User> users = EasyData.of(User.class).generate(10);
        assertEquals(10, users.size());
        for (User user : users) {
            assertNotNull(user.getName());
        }
    }

    @Test
    void nestedFieldPath() {
        Address address = EasyData.of(Address.class)
                .field("city", "Shanghai")
                .generate();

        assertEquals("Shanghai", address.getCity());
    }
}
