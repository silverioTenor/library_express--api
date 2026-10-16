package org.libraryexpress.application.loan.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.loan.dto.request.LoanFilterDTO;
import org.libraryexpress.application.loan.dto.response.LoanDTO;
import org.libraryexpress.application.loan.mapper.LoanMapper;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchLoans UseCase - Unit Test")
class SearchLoansTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper mapper;

    @InjectMocks
    private SearchLoans searchLoans;

    private static final String CUSTOMER_ID = "customer-uuid-123";
    private static final String ISBN_STR = "978-43-12345-67-8";

    @Test
    @DisplayName("Should successfully return mapped DTOs when valid criteria are provided")
    void shouldReturnLoanDtos_whenFilterCriteriaAreValid() {
        // Arrange
        Set<LoanStatus> statuses = Set.of(LoanStatus.ACTIVE);
        var validFilter = new LoanFilterDTO(null, CUSTOMER_ID, ISBN_STR, "active", 0, 0);

        Loan mockLoan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(ISBN_STR)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .build();

        LoanDTO expectedDto = new LoanDTO("loan-1", CUSTOMER_ID, ISBN_STR, LoanStatus.ACTIVE, LocalDate.now(), LocalDate.now().plusDays(15));

        // Stubbing repository query and mapper conversion layers
        when(loanRepository.search(validFilter.toLoanFilter())).thenReturn(new QueryResult<>(List.of(mockLoan), 1));
        when(mapper.toResponseDto(mockLoan)).thenReturn(expectedDto);

        // Act
        OutputPaginationDto<LoanDTO> results = searchLoans.execute(validFilter);

        // Assert
        assertNotNull(results.items());
        assertEquals(1, results.total(), "Result set should contain exactly 1 mapped DTO");
        assertTrue(results.items().contains(expectedDto));
    }
}
