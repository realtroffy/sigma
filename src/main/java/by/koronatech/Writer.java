package by.koronatech;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Writer {

    public static final String MANAGER_FORMAT = "Manager,%d,%s,%s";
    public static final String EMPLOYEE_FORMAT = "Employee,%d,%s,%s,%d";
    public static final String STAT_HEADER = "department,min,max,mid\n";
    public static final String STAT_FORMAT = "%s,%s,%s,%s\n";
    public static final String MISSING_MANAGER_ERROR = "Не найден менеджер для департамента: ";
    public static final String FILE_OUTPUT_TYPE = "file";
    public static final String DECIMAL_PATTERN = "0.00";
    public static final char DECIMAL_SEPARATOR = '.';
    public static final int ZERO_SCALE = 0;

    private final Path outputDir;
    private final String errorLogFile;
    private final String inputExtension;
    private final DecimalFormat salaryFormat;

    public Writer(Path outputDir, String errorLogFile, String inputExtension) {
        this.outputDir = outputDir;
        this.errorLogFile = errorLogFile;
        this.inputExtension = inputExtension;
        this.salaryFormat = createDecimalFormat();
    }

    private DecimalFormat createDecimalFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(DECIMAL_SEPARATOR);
        return new DecimalFormat(DECIMAL_PATTERN, symbols);
    }

    private String formatSalary(BigDecimal salary) {
        if (salary.scale() <= ZERO_SCALE || salary.stripTrailingZeros().scale() <= ZERO_SCALE) {
            return salary.toBigInteger().toString();
        }
        return salaryFormat.format(salary);
    }

    public void prepareOutputDirectory() throws IOException {
        if (Files.exists(outputDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDir)) {
                for (Path file : stream) {
                    Files.delete(file);
                }
            }
        } else {
            Files.createDirectories(outputDir);
        }
    }

    public void writeErrors(Set<String> errors) throws IOException {
        if (!errors.isEmpty()) {
            Path errorLogPath = outputDir.resolve(errorLogFile);
            try (BufferedWriter writer = Files.newBufferedWriter(errorLogPath)) {
                for (String error : errors) {
                    writer.write(error);
                    writer.newLine();
                }
            }
        }
    }

    public void writeDepartmentFiles(Map<String, Department> departments, Set<String> errors) throws IOException {
        for (Map.Entry<String, Department> entry : departments.entrySet()) {
            String deptName = entry.getKey();
            Department dept = entry.getValue();

            if (dept.getManager() == null) {
                errors.add(MISSING_MANAGER_ERROR + deptName);
                continue;
            }

            Path deptFile = outputDir.resolve(deptName + inputExtension);
            try (BufferedWriter writer = Files.newBufferedWriter(deptFile)) {
                Manager manager = dept.getManager();
                writer.write(String.format(MANAGER_FORMAT,
                        manager.getId(), manager.getName(), formatSalary(manager.getSalary())));
                writer.newLine();
                for (Employee emp : dept.getEmployees()) {
                    writer.write(String.format(EMPLOYEE_FORMAT,
                            emp.getId(), emp.getName(), formatSalary(emp.getSalary()), emp.getManagerId()));
                    writer.newLine();
                }
            }
        }
    }

    public void writeStatistics(Map<String, Department> departments, Config config) throws IOException {
        List<Stats> stats = departments.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().calculateStats(entry.getKey()))
                .toList();

        StringBuilder statsOutput = new StringBuilder(STAT_HEADER);
        for (Stats stat : stats) {
            statsOutput.append(String.format(STAT_FORMAT,
                    stat.getDepartment(),
                    salaryFormat.format(stat.getMinSalary()),
                    salaryFormat.format(stat.getMaxSalary()),
                    salaryFormat.format(stat.getAverageSalary())));
        }

        if (FILE_OUTPUT_TYPE.equals(config.getOutputType())) {
            Path outputPath = outputDir.resolve(config.getOutputPath());
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write(statsOutput.toString());
            }
        } else {
            System.out.print(statsOutput);
        }
    }
}
