package com.github.davidholiday.handlers;

import io.javalin.http.Context;

import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LogoutHandler.class);

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        LOG.info("invalidating any active sessions associated with this user...");
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

}

