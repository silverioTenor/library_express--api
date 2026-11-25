package org.libraryexpress.infrastructure.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.user.dto.request.CreateSuperAdminDTO;
import org.libraryexpress.application.user.dto.response.UserCreatedDTO;
import org.libraryexpress.application.user.usecase.CreateSuperAdmin;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("RestrictController")
class RestrictControllerTest {

    @Mock
    private CreateSuperAdmin useCase;

    @Test
    @DisplayName("Should delegate the parsed DTO and answer 201")
    void shouldDelegateDto_whenValidRequest() throws Exception {
        CreateSuperAdminDTO dto =
                new CreateSuperAdminDTO("Library Admin", "admin@example.com", "N0tCommon-LongPassword!2026");
        HttpContextRequest request = mock(HttpContextRequest.class);
        HttpContextResponse response = mock(HttpContextResponse.class);
        when(request.parseBody(CreateSuperAdminDTO.class)).thenReturn(dto);
        when(useCase.execute(dto)).thenReturn(new UserCreatedDTO("1234"));
        when(response.status(HttpStatusCode.CREATED)).thenReturn(response);
        RestrictController controller = new RestrictController(useCase);

        controller.createSuperAdmin(request, response);

        verify(useCase).execute(dto);
        verify(response).status(HttpStatusCode.CREATED);
        verify(response).json(any(UserCreatedDTO.class));
    }

    @Test
    @DisplayName("Should propagate use case exceptions unchanged")
    void shouldPropagateException_whenUseCaseFails() throws Exception {
        CreateSuperAdminDTO dto =
                new CreateSuperAdminDTO("Library Admin", "admin@example.com", "N0tCommon-LongPassword!2026");
        HttpContextRequest request = mock(HttpContextRequest.class);
        HttpContextResponse response = mock(HttpContextResponse.class);
        RuntimeException failure = new RuntimeException("use case failure");
        when(request.parseBody(CreateSuperAdminDTO.class)).thenReturn(dto);
        when(useCase.execute(dto)).thenThrow(failure);
        RestrictController controller = new RestrictController(useCase);

        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> controller.createSuperAdmin(request, response));

        Assertions.assertSame(failure, thrown);
    }
}
