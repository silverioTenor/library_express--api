package org.libraryexpress.infrastructure.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperties;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.libraryexpress.application.customer.dto.request.CreateCustomerDto;
import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDto;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.config.AppContext;

import java.sql.Array;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

@Path("/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
public final class CustomerController {

    private final AppContext context;

    public CustomerController(AppContext context) {
        this.context = context;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a customer",
            requestBody = @RequestBody(content =  @Content(
                    mediaType =  MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CreateCustomerDto.class)
            ))
    )
    @Parameters({
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failure",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            ,
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
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
            requestBody =  @RequestBody(content = @Content())
    )
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.QUERY),
            @Parameter(name = "email", in = ParameterIn.QUERY),
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer found",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No parameter provided",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
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
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update a customer's email",
            requestBody = @RequestBody(content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(
                            type = "object",
                            requiredProperties = {"email"},
                            properties = @StringToClassMapItem(key = "email", value = String.class)
                    )
            ))
    )
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true),
            @Parameter(name = "X-Trace-Id", in = ParameterIn.HEADER)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Email updated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already in use by another customer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
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
    @Operation(summary = "List customers", requestBody =  @RequestBody(content = @Content()))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list of customers",
                    content = @Content(schema = @Schema(implementation = OutputPaginationDto.class) )
            ),
    })
    public void list(HttpContextRequest request, HttpContextResponse response) throws Exception {
        Pagination.PageRequest pageRequest = request.getPageRequest();

        var inputDto = InputPaginationDto.of(pageRequest.page(), pageRequest.size());
        var outputDto = context.getListCustomers().execute(inputDto);

        response.status(SUCCESS).json(outputDto);
    }
}
