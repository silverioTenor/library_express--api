package org.libraryexpress.domain.book.valueobject;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;

public record Isbn(String value) {

    public Isbn {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("ISBN cannot be null or empty.");
        }

        // Removes hyphens and whitespaces
        String isbnCleaned = value.replaceAll("[\\s-]", "").toUpperCase();

        // If it is already an ISBN-13, validates the basic length and returns it
        if (isbnCleaned.length() == 13 && isbnCleaned.matches("\\d{13}")) {
            value = isbnCleaned;
        } else {
            validateIsbn10(isbnCleaned);
            value = convertToIsbn13(isbnCleaned);
        }
    }

    /**
     * Converts an ISBN-10 to the ISBN-13 standard.
     * If the input is already a valid ISBN-13, returns the cleaned input itself.
     *
     * @param isbn The ISBN code (with or without hyphens/spaces)
     * @return The formatted ISBN-13 code containing only numbers
     * @throws InvalidArgumentException if the format is invalid
     */
    private static String convertToIsbn13(String isbn) {
        // 1. Takes the first 9 digits of the ISBN-10 and prepends the 978 prefix
        String isbn13Base = "978" + isbn.substring(0, 9);

        // 2. Calculates the new check digit for the ISBN-13
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn13Base.charAt(i));
            // Multiplies by 1 for odd positions (even index) and by 3 for even positions (odd index)
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        int remainder = sum % 10;
        int checkDigit = (10 - remainder) % 10;

        // 3. Assembles the final ISBN-13
        return isbn13Base + checkDigit;
    }

    private static void validateIsbn10(String isbn) {
        // Validates if it matches the basic ISBN-10 format
        if (isbn.length() != 10 || !isbn.matches("\\d{9}[\\dX]")) {
            throw new InvalidArgumentException("Invalid ISBN format provided: " + isbn);
        }

        // Strict Validation of the Original Check Digit (Modulo 11)
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(isbn.charAt(i)) * (10 - i);
        }

        // Processes the last character (can be a number or 'X')
        char lastChar = isbn.charAt(9);
        sum += (lastChar == 'X') ? 10 : Character.getNumericValue(lastChar);

        // If the remainder of the division by 11 is not zero, the ISBN-10 is false/invalid.
        if (sum % 11 != 0) {
            throw new IllegalArgumentException("Dígito verificador do ISBN-10 original é inválido.");
        }
    }
}
