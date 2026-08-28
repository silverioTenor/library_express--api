package org.libraryexpress.infrastructure.api.controller;

import org.libraryexpress.application.book.dto.request.RegisterBookDto;
import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

public final class BookController {

    private final AppContext context;

    public BookController(AppContext context) {
        this.context = context;
    }

    public void register(HttpContextRequest request, HttpContextResponse response) throws Exception {
        RegisterBookDto inputDto = request.parseBody(RegisterBookDto.class);
        context.getRegisterBook().execute(inputDto);

        response.status(CREATED).sendEmpty();
    }

    public void get(HttpContextRequest request, HttpContextResponse response) throws Exception {
        BookDto outputDto = context.getFindBook()
                .execute(request.getRouteParam("isbn"));

        response.status(SUCCESS).json(outputDto);
    }

    public void list(HttpContextRequest request, HttpContextResponse response) throws Exception {
        try {
            Pagination.PageRequest pageRequest = request.getPageRequest();

            var inputDto = InputPaginationDto.of(pageRequest.page(), pageRequest.size());
            OutputPaginationDto<BookDto> outputDto = context.getListBooks().execute(inputDto);

            response.status(SUCCESS).json(outputDto);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
