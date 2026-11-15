package org.libraryexpress.infrastructure.api.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.libraryexpress.application.loan.dto.request.CreateLoanDTO;
import org.libraryexpress.application.loan.dto.request.LoanFilterDTO;
import org.libraryexpress.application.loan.dto.response.CreateLoanResponseDTO;
import org.libraryexpress.application.loan.dto.response.LoanDTO;
import org.libraryexpress.application.loan.dto.response.LoanReturnResponseDTO;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.contract.openApi.LoanApiMapping;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

@Path("/loans")
public final class LoanController implements LoanApiMapping {

    private final AppContext context;

    public LoanController(AppContext context) {
        this.context = context;
    }

    @POST
    public void create(HttpContextRequest request, HttpContextResponse response) throws Exception {
        var inputDto = request.parseBody(CreateLoanDTO.class);
        CreateLoanResponseDTO responseDTO = context.getCreateLoan().execute(inputDto);

        response.status(CREATED).json(responseDTO);
    }

    @GET
    public void search(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        String id = request.getQueryParam("id");
        String customerId = request.getQueryParam("customerId");
        String ISBN = request.getQueryParam("isbn");
        String statuses = request.getQueryParam("statuses");

        var filterDTO = new LoanFilterDTO(id, customerId, ISBN, statuses, pageRequest.page(), pageRequest.size());
        OutputPaginationDto<LoanDTO> outputDto = context.getSearchLoans().execute(filterDTO);

        response.status(SUCCESS).json(outputDto);
    }

    @POST
    @Path("/{loanId}/returns")
    public void loanReturn(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String loanId = request.getRouteParam("loanId");
        LoanReturnResponseDTO responseDTO = context.getLoanReturn().execute(loanId);

        response.status(SUCCESS).json(responseDTO);
    }

    /**
     * TODO - Temporary/palliative flow.
     * Once fine calculation and score adjustment are implemented directly in LoanReturn,
     * this flow should become obsolete — OVERDUE loans should transition to FINISHED
     * automatically at return time, not via manual intervention.
     */
    @PATCH
    @Path("/{loanId}/close-overdue")
    public void closeOverdueLoan(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String loanId = request.getRouteParam("loanId");
        context.getCloseOverdueLoan().execute(loanId);

        response.status(NO_CONTENT).sendEmpty();
    }
}
