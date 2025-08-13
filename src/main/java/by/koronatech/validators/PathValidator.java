package by.koronatech.validators;

import by.koronatech.ArgumentValidator;
import by.koronatech.Config;

public class PathValidator implements ArgumentValidator {

    public static final String OUTPUT_PATH_DUPLICATE_EXCEPTION_MESSAGE =
            "Допускается указывать путь вывода статистики не более одного раза";

    @Override
    public void preValidate(Config config, String value) {
        if (config.getOutputPath() != null) {
            throw new IllegalArgumentException(OUTPUT_PATH_DUPLICATE_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public void apply(Config config, String value) {
        config.setOutputPath(value);
    }
}
