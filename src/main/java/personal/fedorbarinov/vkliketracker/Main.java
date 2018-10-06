package personal.fedorbarinov.vkliketracker;

import com.vk.api.sdk.client.actors.UserActor;
import personal.fedorbarinov.vkliketracker.authorization.AuthManager;
import personal.fedorbarinov.vkliketracker.authorization.BrowserAuthManager;
import personal.fedorbarinov.vkliketracker.parsing.AuthParametersParser;
import personal.fedorbarinov.vkliketracker.parsing.Parser;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Main class of the application
 */
public class Main {
    private static final String CONFIG_AUTH_PATH = "authorization.config"; //Path to the authorization config

    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        try (FileInputStream authConfig = new FileInputStream(CONFIG_AUTH_PATH)) {
            //Get authorization parameters
            AuthParametersParser authParametersParser = new AuthParametersParser();
            AuthManager authManager = new BrowserAuthManager(authParametersParser.parse(authConfig));

            //Perform authorization
            logger.log(Logger.LogKind.INFO, "Authorizing...");
            UserActor userActor = authManager.authorize();
            logger.log(Logger.LogKind.INFO, "Authorized");
        } catch (AuthManager.AuthException | Parser.ParsingException | IOException e) {
            logger.log(Logger.LogKind.ERROR, e.getMessage()); //All kinds of exceptions fall there to be logged
        }
    }
}
