package com.github.davidholiday;

import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.session.*;

public class SessionHelpers {

    public static int MAX_SESSION_INACTIVE_INTERVAL_IN_SECONDS = 60 * 15; // 15min

    public static SessionHandler sqlSessionHandler(String driver, String url) {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(
                jdbcDataStoreFactory(driver, url).getSessionDataStore(sessionHandler)
        );
        sessionHandler.setSessionCache(sessionCache);
        sessionHandler.setMaxInactiveInterval(MAX_SESSION_INACTIVE_INTERVAL_IN_SECONDS);
        sessionHandler.setHttpOnly(true);
        sessionHandler.setSecureRequestOnly(true);
        sessionHandler.setSameSite(HttpCookie.SameSite.STRICT);
        return sessionHandler;
    }

    private static JDBCSessionDataStoreFactory jdbcDataStoreFactory(String driver, String url) {
        DatabaseAdaptor databaseAdaptor = new DatabaseAdaptor();
        databaseAdaptor.setDriverInfo(driver, url);
        // databaseAdaptor.setDatasource(myDataSource); // you can set data source here (for connection pooling, etc)
        JDBCSessionDataStoreFactory jdbcSessionDataStoreFactory = new JDBCSessionDataStoreFactory();
        jdbcSessionDataStoreFactory.setDatabaseAdaptor(databaseAdaptor);
        return jdbcSessionDataStoreFactory;
    }

}
