package org.libraryexpress.domain.book.valueobject;

import java.util.Arrays;
import java.util.List;

import static java.util.function.Predicate.not;

public record Category(String name) {

    public static List<Category> parseMany(String strCategory) {
        return (strCategory == null || strCategory.isBlank())
                ? List.of()
                : Arrays.stream(strCategory.split(","))
                        .map(String::trim)
                        .filter(not(String::isBlank))
                        .map(Category::new)
                        .toList();
    }

    public static List<Category> parseMany(String[] categoryArray) {
        return (categoryArray == null || categoryArray.length == 0)
                ? List.of()
                : Arrays.stream(categoryArray)
                .map(String::trim)
                .filter(not(String::isBlank))
                .map(Category::new)
                .toList();
    }
}
