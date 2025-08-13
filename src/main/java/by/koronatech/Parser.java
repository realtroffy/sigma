package by.koronatech;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parser {

    public static final int EXPECTED_PARTS_COUNT = 5;
    public static final String CSV_DELIMITER = ",";
    public static final String TEMP_DEPT_PREFIX = "TEMP_";
    public static final String TYPE_MANAGER = "Manager";
    public static final String TYPE_EMPLOYEE = "Employee";
    public static final String FILE_READ_ERROR = "Ошибка чтения файла ";

    public Map<Employee, String> parseFile(
            Path file,
            Map<String, Department> departments,
            Set<String> errors,
            Set<Integer> usedIds) {

        Map<Employee, String> employeeLines = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line.trim(), departments, errors, employeeLines, usedIds);
            }
        } catch (IOException e) {
            errors.add(FILE_READ_ERROR + file + ": " + e.getMessage());
        }

        return employeeLines;
    }

    private void parseLine(
            String line,
            Map<String, Department> departments,
            Set<String> errors,
            Map<Employee, String> employeeLines,
            Set<Integer> usedIds) {

        if (line.isEmpty()) return;

        String[] parts = line.split(CSV_DELIMITER);
        if (parts.length != EXPECTED_PARTS_COUNT) {
            errors.add(line);
            return;
        }

        String type = parts[0].trim();
        String idStr = parts[1].trim();
        String name = parts[2].trim();
        String salaryStr = parts[3].trim();
        String deptOrManagerId = parts[4].trim();

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            errors.add(line);
            return;
        }

        if (!usedIds.add(id)) {
            errors.add(line);
            return;
        }

        if (TYPE_MANAGER.equals(type)) {
            handleManager(id, name, salaryStr, deptOrManagerId, departments, errors, line);
        } else if (TYPE_EMPLOYEE.equals(type)) {
            handleEmployee(id, name, salaryStr, deptOrManagerId, departments, errors, employeeLines, line);
        } else {
            errors.add(line);
        }
    }

    private void handleManager(
            int id,
            String name,
            String salaryStr,
            String departmentId,
            Map<String, Department> departments,
            Set<String> errors,
            String originalLine) {

        BigDecimal salary = parseSalary(salaryStr, errors, originalLine);
        if (salary == null) {
            return;
        }

        Department dept = departments.computeIfAbsent(departmentId, k -> new Department());
        if (dept.getManager() != null) {
            errors.add(originalLine);
            return;
        }

        Manager manager = new Manager(id, name, salary, departmentId);
        dept.setManager(manager);
    }

    private void handleEmployee(
            int id,
            String name,
            String salaryStr,
            String managerIdStr,
            Map<String, Department> departments,
            Set<String> errors,
            Map<Employee, String> employeeLines,
            String originalLine) {

        int managerId;
        try {
            managerId = Integer.parseInt(managerIdStr);
        } catch (NumberFormatException e) {
            errors.add(originalLine);
            return;
        }

        BigDecimal salary = parseSalary(salaryStr, errors, originalLine);
        if (salary == null) {
            return;
        }

        Employee employee = new Employee(id, name, salary, managerId);
        Department tempDept = departments.computeIfAbsent(TEMP_DEPT_PREFIX + managerId, k -> new Department());
        tempDept.getEmployees().add(employee);
        employeeLines.put(employee, originalLine);
    }

    private BigDecimal parseSalary(String salaryStr, Set<String> errors, String originalLine) {
        try {
            BigDecimal salary = new BigDecimal(salaryStr);
            if (salary.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(originalLine);
                return null;
            }
            return salary;
        } catch (NumberFormatException e) {
            errors.add(originalLine);
            return null;
        }
    }
}
