package personal.fedorbarinov.vkliketracker.parsing;

import java.util.Set;
import java.util.TreeSet;

/**
 * Authorization config parser
 */
public class AuthConfigParser extends ConfigParser {
    public static final String APP_ID_LABEL = "APP_ID";
    public static final String APP_SECRET_LABEL = "APP_SECRET";
    public static final String REDIRECT_LABEL = "REDIRECT_URI";
    public static final String API_VERSION_LABEL = "API_VERSION";
    public static final String PERMISSIONS_LABEL = "PERMISSIONS";
    public static final String CACHE_PATH_LABEL = "CACHE_PATH";
    public static final String NO_CACHING_LABEL = "NO_TOKEN_CACHING";

    private static final String DEFAULT_CACHE_PATH = "auth.cache";

    private static Set<String> parametersNames;
    private static Set<String> optionsNames;

    static {
        //Adding all of the allowed parameters there
        parametersNames = new TreeSet<>();
        parametersNames.add(APP_ID_LABEL);
        parametersNames.add(APP_SECRET_LABEL);
        parametersNames.add(REDIRECT_LABEL);
        parametersNames.add(PERMISSIONS_LABEL);
        parametersNames.add(API_VERSION_LABEL);
        parametersNames.add(CACHE_PATH_LABEL);
        //Adding all of the allowed options there
        optionsNames = new TreeSet<>();
        optionsNames.add(NO_CACHING_LABEL);
    }

    @Override
    protected boolean isValidParameter(String parameter) {
        return parametersNames.contains(parameter);
    }

    @Override
    protected boolean isValidOption(String option) {
        return optionsNames.contains(option);
    }

    @Override
    protected boolean checkedParametersAppend() {
        if (!parameters.containsKey(CACHE_PATH_LABEL))
            parameters.put(CACHE_PATH_LABEL, DEFAULT_CACHE_PATH);

        //Check that every parameter is present
        for (String parameterName : parametersNames)
            if (!parameters.containsKey(parameterName))
                return false;
        return true;
    }
}
