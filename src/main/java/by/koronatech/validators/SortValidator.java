package by.koronatech.validators;

import by.koronatech.ArgumentValidator;
import by.koronatech.Config;

import java.util.Arrays;

public class SortValidator implements ArgumentValidator {

    public static final String SORT_TYPE_DUPLICATE_EXCEPTION_MESSAGE =
            "Допускается указывать тип сортировки не более одного раза";
    public static final String[] VALID_SORT_TYPES = {"name", "salary"};
    public static final String INVALID_SORT_TYPE_EXCEPTION_MESSAGE = "Недопустимый тип сортировки: ";

    @Override
    public void preValidate(Config config, String value) {
        if (config.getSortType() != null) {
            throw new IllegalArgumentException(SORT_TYPE_DUPLICATE_EXCEPTION_MESSAGE);
        }
        if (!Arrays.asList(VALID_SORT_TYPES).contains(value)) {
            throw new IllegalArgumentException(INVALID_SORT_TYPE_EXCEPTION_MESSAGE + value);
        }
    }

    @Override
    public void apply(Config config, String value) {
        config.setSortType(value);
    }
}
