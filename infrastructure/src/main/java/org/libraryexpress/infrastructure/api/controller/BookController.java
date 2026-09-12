package org.libraryexpress.infrastructure.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.libraryexpress.application.book.dto.request.RegisterBookDto;
import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

@Path("/books")
@Tag(name = "Books", description = "Book management endpoints")
public final class BookController {

    private final AppContext context;

    public BookController(AppContext context) {
        this.context = context;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Register a book",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = RegisterBookDto.class)))
    )
    @Parameters({
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failure",
                    content =  @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate ISBN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public void register(HttpContextRequest request, HttpContextResponse response) throws Exception {
        RegisterBookDto inputDto = request.parseBody(RegisterBookDto.class);
        context.getRegisterBook().execute(inputDto);

        response.status(CREATED).sendEmpty();
    }

    @GET
    @Path("/{isbn}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a book by ISBN", requestBody = @RequestBody(content = @Content()))
    @Parameters({
            @Parameter(name = "isbn", in = ParameterIn.PATH, required = true),
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = OutputPaginationDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content =  @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
    })
    public void get(HttpContextRequest request, HttpContextResponse response) throws Exception {
        BookDto outputDto = context.getFindBook()
                .execute(request.getRouteParam("isbn"));

        response.status(SUCCESS).json(outputDto);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List books", requestBody = @RequestBody(content = @Content()))
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of books",
            content = @Content(schema = @Schema(implementation = OutputPaginationDto.class))
    )
    public void list(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        var inputDto = InputPaginationDto.of(pageRequest.page(), pageRequest.size());
        OutputPaginationDto<BookDto> outputDto = context.getListBooks().execute(inputDto);

        response.status(SUCCESS).json(outputDto);
    }
}
