package personal.fedorbarinov.vkliketracker.authorization;

import com.vk.api.sdk.client.actors.UserActor;

/**
 * VK authorization manager
 */
public interface AuthManager {
    /**
     * Exception that is thrown during authorization
     */
    class AuthException extends Exception {
        AuthException(String s) {
            super(s);
        }
    }

    /**
     * Authorize to VK
     * @return UserActor object used in VK API calls (represents current user)
     * @throws AuthException Exception that is thrown during authorization
     */
    UserActor authorize() throws AuthException;
}
