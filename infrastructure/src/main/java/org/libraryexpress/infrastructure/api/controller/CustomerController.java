package org.libraryexpress.infrastructure.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.libraryexpress.application.customer.dto.request.CreateCustomerDto;
import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDto;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

@Path("/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
public final class CustomerController {

    private final AppContext context;

    public CustomerController(AppContext context) {
        this.context = context;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created"),
            @ApiResponse(responseCode = "400", description = "Validation failure"),
            @ApiResponse(responseCode = "409", description = "Duplicate email")
    })
    public void create(HttpContextRequest request, HttpContextResponse response) throws Exception {
        CreateCustomerDto inputDto = request.parseBody(CreateCustomerDto.class);
        context.getCreateCustomer().execute(inputDto);

        response.status(CREATED).sendEmpty();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Find a customer by id or email",
            description = "Looks up a single customer using either the 'id' or 'email' query parameter; 'id' takes precedence when both are present. Distinct from the paginated listing at GET /customers."
    )
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.QUERY, description = "Customer id", required = false),
            @Parameter(name = "email", in = ParameterIn.QUERY, description = "Customer email", required = false)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400", description = "Neither id nor email provided"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public void get(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String customerId = request.getQueryParam("id");
        String email = request.getQueryParam("email");

        String criteria = (customerId != null && !customerId.isBlank())
                ? customerId
                : email;

        CustomerDto outputDto = context.getFindCustomer().execute(criteria);

        response.status(SUCCESS).json(outputDto);
    }

    @PATCH
    @Path("/{id}/update-email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a customer's email")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Customer id", required = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email updated"),
            @ApiResponse(responseCode = "400", description = "Invalid email format"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Email already in use by another customer")
    })
    public void updateEmail(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String customerId = request.getRouteParam("id");
        String email = request.parseBody(UpdateCustomerEmailDto.class).email();

        var inputDto = new UpdateCustomerEmailDto(customerId, email);
        context.getUpdateCustomerEmail().execute(inputDto);

        response.status(NO_CONTENT).sendEmpty();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List customers (paginated)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of customers",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public void list(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        var inputDto = InputPaginationDto.of(pageRequest.page(), pageRequest.size());
        var outputDto = context.getListCustomers().execute(inputDto);

        response.status(SUCCESS).json(outputDto);
    }
}
