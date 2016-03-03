package com.example.foodnote.backend.utils;

import com.example.foodnote.backend.models.AppEngineUser;
import com.example.foodnote.backend.OfyService;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Objectify;

import java.util.logging.Logger;

public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    /**
     * Get userId for null userId from Android clients.
     *
     * @param user A User object injected by the cloud endpoints.
     * @return the App Engine userId for the user.
     */
    public static String getUserId(User user) {
        if (user == null) {
            LOG.warning("Got a null user! Investigate!");
            return "";
        }
        String userId = user.getUserId();
        if (userId == null) {
            LOG.info("userId is null, so trying to obtain it from the datastore.");
            AppEngineUser appEngineUser = new AppEngineUser(user);
            OfyService.ofy().save().entity(appEngineUser).now();
            // Begin new session for not using session cache.
            Objectify objectify = OfyService.ofy().factory().begin();
            AppEngineUser savedUser = objectify.load().key(appEngineUser.getKey()).now();
            userId = savedUser.getUser().getUserId();
            LOG.info("Obtained the userId: " + userId);
        }
        return userId;
    }
}
