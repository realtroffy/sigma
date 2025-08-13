package by.koronatech;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Stats {
    private String department;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private BigDecimal averageSalary;
}
