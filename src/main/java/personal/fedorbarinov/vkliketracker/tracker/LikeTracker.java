package personal.fedorbarinov.vkliketracker.tracker;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.likes.responses.IsLikedResponse;
import com.vk.api.sdk.objects.utils.DomainResolved;
import com.vk.api.sdk.objects.utils.DomainResolvedType;
import com.vk.api.sdk.queries.likes.LikesType;
import personal.fedorbarinov.vkliketracker.Logger;
import personal.fedorbarinov.vkliketracker.parsing.Parser;
import personal.fedorbarinov.vkliketracker.parsing.TaskConfigParser;

/**
 * Like tracking engine
 */
public class LikeTracker {
    private static final String BAD_POST_LINK  = "Couldn't parse link to the wall post";
    private static final String BAD_TARGET  = "Target user doesn't exist";
    private static final String POST_LIKED  = "The post is liked";
    private static final String POST_NOT_LIKED  = "The post is not liked";
    private static final String EXCEPTION_PREFIX  = "[LikeTracker]:"; //Prefix for an exception

    /**
     * Exception that is thrown during tracking
     */
    public class TrackingException extends Exception {
        TrackingException(String s) {
            super(s);
        }
    }

    private UserActor user; //Current authorized user
    private VkApiClient vkClient; //VK client instance
    private Parser.ParsingResult task; //Task ontained from parser

    private Integer targetId; //Who liked
    private Integer ownerId; //Whose post was liked
    private Integer postId; //Which post was liked

    public LikeTracker(UserActor user, Parser.ParsingResult task) {
        this.user = user;
        this.vkClient = new VkApiClient(HttpTransportClient.getInstance());
        this.task = task;
    }

    /**
     * Run tracking
     * @throws TrackingException Exception that is thrown during tracking
     */
    public void run() throws TrackingException {
        processTaskParameters();
        try {
            //Check whether targetId liked postId of ownerId
            IsLikedResponse isLikedResponse = vkClient.likes().isLiked(user, LikesType.POST, postId)
                    .ownerId(ownerId).userId(targetId)
                    .execute();
            if (isLikedResponse.isLiked())
                Logger.getInstance().log(Logger.LogKind.INFO, POST_LIKED);
            else
                Logger.getInstance().log(Logger.LogKind.INFO, POST_NOT_LIKED);
        } catch (ClientException | ApiException e) {
            throw new TrackingException(buildErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * Process task parameters manually (a.e. substitute data)
     * @throws TrackingException Exception that is thrown during tracking
     */
    private void processTaskParameters() throws TrackingException {
        //Processing link
        String link = task.get(TaskConfigParser.POST_LINK_LABEL);
        String[] segments = link.split("wall");
        String postData = segments[segments.length - 1];
        segments = postData.split("_");
        if (segments.length != 2)
            throw new TrackingException(buildErrorMessage(BAD_POST_LINK));
        this.ownerId = Integer.parseInt(segments[0]);
        this.postId = Integer.parseInt(segments[1]);

        //Processing target
        String name = task.get(TaskConfigParser.TARGET_LABEL);
        if (!name.matches("[0-9]+")) {
            try {
                //Transform screen_name to user ID
                DomainResolved resolved = vkClient.utils().resolveScreenName(user, name)
                        .execute();
                if (resolved.getType() != DomainResolvedType.USER)
                    throw new TrackingException(buildErrorMessage(BAD_TARGET));
                this.targetId = resolved.getObjectId();
            } catch (ApiException | ClientException e) {
                throw new TrackingException(buildErrorMessage(BAD_TARGET));
            }
        } else //In case the target parameter is already an ID
            this.targetId = Integer.parseInt(name);
    }

    /**
     * Build error message for an exception
     * @param message Message body
     * @return Built error message
     */
    private String buildErrorMessage(String message) {
        return EXCEPTION_PREFIX + ' ' + message;
    }
}
