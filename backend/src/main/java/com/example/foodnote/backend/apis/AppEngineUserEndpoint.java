package com.example.foodnote.backend.apis;

import com.example.foodnote.backend.Constants;
import com.example.foodnote.backend.OfyService;
import com.example.foodnote.backend.models.AppEngineUser;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "appEngineUserApi",
        version = "v2",
        resource = "appEngineUser",
        namespace = @ApiNamespace(
                ownerDomain = "models.backend.foodnote.example.com",
                ownerName = "models.backend.foodnote.example.com",
                packagePath = ""
        ),
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.WEB_CLIENT_ID,
                Constants.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE_ID}
)
public class AppEngineUserEndpoint {

    private static final Logger logger = Logger.getLogger(AppEngineUserEndpoint.class.getName());

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(AppEngineUser.class);
    }

    /**
     * @param user used to build an AppEngineUser entity
     * @return user as the AppEngineUser entity
     */
    @ApiMethod(
            name = "getUser",
            path = "appEngineUser",
            httpMethod = ApiMethod.HttpMethod.POST)
    public AppEngineUser getAppEngineUser(final User user) {
        AppEngineUser appEngineUser = new AppEngineUser(user);
        if (user.getUserId() == null) {
            OfyService.ofy().save().entity(appEngineUser).now();
        }
        Objectify objectify = OfyService.ofy().factory().begin();
        return objectify.load().key(appEngineUser.getKey()).now();
    }
}