package com.github.davidholiday.handlers;

import io.javalin.http.Context;

import j2html.tags.ContainerTag;
import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static j2html.TagCreator.*;

public class AdminHandler extends AbstractHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AdminHandler.class);

    @Override
    public void handle(@NotNull Context ctx) throws Exception {

        var head = getHeadContainerTag();

        var navBar = getNavBarWithLoginOrOutButton(ctx);
        var admin = getAdminFormContainerTag();
        var body = body(navBar, admin);

        String r = html(
                head,
                body
        ).render();

        ctx.html(r);
    }


    ContainerTag getAdminFormContainerTag() {
        // ty so https://stackoverflow.com/questions/1397592/difference-between-id-and-name-attributes-in-html
        return
                form().withClass("pure-form pure-form-aligned")
                        .withStyle("display: flex; justify-content: center; ")
                        .withAction("/admin")
                        .withMethod("post")
                        .with(
                                fieldset().with(
                                        div().withClass("pure-form pure-form-stacked")
                                             .with(
                                                     legend("SUBMIT SECURITY RESOURCE"),
                                                     label("description").withFor("description"),
                                                     textarea().withName("description")
                                                               .withPlaceholder("max 256 characters")
                                                               .withMaxlength("255"),
                                                     label("url").withFor("url"),
                                                     input().withType("url")
                                                            .withName("url")
                                                            .withPlaceholder("must be valid url"),
                                                     button("SUBMIT").withType("submit")
                                                                          .withClass("pure-button")
                                                                          .withStyle("background: rgb(28, 184, 65); color: white;")
                                             )

                                )
                        );
    }


}

