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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import org.libraryexpress.application.loan.dto.response.LoanReturnResponseDTO;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;

@Tag(name = "Loans", description = "Loan lifecycle endpoints")
public interface LoanApiMapping {

    @Schema(name = "CreateLoanRequest")
    record CreateLoanContract(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            String customerId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            String bookId,

            @Schema(description = "(optional)") LocalDate dueDate) {}

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a loan",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateLoanContract.class))))
    @Parameters({@Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)})
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Loan created"),
        @ApiResponse(
                responseCode = "404",
                description = "Book or customer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Book unavailable or active loan size reached",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void create(HttpContextRequest request, HttpContextResponse response) throws Exception;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search loans (paginated)", requestBody = @RequestBody(content = @Content()))
    @Parameters({
        @Parameter(name = "id", in = ParameterIn.QUERY),
        @Parameter(name = "customerId", in = ParameterIn.QUERY),
        @Parameter(name = "bookId", in = ParameterIn.QUERY),
        @Parameter(name = "statuses", in = ParameterIn.QUERY, example = "active, overdue, finished"),
        @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Paginated list of loans",
                content = @Content(schema = @Schema(implementation = OutputPaginationDto.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid filter parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void search(HttpContextRequest request, HttpContextResponse response) throws Exception;

    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Return a loan",
            description =
                    "Creates a return event for an active loan — updates both the loan status and the book's availability",
            requestBody = @RequestBody(content = @Content()))
    @Parameters({
        @Parameter(name = "loanId", in = ParameterIn.PATH),
        @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Book returned",
                content = @Content(schema = @Schema(implementation = LoanReturnResponseDTO.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Loan not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void loanReturn(HttpContextRequest request, HttpContextResponse response) throws Exception;

    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Close an overdue loan (temporary/palliative flow)",
            description =
                    "Manually changes the status of an overdue loan (OVERDUE) to completed (FINISHED). "
                            + "This functionality will be replaced once the fine calculation is performed directly by the LoanReturn function.",
            requestBody = @RequestBody(content = @Content()))
    @Parameters({
        @Parameter(name = "loanId", in = ParameterIn.PATH),
        @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Overdue loan closed"),
        @ApiResponse(
                responseCode = "404",
                description = "Loan not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Loan not in OVERDUE state",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Only OVERDUE loans",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void closeOverdueLoan(HttpContextRequest request, HttpContextResponse response) throws Exception;
}
