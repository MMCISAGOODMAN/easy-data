package com.easydata;

import com.easydata.testmodel.User;

/**
 * Quick demo for documentation screenshots.
 */
public final class DemoMain {

    public static void main(String[] args) {
        System.out.println("=== EasyData Demo ===\n");

        System.out.println("1) One line generate:");
        User user = EasyData.generate(User.class);
        printUser(user);

        System.out.println("\n2) Builder customize:");
        User custom = EasyData.of(User.class)
                .field("name", "张三")
                .size("orders", 2)
                .ignore("password")
                .generate();
        printUser(custom);

        System.out.println("\n3) Batch generate:");
        System.out.println("count = " + EasyData.of(User.class).generate(3).size());
        System.out.println("Done.");
    }

    private static void printUser(User user) {
        System.out.println("  name     = " + user.getName());
        System.out.println("  email    = " + user.getEmail());
        System.out.println("  age      = " + user.getAge());
        System.out.println("  city     = " + (user.getAddress() != null ? user.getAddress().getCity() : null));
        System.out.println("  orders   = " + (user.getOrders() != null ? user.getOrders().size() : 0));
        System.out.println("  password = " + user.getPassword());
    }
}
