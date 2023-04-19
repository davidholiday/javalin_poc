package com.github.davidholiday.handlers;


import com.github.davidholiday.App;
import com.github.davidholiday.entities.Resource;
import io.javalin.http.Context;

import j2html.tags.ContainerTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static j2html.TagCreator.*;

public class RootHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RootHandler.class);

    @Override
    public void handle(@NotNull Context ctx) throws Exception {

        var head = getHeadContainerTag();
        var navBar = getNavBarWithLoginOrOutButton(ctx);
        var resourcesTable = getResourcesTable(ctx);

        String r = html(
            head,
            navBar,
            body(resourcesTable)
        ).render();

        ctx.html(r);
    }

    ContainerTag getResourcesTable(Context ctx) {

        // dump Resource table
        EntityManager entityManager = ctx.attribute(App.RESOURCE_TABLE_ENTITY_MANAGER);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Resource> criteriaQuery = criteriaBuilder.createQuery(Resource.class);
        Root<Resource> rootEntry = criteriaQuery.from(Resource.class);
        CriteriaQuery<Resource> all = criteriaQuery.select(rootEntry);
        TypedQuery<Resource> allQuery = entityManager.createQuery(all);
        List<Resource> resultList = allQuery.getResultList();

        // turn results into html table obj
        return
                div().withStyle("display: flex; justify-content: center; ")
                     .with(
                        table().withClass("pure-table")
                                .with(
                                        thead().with(
                                                tr().with(
                                                        th(AdminPostHandler.URL_KEY),
                                                        th(AdminPostHandler.DESCRIPTION_KEY)
                                                )
                                        ),
                                        tbody(
                                              each(resultList, row -> tr(
                                                      td(row.getUrl()),
                                                      td(row.getDescription())
                                              ))
                                        )
                                )
                     );
    }

}
/*
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 9, 10);
        ...
        table(attr("#table-example"),
        tbody(
        each(numbers, i -> tr(
        each(numbers, j -> td(
        String.valueOf(i * j)
        ))
        ))
        )
        )
*/
