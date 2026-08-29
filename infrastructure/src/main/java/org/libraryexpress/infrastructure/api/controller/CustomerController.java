package org.libraryexpress.infrastructure.api.controller;

import org.libraryexpress.application.customer.dto.request.CreateCustomerDto;
import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDto;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

public final class CustomerController {

    private final AppContext context;

    public CustomerController(AppContext context) {
        this.context = context;
    }

    public void create(HttpContextRequest request, HttpContextResponse response) throws Exception {
        CreateCustomerDto inputDto = request.parseBody(CreateCustomerDto.class);
        context.getCreateCustomer().execute(inputDto);

        response.status(CREATED).sendEmpty();
    }

    public void get(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String customerId = request.getQueryParam("id");
        String email = request.getQueryParam("email");

        String criteria = (customerId != null && !customerId.isBlank())
                ? customerId
                : email;

        CustomerDto outputDto = context.getFindCustomer().execute(criteria);

        response.status(SUCCESS).json(outputDto);
    }

    public void updateEmail(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String customerId = request.getRouteParam("id");
        String email = request.parseBody(UpdateCustomerEmailDto.class).email();

        var inputDto = new UpdateCustomerEmailDto(customerId, email);
        context.getUpdateCustomerEmail().execute(inputDto);

        response.status(NO_CONTENT).sendEmpty();
    }

    public void list(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        var inputDto = InputPaginationDto.of(pageRequest.page(), pageRequest.size());
        var outputDto = context.getListCustomers().execute(inputDto);

        response.status(SUCCESS).json(outputDto);
    }
}
