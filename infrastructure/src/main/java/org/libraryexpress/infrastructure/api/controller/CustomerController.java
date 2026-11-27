package org.libraryexpress.infrastructure.api.controller;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.libraryexpress.application.customer.dto.request.CreateCustomerDTO;
import org.libraryexpress.application.customer.dto.request.CustomerFilterDTO;
import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDTO;
import org.libraryexpress.application.customer.dto.response.CreateCustomerResponseDTO;
import org.libraryexpress.application.customer.usecase.CreateCustomer;
import org.libraryexpress.application.customer.usecase.SearchCustomers;
import org.libraryexpress.application.customer.usecase.UpdateCustomerEmail;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.contract.openApi.CustomerApiMapping;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;

@Path("/customers")
public final class CustomerController implements CustomerApiMapping {

    private final CreateCustomer createCustomer;
    private final UpdateCustomerEmail updateCustomerEmail;
    private final SearchCustomers searchCustomers;

    public CustomerController(
            CreateCustomer createCustomer, UpdateCustomerEmail updateCustomerEmail, SearchCustomers searchCustomers) {
        this.createCustomer = createCustomer;
        this.updateCustomerEmail = updateCustomerEmail;
        this.searchCustomers = searchCustomers;
    }

    @POST
    public void create(HttpContextRequest request, HttpContextResponse response) throws Exception {
        CreateCustomerDTO inputDto = request.parseBody(CreateCustomerDTO.class);
        CreateCustomerResponseDTO responseDTO = createCustomer.execute(inputDto);

        response.status(CREATED).json(responseDTO);
    }

    @PATCH
    @Path("/{id}/update-email")
    public void updateEmail(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String customerId = request.getRouteParam("id");
        String email = request.parseBody(UpdateCustomerEmailDTO.class).email();

        var inputDto = new UpdateCustomerEmailDTO(customerId, email);
        updateCustomerEmail.execute(inputDto);

        response.status(NO_CONTENT).sendEmpty();
    }

    @GET
    public void search(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        String id = request.getQueryParam("id");
        String name = request.getQueryParam("name");
        String email = request.getQueryParam("email");

        var filterDto = new CustomerFilterDTO(id, name, email, pageRequest.page(), pageRequest.size());
        var outputDto = searchCustomers.execute(filterDto);

        response.status(SUCCESS).json(outputDto);
    }
}
