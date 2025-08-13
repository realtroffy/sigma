package by.koronatech.validators;

import by.koronatech.ArgumentValidator;
import by.koronatech.Config;

import java.util.Arrays;

public class OrderValidator implements ArgumentValidator {

    public static final String ORDER_TYPE_DUPLICATE_EXCEPTION_MESSAGE =
            "Допускается указывать тип порядок сортировки не более одного раза";
    public static final String[] VALID_ORDERS = {"asc", "desc"};
    public static final String INVALID_ORDER = "Недопустимый порядок сортировки: ";

    @Override
    public void preValidate(Config config, String value) {
        if (config.getOrder() != null) {
            throw new IllegalArgumentException(ORDER_TYPE_DUPLICATE_EXCEPTION_MESSAGE);
        }

        if (!Arrays.asList(VALID_ORDERS).contains(value)) {
            throw new IllegalArgumentException(INVALID_ORDER + value);
        }
    }

    @Override
    public void apply(Config config, String value) {
        config.setOrder(value);
    }
}
