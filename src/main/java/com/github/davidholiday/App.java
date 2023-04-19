package com.github.davidholiday;

import com.github.davidholiday.entities.User;
import com.github.davidholiday.handlers.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    //

    public static final String USER_TABLE_PERSISTENCE_UNIT = "user_table";
    public static final String USER_TABLE_ENTITY_MANAGER = "user_table_em";

    public static final String RESOURCE_TABLE_PERSISTENCE_UNIT = "resource_table";
    public static final String RESOURCE_TABLE_ENTITY_MANAGER = "resource_table_em";

    //

    public static final String DB_DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:mr_data.db";

    //

    public static final String USER_ROLE_KEY = "user_role";
    public static final String USER_NAME_KEY = "username";

    public static void main(String[] args) {

        // create javalin app object but don't fire it up yet
        var app = Javalin.create(config -> {
            // automagically stores session data to the same db the rest of the app is using
            config.jetty.sessionHandler(() -> SessionHelpers.sqlSessionHandler(DB_DRIVER, DB_URL));

            // sets up an RBAC scheme for our endpoints
            config.accessManager((handler, ctx, roles) -> {
                var currentUserRole = getSessionRoleOrAnyone(ctx);

                // the first check is so I can associate ONLY the role ANYONE with a resource. otherwise
                // with only the second check, every resource would have to have every acceptable role explicitly
                // stated and thus nerfing the value of role ANYONE as a universal catch all
                if (roles.contains(Role.ANYONE) || roles.contains(currentUserRole)) {
                    handler.handle(ctx);
                } else {
                    throw new UnauthorizedResponse();
                }
            });
        });

        // create JPA entity objects
        //   also - this is why EntityManagerFactory and not SessionFactory
        //   https://stackoverflow.com/questions/5640778/hibernate-sessionfactory-vs-jpa-entitymanagerfactory
        EntityManagerFactory user_emf = Persistence.createEntityManagerFactory(USER_TABLE_PERSISTENCE_UNIT);
        EntityManagerFactory resource_emf = Persistence.createEntityManagerFactory(RESOURCE_TABLE_PERSISTENCE_UNIT);



// CONVENIENCE FOR INJECTING A USER INTO THE DB *-*-*-*-*-*-*-*-*-

//        try {
//            User user = new User();
//            user.setUsername(PUT USERNAME HERE);
//            user.setPassword(PUT PASSWORD HERE);
//            user.setRole(Role.ADMIN);
//
//            EntityManager em = user_emf.createEntityManager();
//            em.getTransaction().begin();
//            em.persist(user);
//            em.getTransaction().commit();
//            em.close();
//
//    } catch (Exception e) {
//        LOG.error("something went wrong making dummy user", e);
//        throw new RuntimeException();
//    }

// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

        // associate handlers with their REST paths
        // ensure only entitymanagers appropriate for a given resource handler are injected as context attributes
        app.get("/", new RootHandler(), Role.ANYONE);
        app.before("/", ctx -> {
            ctx.attribute(RESOURCE_TABLE_ENTITY_MANAGER, resource_emf.createEntityManager());
        });

        app.get("/login", new LoginHandler(), Role.ANYONE);
        app.post("/login", new LoginPostHandler(), Role.ANYONE);
        app.before("/login", ctx -> {
            ctx.attribute(USER_TABLE_ENTITY_MANAGER, user_emf.createEntityManager());
        });

        app.get("/logout", new LogoutHandler(), Role.ANYONE);

        app.get("/admin", new AdminHandler(), Role.ADMIN);
        app.post("/admin", new AdminPostHandler(), Role.ADMIN);
        app.before("/admin", ctx -> {
            ctx.attribute(RESOURCE_TABLE_ENTITY_MANAGER, resource_emf.createEntityManager());
        });

        // make sure that all entity managers are closed before we flush the context object
        app.after(ctx -> {
            if (ctx.attribute(USER_TABLE_ENTITY_MANAGER) != null ) {
                ((EntityManager)ctx.attribute(USER_TABLE_ENTITY_MANAGER)).close();
            }

            if (ctx.attribute(RESOURCE_TABLE_ENTITY_MANAGER) != null ) {
                ((EntityManager)ctx.attribute(RESOURCE_TABLE_ENTITY_MANAGER)).close();
            }
        });

        // spin up the app
        app.start(7070);
    }


    public static Role getSessionRoleOrAnyone(Context ctx) {
        Optional<Role> roleOptional = Optional.ofNullable(ctx.sessionAttribute(USER_ROLE_KEY));
        if (roleOptional.isEmpty()) {
            return Role.ANYONE;
        } else {
            return roleOptional.get();
        }
    }

}
