package org.libraryexpress.infrastructure.api.controller;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.libraryexpress.application.book.dto.request.BookFilterDTO;
import org.libraryexpress.application.book.dto.request.RegisterBookDTO;
import org.libraryexpress.application.book.dto.response.BookDTO;
import org.libraryexpress.application.book.dto.response.RegisterBookResponseDTO;
import org.libraryexpress.application.book.usecase.FindBook;
import org.libraryexpress.application.book.usecase.RegisterBook;
import org.libraryexpress.application.book.usecase.SearchBooks;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.contract.openApi.BookApiMapping;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;

@Path("/books")
public final class BookController implements BookApiMapping {

    private final RegisterBook registerBook;
    private final FindBook findBook;
    private final SearchBooks searchBooks;

    public BookController(RegisterBook registerBook, FindBook findBook, SearchBooks searchBooks) {
        this.registerBook = registerBook;
        this.findBook = findBook;
        this.searchBooks = searchBooks;
    }

    @POST
    public void register(HttpContextRequest request, HttpContextResponse response) throws Exception {
        RegisterBookDTO inputDto = request.parseBody(RegisterBookDTO.class);
        RegisterBookResponseDTO responseDTO = registerBook.execute(inputDto);

        response.status(CREATED).json(responseDTO);
    }

    @GET
    @Path("/{isbn}")
    public void get(HttpContextRequest request, HttpContextResponse response) throws Exception {
        BookDTO outputDto = findBook.execute(request.getRouteParam("isbn"));
        response.status(SUCCESS).json(outputDto);
    }

    @GET
    public void search(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        String id = request.getQueryParam("id");
        String ISBN = request.getQueryParam("isbn");
        String title = request.getQueryParam("title");
        String author = request.getQueryParam("author");
        String statuses = request.getQueryParam("statuses");
        String publishedYear = request.getQueryParam("publishedYear");

        var filterBookDto = new BookFilterDTO(
                id, ISBN, title, author, statuses, publishedYear, pageRequest.page(), pageRequest.size());

        OutputPaginationDto<BookDTO> outputDto = searchBooks.execute(filterBookDto);
        response.status(SUCCESS).json(outputDto);
    }
}
