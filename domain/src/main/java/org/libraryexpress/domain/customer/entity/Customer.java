package org.libraryexpress.domain.customer.entity;

import org.libraryexpress.domain.core.util.RandomGenerator;
import org.libraryexpress.domain.customer.valueobject.Email;

import java.util.Objects;

public class Customer implements Comparable<Customer> {

    private final String id;

    private final String name;

    private Email email;

    private Customer(String id, String name, Email email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static Customer create(String name, String email) {
        return new Customer(RandomGenerator.UUID(), name, new Email(email));
    }

    public static Customer restore(String id, String name, String email) {
        return new Customer(id, name, new Email(email));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public void changeEmail(String newEmail) {
        this.email = new Email(newEmail);
    }

    @Override
    public String toString() {
        return "{\n" +
                " Id: " + id + ",\n" +
                " name: " + name + ",\n" +
                " email: " + email + "\n" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Customer customer)) return false;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Customer otherCustomer) {
        return Objects.compare(this.name, otherCustomer.getName(), String::compareTo);
    }

}
