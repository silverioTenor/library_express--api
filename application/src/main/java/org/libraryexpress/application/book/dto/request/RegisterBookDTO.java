package org.libraryexpress.application.book.dto.request;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record RegisterBookDTO(
        String isbn,
        String title,
        String author,
        int publicationYear,
        String status
) {
    public RegisterBookDTO {
        List<String> errors = new ArrayList<>();

        if ((title == null || title.isBlank()) || (author == null || author.isBlank())) {
           errors.add("Title and author are mandatory");
        }

        if (publicationYear < 0 || publicationYear > LocalDate.now().getYear()) {
            errors.add("Publication year is invalid");
        }

        if (status != null && !status.isBlank()) {
            String strStatus = status.toUpperCase().trim();

            var result = Arrays.stream(BookStatus.values())
                    .filter(bookStatus -> bookStatus.name().equals(strStatus))
                    .findFirst();

            if (result.isEmpty()) {
                errors.add("Invalid status provided");
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    public boolean isManualRegistration() {
        return (!this.isbn().isBlank() && !this.status().isBlank());
    }

    public Book toEntity() {
        return Book.create(
                this.isbn(),
                this.title(),
                this.author(),
                this.publicationYear(),
                this.status()
        );
    }
}
