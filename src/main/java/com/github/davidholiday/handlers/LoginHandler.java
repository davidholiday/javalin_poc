package com.github.davidholiday.handlers;

import io.javalin.http.Context;

import j2html.tags.ContainerTag;
import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static j2html.TagCreator.*;

public class LoginHandler extends AbstractHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LoginHandler.class);

    @Override
    public void handle(@NotNull Context ctx) throws Exception {

        var head = getHeadContainerTag();

        var navBar = getNavBarWithHiddenLoginButton();
        var login = getLoginFormContainerTag();
        var body = body(navBar, login);

        String r = html(
                head,
                body
        ).render();

        ctx.html(r);
    }


    ContainerTag getLoginFormContainerTag() {
        // ty so https://stackoverflow.com/questions/1397592/difference-between-id-and-name-attributes-in-html
        return
                form().withClass("pure-form pure-form-aligned")
                        .withStyle("display: flex; justify-content: center; ")
                        .withAction("/login")
                        .withMethod("post")
                        .with(
                                fieldset().with(
                                        div().withClass("pure-form pure-form-stacked")
                                                .with(
                                                        legend("LOGIN FORM"),
                                                        label("username").withFor("username"),
                                                        input().withType("text")
                                                               .withName("username")
                                                               .withPlaceholder("username"),
                                                        label("password").withFor("password"),
                                                        input().withType("password")
                                                               .withName("password")
                                                               .withPlaceholder("password"),
                                                        button("SUBMIT").withType("submit")
                                                                             .withClass("pure-button")
                                                                             .withStyle("background: rgb(28, 184, 65); color: white;")
                                                )

                                )
                        );

    }


}
