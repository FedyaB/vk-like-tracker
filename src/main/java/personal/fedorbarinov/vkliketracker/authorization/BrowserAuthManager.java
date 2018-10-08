package personal.fedorbarinov.vkliketracker.authorization;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.OAuthException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.ServiceClientCredentialsFlowResponse;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.secure.TokenChecked;
import personal.fedorbarinov.vkliketracker.Logger;
import personal.fedorbarinov.vkliketracker.parsing.AuthConfigParser;
import personal.fedorbarinov.vkliketracker.parsing.Parser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Authorization using default browser and manual auth code input
 */
public class BrowserAuthManager implements AuthManager {
    //Parameters needed in authorization request
    private static final String AUTH_FORMAT_URI = "https://oauth.vk.com/authorize?client_id=%d&display=%s&redirect_uri=%s&scope=%s&response_type=%s&v=%s";
    private static final String RESPONSE_TYPE = "code";
    private static final String DISPLAY = "page";
    private static final String API_VERSION = "5.85";
    private static final String PERMISSIONS = "messages";

    private static final String INPUT_MESSAGE = "Enter the code parameter from browser:";
    private static final String INPUT_ERROR = "The input code was empty";
    private static final String EXCEPTION_PREFIX = "[Authorization]:"; //Prefix for an exception message

    private static final String LOG_MSG_CACHE_NEW = "Auth token has been cached";
    private static final String LOG_MSG_CACHE_USE = "Authorized with cached token";
    private static final String LOG_MSG_CACHE_WRITE_IO = "Auth cache haven't been created";
    private static final String LOG_MSG_CACHE_READ_FORMAT = "Auth cache bad format";

    /**
     * Auth cache class
     */
    private class Cache {
        Cache(Integer userId, String token) {
            this.userId = userId;
            this.token = token;
        }
        Integer userId;
        String token;
    }

    private Integer appId; //Current VK app id
    private String appSecret; //Secret key of the app
    private String redirectURI; //URI we're being redirected to after passing credentials
    private String apiVersion; //Current VK API version
    private String permissions; //Permissions we need for the app
    private Path cachePath; //Path to a cache file

    private VkApiClient vkClient; //VkApi client instance
    private boolean validation; //Is "need_validation" error is being handled right now?
    private boolean isAuthCacheUsed; //Is caching token allowed?

    /**
     * Public constructor of the class
     * @param parameters Parameters that were obtained from the corresponding config file
     */
    public BrowserAuthManager(Parser.ParsingResult parameters) {
        this.appId = Integer.parseInt(parameters.get(AuthConfigParser.APP_ID_LABEL));
        this.appSecret = parameters.get(AuthConfigParser.APP_SECRET_LABEL);
        this.redirectURI = parameters.get(AuthConfigParser.REDIRECT_LABEL);
        this.apiVersion = API_VERSION;
        this.permissions = PERMISSIONS;
        this.cachePath = Paths.get(parameters.get(AuthConfigParser.CACHE_PATH_LABEL));
        this.vkClient = new VkApiClient(HttpTransportClient.getInstance());
        this.validation = false;
        this.isAuthCacheUsed = parameters.contains(AuthConfigParser.USE_CACHED_TOKEN_LABEL);
    }

    @Override
    public UserActor authorize() throws AuthException {
        try {
            if (isAuthCacheUsed && !validation) { //If we're not in "need_validation" state and caching is on
                Cache cache = readCachedValue(); //Try to use cached token
                if (cache != null && isCacheValid(cache)) {
                    Logger.getInstance().log(Logger.LogKind.INFO, LOG_MSG_CACHE_USE);
                    return new UserActor(cache.userId, cache.token);
                }
            }
            UserAuthResponse authResponse = performAuthorization(); //Try to get authorization response
            if (isAuthCacheUsed) {
                writeCachedValue(new Cache(authResponse.getUserId(), authResponse.getAccessToken()));
                Logger.getInstance().log(Logger.LogKind.INFO, LOG_MSG_CACHE_NEW);
            }
            return new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
        } catch (OAuthException e) { //Case of additional validation necessity
            if (validation) //If the app is stuck on validation more than once -- that's fatal
                throw new AuthException(buildErrorMessage(e.getLocalizedMessage()));
            redirectURI = e.getRedirectUri();
            validation = true;
            UserActor userActor = authorize(); //Another authorization try with a different redirect URI
            validation = false;
            return userActor;
        }
    }

    /**
     * Perform user authorization
     * @return Authorization response
     * @throws OAuthException A sign that the app needs to handle additional validation
     * @throws AuthException Exception that is thrown during authorization
     */
    private UserAuthResponse performAuthorization() throws OAuthException, AuthException {
        String code = obtainCode(); //Obtain 'code' parameter
        try {
            return vkClient.oauth()
                    .userAuthorizationCodeFlow(appId, appSecret, redirectURI, code)
                    .execute();
        }  catch (OAuthException e) {
            throw e; //Lift the exception up the call stack
        }catch (ApiException | ClientException e) {
            throw new AuthException(buildErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * Obtain 'code' parameter during authorization to VK
     * @return Code parameter
     * @throws AuthException Exception that is thrown during authorization
     */
    private String obtainCode() throws AuthException {
        String authURI = String.format(AUTH_FORMAT_URI, appId, DISPLAY, redirectURI, permissions, RESPONSE_TYPE, apiVersion);
        try {
            Desktop.getDesktop().browse(new URI(authURI)); //Emit URI processing in the default browser
            String input = JOptionPane.showInputDialog(INPUT_MESSAGE); //The dialog in which user should print code parameter
            if (input == null)
                throw new AuthException(INPUT_ERROR);
            return input;
        } catch (URISyntaxException | IOException e) {
            throw new AuthException(buildErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * Check whether auth cache is valid
     * @param cache Cached auth parameters
     * @return True if cache is valid (False otherwise)
     * @throws AuthException Exception that is thrown during authorization
     */
    private boolean isCacheValid(Cache cache) throws AuthException {
        try {
            ServiceClientCredentialsFlowResponse authResponse = vkClient.oauth()
                    .serviceClientCredentialsFlow(appId, appSecret)
                    .execute();
            ServiceActor actor = new ServiceActor(appId, appSecret, authResponse.getAccessToken());
            TokenChecked tokenChecked = vkClient.secure().checkToken(actor).token(cache.token).execute();
            return tokenChecked.getSuccess().getValue() == 1;
        } catch (ApiException | ClientException e) {
            throw new AuthException(buildErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * Read auth parameters from cache file
     * @return Auth parameters cache
     */
    private Cache readCachedValue() {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(cachePath.toString()))) {
            String id = bufferedReader.readLine();
            String token = bufferedReader.readLine();
            if (token.isEmpty() || id.isEmpty()) {
                Logger.getInstance().log(Logger.LogKind.WARNING, LOG_MSG_CACHE_READ_FORMAT);
                return null;
            }
            return new Cache(Integer.parseInt(id), token);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Write obtained auth parameters to cache
     * @param cache Cached auth data, obtained from server
     */
    private void writeCachedValue(Cache cache) {
        try(PrintStream bufferedWriter = new PrintStream(cachePath.toString())) {
            bufferedWriter.println(cache.userId);
            bufferedWriter.println(cache.token);
        } catch (FileNotFoundException e) {
            Logger.getInstance().log(Logger.LogKind.WARNING, LOG_MSG_CACHE_WRITE_IO);
        }
    }

    /**
     * Build error message for an exception
     * @param message Message body
     * @return Built error message
     */
    private static String buildErrorMessage(String message) {
        return EXCEPTION_PREFIX + ' ' + message;
    }
}
