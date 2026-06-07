package com.easydata;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.easydata.annotation.GenerateData;
import com.easydata.jupiter.EasyDataExtension;
import com.easydata.testmodel.User;

class EasyDataExtensionTest {

    @Test
    @GenerateData
    void injectsGeneratedUser(User user) {
        assertNotNull(user);
        assertNotNull(user.getName());
        assertNull(user.getPassword());
    }

    @Test
    @ExtendWith(EasyDataExtension.class)
    void injectsViaExplicitExtension(@GenerateData User user) {
        assertNotNull(user);
        assertNotNull(user.getAddress());
    }
}
