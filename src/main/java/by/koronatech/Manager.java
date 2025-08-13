package by.koronatech;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Manager {
    private int id;
    private String name;
    private BigDecimal salary;
    private String department;

    @Override
    public String toString() {
        return String.format("Manager,%d,%s,%.2f", id, name, salary);
    }
}
