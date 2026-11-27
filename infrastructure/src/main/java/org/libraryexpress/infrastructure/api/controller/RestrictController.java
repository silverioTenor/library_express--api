package org.libraryexpress.infrastructure.api.controller;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.CREATED;

import org.libraryexpress.application.user.dto.request.CreateSuperAdminDTO;
import org.libraryexpress.application.user.dto.response.UserCreatedDTO;
import org.libraryexpress.application.user.usecase.CreateSuperAdmin;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;

public class RestrictController {

    private final CreateSuperAdmin createSuperAdmin;

    public RestrictController(CreateSuperAdmin createSuperAdmin) {
        this.createSuperAdmin = createSuperAdmin;
    }

    public void createSuperAdmin(HttpContextRequest request, HttpContextResponse response) throws Exception {
        CreateSuperAdminDTO inputDto = request.parseBody(CreateSuperAdminDTO.class);
        UserCreatedDTO outputDto = createSuperAdmin.execute(inputDto);

        response.status(CREATED).json(outputDto);
    }
}
