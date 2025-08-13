package by.koronatech;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
    private String sortType;
    private String order;
    private boolean generateStats;
    private String outputType;
    private String outputPath;
}
