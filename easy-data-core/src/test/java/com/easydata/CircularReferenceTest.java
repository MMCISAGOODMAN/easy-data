package com.easydata;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.easydata.testmodel.Order;
import com.easydata.testmodel.User;

/**
 * Verifies that circular references (User → Order → User) do not cause stack overflow.
 */
class CircularReferenceTest {

    @Test
    void handlesCircularReferences() {
        assertDoesNotThrow(() -> {
            User user = EasyData.generate(User.class);
            for (Order order : user.getOrders()) {
                if (order.getUser() != null) {
                    order.getUser().getName();
                }
            }
        });
    }
}
