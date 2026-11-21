package org.libraryexpress.infrastructure.api.contract.openApi;

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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.libraryexpress.application.book.dto.response.BookDTO;
import org.libraryexpress.application.book.dto.response.RegisterBookResponseDTO;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;

@Tag(name = "Books", description = "Book management endpoints")
public interface BookApiMapping {

    @Schema(name = "RegisterBookRequest")
    record RegisterBookContract(
            @Schema(description = "(optional)") String isbn,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            String title,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            String author,

            @Schema(description = "(optional)") int publicationYear,
            @Schema(description = "(optional)") String status) {}

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Register a book",
            requestBody =
                    @RequestBody(
                            required = true,
                            content = @Content(schema = @Schema(implementation = RegisterBookContract.class))))
    @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Book created",
                content = @Content(schema = @Schema(implementation = RegisterBookResponseDTO.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Validation failure",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Duplicate isbn",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void register(HttpContextRequest request, HttpContextResponse response) throws Exception;

    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a book by isbn", requestBody = @RequestBody(content = @Content()))
    @Parameter(name = "isbn", in = ParameterIn.PATH, required = true)
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Book found",
                content = @Content(schema = @Schema(implementation = BookDTO.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Book not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void get(HttpContextRequest request, HttpContextResponse response) throws Exception;

    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search books (paginated)", requestBody = @RequestBody(content = @Content()))
    @Parameters({
        @Parameter(name = "id", in = ParameterIn.QUERY),
        @Parameter(name = "isbn", in = ParameterIn.QUERY),
        @Parameter(name = "title", in = ParameterIn.QUERY),
        @Parameter(name = "author", in = ParameterIn.QUERY),
        @Parameter(name = "statuses", in = ParameterIn.QUERY),
        @Parameter(name = "publishedYear", in = ParameterIn.QUERY),
        @Parameter(name = "page", in = ParameterIn.QUERY),
        @Parameter(name = "limit", in = ParameterIn.QUERY)
    })
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Paginated list of books",
                content = @Content(schema = @Schema(implementation = OutputPaginationDto.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Validation failure",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Book not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    void search(HttpContextRequest request, HttpContextResponse response) throws Exception;
}
