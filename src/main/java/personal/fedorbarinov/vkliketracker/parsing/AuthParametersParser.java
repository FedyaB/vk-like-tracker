package personal.fedorbarinov.vkliketracker.parsing;

import java.util.Set;
import java.util.TreeSet;

/**
 * Authorization config parser
 */
public class AuthParametersParser extends ParametersParser {
    private static Set<String> parametersNames;
    static {
        //Adding all of the allowed parameters there
        parametersNames = new TreeSet<>();
        parametersNames.add("APP_ID");
        parametersNames.add("APP_SECRET");
        parametersNames.add("REDIRECT_URI");
        parametersNames.add("PERMISSIONS");
        parametersNames.add("API_VERSION");
    }

    @Override
    protected boolean isValidParameter(String parameter) {
        return parametersNames.contains(parameter);
    }

    @Override
    protected boolean isMapFilled() {
        return parametersNames.size() == parameters.size();
    }
}
