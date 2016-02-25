package com.example.foodnote.backend.apis;

import com.example.foodnote.backend.models.Step;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Api(
        name = "stepApi",
        version = "v1",
        resource = "step",
        namespace = @ApiNamespace(
                ownerDomain = "apis.backend.foodnote.example.com",
                ownerName = "apis.backend.foodnote.example.com",
                packagePath = ""
        )
)
public class StepEndpoint {

    private static final Logger logger = Logger.getLogger(StepEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Returns the {@link Step} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Step} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "step/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Step get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Step with ID: " + id);
        Step step = ofy().load().type(Step.class).id(id).now();
        if (step == null) {
            throw new NotFoundException("Could not find Step with ID: " + id);
        }
        return step;
    }

    /**
     * Inserts a new {@code Step}.
     */
    @ApiMethod(
            name = "insert",
            path = "step",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Step insert(Step step) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that step.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(step).now();
        logger.info("Created Step with ID: " + step.getId());

        return ofy().load().entity(step).now();
    }

    /**
     * Updates an existing {@code Step}.
     *
     * @param id   the ID of the entity to be updated
     * @param step the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Step}
     */
    @ApiMethod(
            name = "update",
            path = "step/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Step update(@Named("id") Long id, Step step) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(step).now();
        logger.info("Updated Step: " + step);
        return ofy().load().entity(step).now();
    }

    /**
     * Deletes the specified {@code Step}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Step}
     */
    @ApiMethod(
            name = "remove",
            path = "step/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Step.class).id(id).now();
        logger.info("Deleted Step with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "step",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Step> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Step> query = ofy().load().type(Step.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Step> queryIterator = query.iterator();
        List<Step> stepList = new ArrayList<Step>(limit);
        while (queryIterator.hasNext()) {
            stepList.add(queryIterator.next());
        }
        return CollectionResponse.<Step>builder().setItems(stepList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(Step.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Step with ID: " + id);
        }
    }
}