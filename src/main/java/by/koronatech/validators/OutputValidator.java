package by.koronatech.validators;

import by.koronatech.ArgumentValidator;
import by.koronatech.Config;

import java.util.Arrays;

public class OutputValidator implements ArgumentValidator {

    public static final String OUTPUT_TYPE_DUPLICATE_EXCEPTION_MESSAGE =
            "Допускается указывать тип вывода статистики не более одного раза";
    public static final String[] VALID_OUTPUT_TYPES = {"console", "file"};
    public static final String INVALID_OUTPUT_TYPE = "Недопустимый тип вывода: ";

    @Override
    public void preValidate(Config config, String value) {
        if (config.getOutputType() != null) {
            throw new IllegalArgumentException(OUTPUT_TYPE_DUPLICATE_EXCEPTION_MESSAGE);
        }
        if (!Arrays.asList(VALID_OUTPUT_TYPES).contains(value)) {
            throw new IllegalArgumentException(INVALID_OUTPUT_TYPE + value);
        }
    }

    @Override
    public void apply(Config config, String value) {
        config.setOutputType(value);
    }
}
