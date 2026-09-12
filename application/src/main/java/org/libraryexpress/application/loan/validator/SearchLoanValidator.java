package org.libraryexpress.application.loan.validator;

import org.libraryexpress.application.loan.dto.request.FilterLoansDto;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;

public class SearchLoanValidator {

    private static final CustomLogger logger =  CustomLoggerFactory.getLogger(SearchLoanValidator.class);

    public void validate(FilterLoansDto filter) {

        boolean hasAnyCriteria = filter.statuses() != null
                || (filter.customerId() != null && !filter.customerId().isBlank())
                || (filter.ISBN() != null && !filter.ISBN().isBlank());

        if (!hasAnyCriteria) {
            logger.warn("SEARCH LOAN VALIDATOR: Filter has no any criteria. Filter: [{}]", filter);
            throw new IllegalArgumentException("At least one search criteria must be provided");
        }
    }
}
