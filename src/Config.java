import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Config {
    private final List<String> EMAILS = new ArrayList<>();
    private final List<String> PHRASES = new ArrayList<>();
    private final List<String> AVAILABLE_PARAMETERS =
            Arrays.asList("-mode", "-ss", "-u", "-i", "-f", "-e", "-s", "-p", "-h", "-d", "-date", "-n", "-vb", "-vs", "-inc", "-debug", "-bt", "-st");
    private final int EMPTY_PAGE_RETRIES = 5;
    private final String EMAIL_REGEX =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private URL url;
    private long interval = 10L;
    private long finish = 1_000_000L;
    private long siteSize;
    private boolean sound;
    private boolean negation;
    private DayOfWeek day;
    private LocalTime hour;
    private LocalDate date;
    private String preValue;
    private Float thresholdValue;
    private Float actualValue;
    private boolean debug;
    private String prefixIncrementation;
    private String tempPage;
    private Mode mode;


    @Getter
    public enum Mode {
        CHECK_VALUE("Check Value"),
        PHRASES("Phrases"),
        VALUE_BIGGER("Value bigger"),
        VALUE_SMALLER("Value smaller"),
        SITE_BIGGER_THAN("Site bigger than"),
        SITE_SMALLER_THAN("Site smaller than");

        private final String label;

        Mode(String label) {
            this.label = label;
        }
    }
}
