package by.koronatech;

public interface ArgumentValidator {
    void preValidate(Config config, String value);
    void apply(Config config, String value);
}
