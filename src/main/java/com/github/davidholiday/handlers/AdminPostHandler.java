package com.github.davidholiday.handlers;

import com.github.davidholiday.App;
import com.github.davidholiday.entities.Resource;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class AdminPostHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AdminPostHandler.class);

    public static final String URL_KEY = "url";
    public static final String DESCRIPTION_KEY = "description";

    @Override
    public void handle(@NotNull Context ctx) throws Exception {

        var formParamMapKeySet = ctx.formParamMap().keySet();
        if (formParamMapKeySet.contains(URL_KEY) == false || formParamMapKeySet.contains(DESCRIPTION_KEY) == false) {
            LOG.warn("rejecting request because one of required keys is missing: {} , {}", URL_KEY, DESCRIPTION_KEY);
            throw new BadRequestResponse();
        }

        var url = ctx.formParam(URL_KEY);
        var description = ctx.formParam(DESCRIPTION_KEY);

        Resource resource = new Resource();
        try {
            resource.setUrl(url);
            resource.setDescription(description);
        } catch (IllegalArgumentException e) {
            LOG.warn("rejecting request because url or description are unacceptable");
            throw new BadRequestResponse();
        }

        // if we're here it's because we can safely serialize the data the caller provided
        EntityManager entityManager = ctx.attribute(App.RESOURCE_TABLE_ENTITY_MANAGER);
        entityManager.getTransaction().begin();
        entityManager.persist(resource);
        entityManager.getTransaction().commit();
        entityManager.close();

        ctx.redirect("/");
    }

}
