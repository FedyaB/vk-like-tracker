package personal.fedorbarinov.vkliketracker.authorization;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.OAuthException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Authorization using default browser and manual auth code input
 */
public class BrowserAuthManager implements AuthManager {
    //Parameters needed in authorization request
    private static final String AUTH_FORMAT_URI = "https://oauth.vk.com/authorize?client_id=%d&display=%s&redirect_uri=%s&scope=%s&response_type=%s&v=%s";
    private static final String RESPONSE_TYPE = "code";
    private static final String DISPLAY = "page";

    private static final String INPUT_MESSAGE = "Enter the code parameter from browser:";
    private static final String INPUT_ERROR = "The input code was empty";
    private static final String EXCEPTION_PREFIX = "While authorization:"; //Prefix for an exception message

    private Integer appId; //Current VK app id
    private String appSecret; //Secret key of the app
    private String redirectURI; //URI we're being redirected to after passing credentials
    private String apiVersion; //Current VK API version
    private String permissions; //Permissions we need for the app

    private VkApiClient vkClient; //VkApi client instance
    private boolean validation; //Is "need_validation" error is being handled right now?

    /**
     * Public constructor of the class
     * @param parameters Parameters that were obtained from the corresponding config file
     */
    public BrowserAuthManager(Map<String, String> parameters) {
        this.appId = Integer.parseInt(parameters.get("APP_ID"));
        this.appSecret = parameters.get("APP_SECRET");
        this.redirectURI = parameters.get("REDIRECT_URI");
        this.apiVersion = parameters.get("API_VERSION");
        this.permissions = parameters.get("PERMISSIONS");
        this.vkClient = new VkApiClient(HttpTransportClient.getInstance());
        this.validation = false;
    }

    @Override
    public UserActor authorize() throws AuthException {

        try {
            UserAuthResponse authResponse = performAuthorization(); //Try to get authorization response
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
     * Build error message for an exception
     * @param message Message body
     * @return Built error message
     */
    private static String buildErrorMessage(String message) {
        return EXCEPTION_PREFIX + ' ' + message;
    }
}
