package by.koronatech;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Employee {
    private int id;
    private String name;
    private BigDecimal salary;
    private int managerId;

    @Override
    public String toString() {
        return String.format("Employee,%d,%s,%.2f,%d", id, name, salary, managerId);
    }
}