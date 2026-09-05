package org.libraryexpress.infrastructure.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.libraryexpress.application.loan.dto.request.CreateLoanDto;
import org.libraryexpress.application.loan.dto.request.FilterLoansDto;
import org.libraryexpress.application.loan.dto.response.LoanDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

@Path("/loans")
@Tag(name = "Loans", description = "Loan lifecycle endpoints")
public final class LoanController {

    private final AppContext context;

    public LoanController(AppContext context) {
        this.context = context;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a loan", description = "Creates a loan for an available book and eligible customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan created"),
            @ApiResponse(responseCode = "400", description = "Validation failure"),
            @ApiResponse(responseCode = "404", description = "Book or customer not found"),
            @ApiResponse(responseCode = "409", description = "Book unavailable or active loan limit reached")
    })
    public void create(HttpContextRequest request, HttpContextResponse response) throws Exception {
        var inputDto = request.parseBody(CreateLoanDto.class);
        context.getCreateLoan().execute(inputDto);

        response.status(CREATED).sendEmpty();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search loans (paginated)", description = "Filters loans by customer, ISBN, and/or status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of loans",
                    content = @Content(schema = @Schema(implementation = LoanDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public void search(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        String customerId = request.getQueryParam("customerId");
        String ISBN = request.getQueryParam("ISBN");
        String statusesStr =  request.getQueryParam("statuses");

        Set<LoanStatus> statuses = new HashSet<>();

        if (statusesStr != null && !statusesStr.isBlank()) {
            try {
                statuses = Arrays.stream(statusesStr.split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .map(LoanStatus::valueOf)
                        .collect(Collectors.toSet());
            } catch (IllegalArgumentException e) {
                response.status(BAD_REQUEST).json("Invalid status values");
                return;
            }
        }

        var paginationDto = new InputPaginationDto(pageRequest.page(), pageRequest.size());
        var inputDto = new FilterLoansDto(customerId, ISBN, statuses, paginationDto);

        OutputPaginationDto<LoanDto> outputDto = context.getSearchLoans().execute(inputDto);

        response.status(SUCCESS).json(outputDto);
    }

    @POST
    @Path("/{loanId}/returns")
    @Operation(
            summary = "Return a loan",
            description = "Creates a return event for an active loan — updates both the loan status and the book's availability"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Loan returned successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "409", description = "Loan not in a returnable state")
    })
    public void returnLoan(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String loanId = request.getRouteParam("loanId");
        context.getReturnLoan().execute(loanId);

        response.status(NO_CONTENT).sendEmpty();
    }

    /**
     * TODO - Temporary/palliative flow.
     * Once fine calculation and score adjustment are implemented directly in ReturnLoan,
     * this flow should become obsolete — OVERDUE loans should transition to FINISHED
     * automatically at return time, not via manual intervention.
     */
    @PATCH
    @Path("/{loanId}/close-overdue")
    @Operation(
            summary = "Close an overdue loan (temporary/palliative flow)",
            description = "Manually transitions an OVERDUE loan to FINISHED. To be superseded once fine calculation and score adjustment are handled directly by ReturnLoan."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Overdue loan closed"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "409", description = "Loan not in OVERDUE state")
    })
    public void closeOverdueLoan(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String loanId = request.getRouteParam("loanId");
        context.getCloseOverdueLoan().execute(loanId);

        response.status(NO_CONTENT).sendEmpty();
    }
}
