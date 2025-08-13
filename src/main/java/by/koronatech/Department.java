package by.koronatech;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Department {

    public static final int SCALE_AVERAGE = 2;
    public static final String SORT_NAME = "name";
    public static final String ORDER_DESC = "desc";

    private Manager manager;
    private List<Employee> employees = new ArrayList<>();

    public void sortEmployees(String sortType, String order) {
        Comparator<Employee> comparator = SORT_NAME.equals(sortType)
                ? Comparator.comparing(Employee::getName)
                : Comparator.comparing(Employee::getSalary);

        if (ORDER_DESC.equals(order)) {
            comparator = comparator.reversed();
        }

        employees = employees.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public Stats calculateStats(String departmentName) {
        if (employees.isEmpty()) {
            return new Stats(departmentName, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<BigDecimal> salaries = employees.stream()
                .map(Employee::getSalary)
                .toList();

        BigDecimal min = Collections.min(salaries);
        BigDecimal max = Collections.max(salaries);

        BigDecimal sum = salaries.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avg = sum.divide(BigDecimal.valueOf(salaries.size()), SCALE_AVERAGE, RoundingMode.HALF_UP);

        return new Stats(departmentName, min, max, avg);
    }
}