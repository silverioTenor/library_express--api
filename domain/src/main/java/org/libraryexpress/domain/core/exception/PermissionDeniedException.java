package org.libraryexpress.domain.core.exception;

import static org.libraryexpress.domain.core.exception.DomainErrorType.PERMISSION_DENIED;

public final class PermissionDeniedException extends DomainException {

    private static final String MESSAGE = "Permission denied.";

    public PermissionDeniedException() {
        super(PERMISSION_DENIED, MESSAGE);
    }
}
