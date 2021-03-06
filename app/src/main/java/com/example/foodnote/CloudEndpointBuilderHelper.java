package com.example.foodnote;

import android.util.Log;

import com.example.foodnote.backend.apis.recipeApi.RecipeApi;
import com.example.foodnote.backend.models.appEngineUserApi.AppEngineUserApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

public class CloudEndpointBuilderHelper {

    /*
     *  Private constructor to prevent instantiating
     */
    private CloudEndpointBuilderHelper() {
    }

    /*
     *  @return RecipeEndpoint to the Google App Engine backend
     */
    static RecipeApi getRecipeEndpoints() {
        RecipeApi.Builder builder = new RecipeApi.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                SignInActivity.getCredential())
                .setRootUrl(Constants.ROOT_URL)
                .setGoogleClientRequestInitializer(
                        new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(
                                    final AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                    throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        }
                );
        return builder.build();
    }

    /*
     *  @return AppEngineUserEndpoint to the Google App Engine backend
     */
    static AppEngineUserApi getAppEngineUserEndpoints() {
        AppEngineUserApi.Builder builder = new AppEngineUserApi.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                SignInActivity.getCredential())
                .setRootUrl(Constants.ROOT_URL)
                .setGoogleClientRequestInitializer(
                        new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(
                                    final AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                    throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        }
                );
        return builder.build();
    }
}
