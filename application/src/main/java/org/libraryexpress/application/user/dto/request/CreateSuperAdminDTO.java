package org.libraryexpress.application.user.dto.request;

import java.util.ArrayList;
import java.util.List;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

public record CreateSuperAdminDTO(String name, String email, String password) {
    public CreateSuperAdminDTO {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) errors.add("Name is required.");
        if (email == null || email.isBlank()) errors.add("Email is required.");
        if (password == null || password.isBlank()) errors.add("Password is required.");

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    @Override
    public String toString() {
        return "CreateSuperAdminDTO[name=%s, email=%s, password=***]".formatted(name, email);
    }
}
