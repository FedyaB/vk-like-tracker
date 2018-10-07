package personal.fedorbarinov.vkliketracker;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton logger class
 */
public class Logger {
    private static final String PREFIX_ERROR = "ERROR:"; //Prefix in case of error
    private static final String PREFIX_INFO = "INFO:"; //Prefix in case of info message
    private static final String PREFIX_WARNING = "WARNING:"; //Prefix in case of warning
    private static final String SEPARATOR_WORD = " ";
    private static final String SEPARATOR_LINE = "\n";
    private static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static final int MODE_TIME = 0x1; //Modes are combined into a bit mask

    /**
     * The kind of a log message
     */
    public enum LogKind {
        ERROR,
        INFO,
        WARNING
    }

    private static Logger instance; //Singleton class instance
    private static DateTimeFormatter formatter;

    static {
        instance = new Logger();
        formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
    }

    /**
     * Get the instance of Logger class
     * @return The instance
     */
    public static Logger getInstance() { return instance; }

    private PrintStream outInfo; //Info messages output stream
    private PrintStream outError; //Error output stream
    private PrintStream outWarning; //Warning output stream

    private boolean enabledTime;

    private Logger() {
        this.outInfo = System.out;
        this.outError = System.err;
        this.enabledTime = true;
    }

    /**
     * Set current logger mode
     * @param mode A bit mask, built of what should be included in a log message
     */
    public void setMode(int mode) {
        enabledTime = (mode & MODE_TIME) != 0;
    }

    /**
     * Set output stream for a specific kind of messages
     * @param kind Kind of messages
     * @param out Output stream to be set
     */
    public void setStream(LogKind kind, PrintStream out) {
        switch(kind) {
            case ERROR:
                this.outError = out;
                break;
            case INFO:
                this.outInfo = out;
                break;
            case WARNING:
                this.outWarning = out;
                break;
        }
    }

    /**
     * Write log message
     * @param kind Kind of message
     * @param message Message body
     */
    public void log(LogKind kind, String message) {
        switch(kind) {
            case ERROR:
                this.outError.print(buildLogMessage(PREFIX_ERROR, message));
                break;
            case INFO:
                this.outInfo.print(buildLogMessage(PREFIX_INFO, message));
                break;
            case WARNING:
                this.outWarning.print(buildLogMessage(PREFIX_WARNING, message));
                break;
        }
    }

    /**
     * Build log message, considering additional info
     * @param prefix Prefix that represents the kind of message
     * @param message Message body
     * @return Built log message
     */
    private String buildLogMessage(String prefix, String message) {
        StringBuilder stringBuilder = new StringBuilder(prefix);
        stringBuilder.append(SEPARATOR_WORD);
        if (enabledTime)
            stringBuilder.append(LocalDateTime.now().format(formatter)).append(SEPARATOR_WORD);
        stringBuilder.append(message).append(SEPARATOR_LINE);
        return stringBuilder.toString();
    }
}
