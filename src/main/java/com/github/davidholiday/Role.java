package com.github.davidholiday;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ADMIN,
    USER,
    ANYONE
}
