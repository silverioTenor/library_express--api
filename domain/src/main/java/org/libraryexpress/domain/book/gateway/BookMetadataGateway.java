package org.libraryexpress.domain.book.gateway;

import org.libraryexpress.domain.book.valueobject.BookMetadata;

import java.util.Optional;

public interface BookMetadataGateway {
    Optional<BookMetadata> fetch(String title, String author);
}
