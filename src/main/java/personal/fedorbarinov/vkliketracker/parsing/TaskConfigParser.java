package personal.fedorbarinov.vkliketracker.parsing;

import java.util.Set;
import java.util.TreeSet;

/**
 * Task parser
 */
public class TaskConfigParser extends ConfigParser {
    public static final String TARGET_LABEL = "TARGET";
    public static final String POST_LINK_LABEL = "POST_LINK";

    private static Set<String> parametersNames;

    static {
        parametersNames = new TreeSet<>();
        parametersNames.add(TARGET_LABEL);
        parametersNames.add(POST_LINK_LABEL);
    }

    @Override
    protected boolean checkedParametersAppend() {
        for (String parameterName : parametersNames)
            if (!parameters.contains(parameterName))
                return false;
        return true;
    }

    @Override
    protected boolean isValidParameter(String parameter) {
        return parametersNames.contains(parameter);
    }

    @Override
    protected boolean isValidOption(String option) {
        return false;
    }
}
