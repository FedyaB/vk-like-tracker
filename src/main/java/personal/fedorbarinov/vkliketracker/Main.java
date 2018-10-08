package personal.fedorbarinov.vkliketracker;

import com.vk.api.sdk.client.actors.UserActor;
import personal.fedorbarinov.vkliketracker.authorization.AuthManager;
import personal.fedorbarinov.vkliketracker.authorization.BrowserAuthManager;
import personal.fedorbarinov.vkliketracker.parsing.AuthConfigParser;
import personal.fedorbarinov.vkliketracker.parsing.Parser;
import personal.fedorbarinov.vkliketracker.parsing.TaskConfigParser;
import personal.fedorbarinov.vkliketracker.tracker.LikeTracker;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Main class of the application
 */
public class Main {
    private static final String CONFIG_AUTH_PATH = "authorization.config"; //Path to the authorization config
    private static final String CONFIG_TASK_PATH = "task.config"; //Path to the authorization config
    private static final String LOG_MSG_AUTHORIZING = "Authorizing...";
    private static final String LOG_MSG_TRACKING = "Tracking...";

    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        try (FileInputStream authConfig = new FileInputStream(CONFIG_AUTH_PATH);
             FileInputStream taskConfig = new FileInputStream(CONFIG_TASK_PATH)) {
            //Get authorization parameters
            Parser authParametersParser = new AuthConfigParser();
            AuthManager authManager = new BrowserAuthManager(authParametersParser.parse(authConfig));

            //Perform authorization
            logger.log(Logger.LogKind.INFO, LOG_MSG_AUTHORIZING);
            UserActor userActor = authManager.authorize();

            //Get task parameters
            Parser taskConfigParser = new TaskConfigParser();
            LikeTracker likeTracker = new LikeTracker(userActor, taskConfigParser.parse(taskConfig));

            //Run like tracker
            logger.log(Logger.LogKind.INFO, LOG_MSG_TRACKING);
            likeTracker.run();
        } catch (AuthManager.AuthException | Parser.ParsingException |
                LikeTracker.TrackingException | IOException e) {
            logger.log(Logger.LogKind.ERROR, e.getMessage()); //All kinds of exceptions fall there to be logged
        }
    }
}
