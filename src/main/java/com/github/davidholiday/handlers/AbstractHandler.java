package com.github.davidholiday.handlers;

import static j2html.TagCreator.*;

import com.github.davidholiday.App;
import com.github.davidholiday.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import j2html.tags.ContainerTag;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractHandler implements Handler {

    ContainerTag getHeadContainerTag() {
        return
            head(
                meta().withCharset("utf-8"),
                meta().withName("viewport").withContent("width=device-width, initial-scale=1.0"),
                link().withRel("stylesheet")
                      .withHref("https://cdn.jsdelivr.net/npm/purecss@3.0.0/build/pure-min.css")
                      .attr("integrity", "sha384-X38yfunGUhNzHpBaEBsWLO+A0HDYOQi8ufWDkZ0k9e0eXz/tH3II7uKZ9msv++Ls")
                      .attr("crossorigin", "anonymous"),
                title("david holiday javalin poc")
           );
    }

    ContainerTag getNavBarPartialContainerTag() {
        return
            div().withClass("pure-menu pure-menu-horizontal")
                 .withStyle("display: flex; justify-content: center; width: 100%")
                 .with(
                         a("SECURITY RESOURCES").withHref("#")
                                                     .withStyle("margin-left: auto; font-size: 24px;")
                                                     .withClass("pure-menu-heading pure-menu-link")

                 );
    }


    ContainerTag getLoginButtonPartialContainerTag() {
        return
            div().withStyle("margin-left: auto;")
                 .with(
                         a("LOGIN").withHref("/login")
                                        .withClass("pure-button pure-button-primary")
                 );
    }

    ContainerTag getLogoutButtonPartialContainerTag() {
        return
            div().withStyle("margin-left: auto;")
                 .with(
                         a("LOGOUT").withHref("/logout")
                                         .withClass("pure-button pure-button-error")
                                         .withStyle("background: rgb(202, 60, 60); color: white;")
                 );
    }

    ContainerTag getHiddenHeaderButtonPartialContainerTag() {
        return
                div().withStyle("margin-left: auto;")
                     .with(
                             a("").withHref("#")
                                       .withClass("pure-button")
                                       .withStyle("background: rgb(255, 255, 255);")
                     );
    }

    //

    ContainerTag getLoginOrOutButton(@NotNull Context ctx) {
        var currentUserRole = App.getSessionRoleOrAnyone(ctx);
        return currentUserRole.equals(Role.ANYONE)
                ? getLoginButtonPartialContainerTag()
                : getLogoutButtonPartialContainerTag();
    }


    ContainerTag getNavBarWithLoginOrOutButton(@NotNull Context ctx) {
        var navbarPartialContainerTag = getNavBarPartialContainerTag();
        var loginOrOutButton = getLoginOrOutButton(ctx);
        return navbarPartialContainerTag.with(loginOrOutButton);
    }


    ContainerTag getNavBarWithHiddenLoginButton() {
        var navbarPartialContainerTag = getNavBarPartialContainerTag();
        var hiddenButton = getHiddenHeaderButtonPartialContainerTag();
        return navbarPartialContainerTag.with(hiddenButton);
    }

}
