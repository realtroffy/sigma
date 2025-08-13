package by.koronatech.validators;

import by.koronatech.ArgumentValidator;
import by.koronatech.Config;

public class StatValidator implements ArgumentValidator {

    public static final String STATS_DUPLICATE_EXCEPTION_MESSAGE =
            "Допускается указывать требование вывода статистики не более одного раза";

    @Override
    public void preValidate(Config config, String value) {
        if (config.isGenerateStats()) {
            throw new IllegalArgumentException(STATS_DUPLICATE_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public void apply(Config config, String value) {
        config.setGenerateStats(true);
    }
}
