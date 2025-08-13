package by.koronatech;

import by.koronatech.validators.OrderValidator;
import by.koronatech.validators.OutputValidator;
import by.koronatech.validators.PathValidator;
import by.koronatech.validators.SortValidator;
import by.koronatech.validators.StatValidator;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class App {

    public static final String INPUT_EXTENSION = ".sb";
    public static final String ERROR_LOG = "error.log";
    public static final String TEMP_DEPT_PREFIX = "TEMP_";
    public static final int TEMP_PREFIX_LENGTH = 5;
    public static final String ERROR_PREFIX = "Ошибка: ";
    public static final String UNEXPECTED_ERROR = "Неожиданная ошибка: ";
    public static final Path OUTPUT_DIR = Path.of("output");
    public static final String UNKNOWN_PARAMETER = "Неизвестный параметр: ";
    public static final String ORDER_REQUIRED = "Порядок сортировки должен быть указан при использовании сортировки";
    public static final String SORT_TYPE_REQUIRED = "Порядок сортировки не может быть указан без типа сортировки";
    public static final String PATH_WITHOUT_FILE = "Путь не может быть указан без output=file";
    public static final String PATH_REQUIRED_FOR_FILE = "Путь должен быть указан при output=file";
    public static final String OUTPUT_WITHOUT_STAT = "Параметры output или path не могут быть указаны без stat";
    public static final Path CURRENT_DIRECTORY = Path.of(".");
    public static final String INPUT_FILE_PATTERN = "*" + INPUT_EXTENSION;
    public static final String FILE_OUTPUT = "file";
    public static final String SORT_FLAG_LONG = "--sort=";
    public static final String SORT_FLAG_SHORT = "-s=";
    public static final String ORDER_FLAG = "--order=";
    public static final String STAT_FLAG = "--stat";
    public static final String OUTPUT_FLAG_LONG = "--output=";
    public static final String OUTPUT_FLAG_SHORT = "-o=";
    public static final String PATH_FLAG = "--path=";
    public static final String ARG_DELIMITER = "=";
    public static final int SPLIT_LIMIT = 2;
    public static final int VALUE_INDEX = 1;
    public static final int ERROR_EXIT_CODE = 1;

    private final Path directoryForFindFiles;
    private final Map<String, ArgumentValidator> argValidators;
    private final Writer writer;
    private final Parser parser;


    public static void main(String[] args) {
        Map<String, ArgumentValidator> argValidators = Map.of(
                SORT_FLAG_LONG, new SortValidator(),
                SORT_FLAG_SHORT, new SortValidator(),
                ORDER_FLAG, new OrderValidator(),
                STAT_FLAG, new StatValidator(),
                OUTPUT_FLAG_LONG, new OutputValidator(),
                OUTPUT_FLAG_SHORT, new OutputValidator(),
                PATH_FLAG, new PathValidator());

        App app = new App(
                CURRENT_DIRECTORY,
                argValidators,
                new Writer(OUTPUT_DIR, ERROR_LOG, INPUT_EXTENSION),
                new Parser());
        app.start(args);
    }

    public void start(String[] args) {
        try {
            Config config = parseArguments(args);
            processFiles(config);
        } catch (IllegalArgumentException e) {
            System.err.println(ERROR_PREFIX + e.getMessage());
            System.exit(ERROR_EXIT_CODE);
        } catch (Exception e) {
            System.err.println(UNEXPECTED_ERROR + e.getMessage());
            System.exit(ERROR_EXIT_CODE);
        }
    }

    private Config parseArguments(String[] args) {
        Config config = new Config();

        for (String arg : args) {
            String key = arg.contains(ARG_DELIMITER) ? arg.substring(0, arg.indexOf(ARG_DELIMITER) + 1) : arg;
            String value = arg.contains(ARG_DELIMITER) ? arg.split(ARG_DELIMITER, SPLIT_LIMIT)[VALUE_INDEX] : null;
            ArgumentValidator validator = argValidators.get(key);
            if (validator != null) {
                validator.preValidate(config, value);
                validator.apply(config, value);
            } else {
                throw new IllegalArgumentException(UNKNOWN_PARAMETER + arg);
            }
        }
        postValidate(config);
        return config;
    }

    private void processFiles(Config config) throws IOException {
        List<Path> inputFiles = loadInputFiles();
        Map<String, Department> departments = new HashMap<>();
        Set<String> errors = new HashSet<>();
        Map<Employee, String> employeeLines = new HashMap<>();
        Set<Integer> usedIds = new HashSet<>();

        for (Path file : inputFiles) {
            Map<Employee, String> fileEmployeeLines = parser.parseFile(file, departments, errors, usedIds);
            employeeLines.putAll(fileEmployeeLines);
        }

        mergeTempDepartments(departments, errors, employeeLines);

        writer.prepareOutputDirectory();
        writer.writeErrors(errors);

        if (config.getSortType() != null) {
            for (Department dept : departments.values()) {
                dept.sortEmployees(config.getSortType(), config.getOrder());
            }
        }

        writer.writeDepartmentFiles(departments, errors);

        if (config.isGenerateStats()) {
            writer.writeStatistics(departments, config);
        }
    }

    private void postValidate(Config config) {
        if (config.getSortType() != null && config.getOrder() == null) {
            throw new IllegalArgumentException(ORDER_REQUIRED);
        }
        if (config.getOrder() != null && config.getSortType() == null) {
            throw new IllegalArgumentException(SORT_TYPE_REQUIRED);
        }
        if (config.getOutputPath() != null && !FILE_OUTPUT.equals(config.getOutputType())) {
            throw new IllegalArgumentException(PATH_WITHOUT_FILE);
        }
        if (FILE_OUTPUT.equals(config.getOutputType()) && config.getOutputPath() == null) {
            throw new IllegalArgumentException(PATH_REQUIRED_FOR_FILE);
        }
        if (!config.isGenerateStats() && config.getOutputType() != null) {
            throw new IllegalArgumentException(OUTPUT_WITHOUT_STAT);
        }
    }

    private List<Path> loadInputFiles() throws IOException {
        List<Path> inputFiles = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directoryForFindFiles, INPUT_FILE_PATTERN)) {
            for (Path path : dirStream) {
                if (Files.isRegularFile(path)) {
                    inputFiles.add(path);
                }
            }
        }
        return inputFiles;
    }

    private void mergeTempDepartments(Map<String, Department> departments,
                                      Set<String> errors,
                                      Map<Employee, String> employeeLines) {
        for (Iterator<Map.Entry<String, Department>> it = departments.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Department> entry = it.next();
            String key = entry.getKey();
            Department dept = entry.getValue();
            if (key.startsWith(TEMP_DEPT_PREFIX)) {
                int managerId = Integer.parseInt(key.substring(TEMP_PREFIX_LENGTH));
                String targetDeptName = findTargetDepartment(departments, managerId);
                if (targetDeptName != null) {
                    Department targetDept = departments.get(targetDeptName);
                    for (Employee emp : dept.getEmployees()) {
                        String line = employeeLines.get(emp);
                        if (line != null) {
                            errors.remove(line);
                        }
                    }
                    targetDept.getEmployees().addAll(dept.getEmployees());
                }
                it.remove();
            }
        }
    }

    private String findTargetDepartment(Map<String, Department> departments, int managerId) {
        for (Map.Entry<String, Department> deptEntry : departments.entrySet()) {
            if (!deptEntry.getKey().startsWith(TEMP_DEPT_PREFIX)
                    && deptEntry.getValue().getManager() != null
                    && deptEntry.getValue().getManager().getId() == managerId) {
                return deptEntry.getKey();
            }
        }
        return null;
    }
}