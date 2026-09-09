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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.libraryexpress.application.loan.dto.request.CreateLoanDto;
import org.libraryexpress.application.loan.dto.request.FilterLoansDto;
import org.libraryexpress.application.loan.dto.response.LoanDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
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
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a loan",
            requestBody = @RequestBody(content =  @Content(schema = @Schema(implementation = CreateLoanDto.class)))
    )
    @Parameters({
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan created"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book or customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Book unavailable or active loan size reached",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public void create(HttpContextRequest request, HttpContextResponse response) throws Exception {
        var inputDto = request.parseBody(CreateLoanDto.class);
        context.getCreateLoan().execute(inputDto);

        response.status(CREATED).sendEmpty();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search loans (paginated)", requestBody =  @RequestBody(content = @Content()))
    @Parameters({
            @Parameter(name = "customerId", in = ParameterIn.QUERY),
            @Parameter(name = "ISBN", in = ParameterIn.QUERY),
            @Parameter(name = "statuses", in = ParameterIn.QUERY, example = "ACTIVE, OVERDUE, FINISHED"),
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list of loans",
                    content = @Content(schema = @Schema(implementation = OutputPaginationDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public void search(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        var inputDto = new FilterLoansDto(
                request.getQueryParam("customerId"),
                request.getQueryParam("ISBN"),
                request.getQueryParam("statuses"),
                pageRequest.page(),
                pageRequest.size()
        );
        OutputPaginationDto<LoanDto> outputDto = context.getSearchLoans().execute(inputDto);

        response.status(SUCCESS).json(outputDto);
    }

    @POST
    @Path("/{loanId}/returns")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Return a loan",
            description = "Creates a return event for an active loan — updates both the loan status and the book's availability",
            requestBody =  @RequestBody(content = @Content())
    )
    @Parameters({
            @Parameter(name = "loanId", in = ParameterIn.PATH),
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Loan returned successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Loan not in a returnable state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
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
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Close an overdue loan (temporary/palliative flow)",
            description = "Manually changes the status of an overdue loan (OVERDUE) to completed (FINISHED). " +
                    "This functionality will be replaced once the fine calculation is performed directly by the ReturnLoan function.",
            requestBody =  @RequestBody(content = @Content())
    )
    @Parameters({
            @Parameter(name = "loanId", in = ParameterIn.PATH),
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Overdue loan closed"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Loan not in OVERDUE state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public void closeOverdueLoan(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String loanId = request.getRouteParam("loanId");
        context.getCloseOverdueLoan().execute(loanId);

        response.status(NO_CONTENT).sendEmpty();
    }
}
