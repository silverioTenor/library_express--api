package org.libraryexpress.domain.book.gateway;

import java.util.Optional;
import org.libraryexpress.domain.book.valueobject.BookMetadata;

public interface BookMetadataGateway {
    Optional<BookMetadata> fetch(String title, String author);
}
