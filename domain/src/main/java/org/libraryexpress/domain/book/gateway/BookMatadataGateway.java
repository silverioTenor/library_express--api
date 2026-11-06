package org.libraryexpress.domain.book.gateway;

import org.libraryexpress.domain.book.valueobject.BookMetadata;

public interface BookMatadataGateway {
    BookMetadata get(String title, String author);
}
