package com.github.davidholiday.handlers;


import com.github.davidholiday.App;
import com.github.davidholiday.entities.User;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoginPostHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoginPostHandler.class);

    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    @Override
    public void handle(@NotNull Context ctx) throws Exception {

        var formParamMapKeySet = ctx.formParamMap().keySet();
        if (formParamMapKeySet.contains(USERNAME_KEY) == false || formParamMapKeySet.contains(PASSWORD_KEY) == false) {
            LOG.warn("rejecting request because one of required keys is missing: {} , {}", USERNAME_KEY, PASSWORD_KEY);
            throw new BadRequestResponse();
        }

        var username = ctx.formParam(USERNAME_KEY);
        var password = ctx.formParam(PASSWORD_KEY);

        // we rely on the setter methods of the entity class to hash and validate caller input
        // we can't do this with the password because we need to handle salt later
        var callerUserObj = new User();
        try {
            callerUserObj.setUsername(username);
        } catch (IllegalArgumentException e) {
            LOG.warn("rejecting request because username or password is unacceptable");
            throw new BadRequestResponse();
        }

        // if we're here it means caller input met validation requirements and we can proceed with the db search
        // ty internets https://www.baeldung.com/hibernate-criteria-queries
        EntityManager entityManager = ctx.attribute(App.USER_TABLE_ENTITY_MANAGER);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate[] predicates = new Predicate[1];
        predicates[0] = criteriaBuilder.equal(root.get(USERNAME_KEY), callerUserObj.getUsername());
        //predicates[1] = criteriaBuilder.equal(root.get(PASSWORD_KEY), caller_user_obj.getPassword());

        criteriaQuery.select(root).where(predicates);
        Query query = entityManager.createQuery(criteriaQuery);
        List<User> results = query.getResultList();
        if (results.isEmpty()) {
            LOG.warn("could not find user: {}", callerUserObj.getUsername());
            throw new UnauthorizedResponse();
        }

        // there is a constraint on the db ensuring field 'username' is always unique/
        // if there is a match we know there will only be one.
        User foundUserObj = results.get(0);

        // now match the pw
        // only the found_user_obj will have the correct salt and thus we use its utility method to
        // handle the equality check
        boolean pwMatch = foundUserObj.checkUnhashedEquality(password);
        if (pwMatch == false) {
            LOG.warn("supplied password incorrect for user: {}", callerUserObj.getUsername());
            throw new UnauthorizedResponse();
        }

        // if we're here it's because the caller has provided correct credentials. now we create a session obj for them
        ctx.sessionAttribute(App.USER_NAME_KEY, foundUserObj.getUsername());
        ctx.sessionAttribute(App.USER_ROLE_KEY, foundUserObj.getRole());
        LOG.info("login successfully. setting session data...");
        LOG.info("ctx username attribute value: {}", ctx.sessionAttribute(App.USER_NAME_KEY).toString());
        LOG.info("ctx role attribute value: {}", ctx.sessionAttribute(App.USER_ROLE_KEY).toString());

        // redirect them to main page
        ctx.redirect("/");
    }
}
