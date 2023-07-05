package com.tuleninov.chiphonecontacts;

/**
 * The Routes class contains the routes for application.
 *
 * @author Oleksandr Tuleninov
 * @version 01
 */
public final class Routes {

    private Routes() {
        throw new AssertionError("non-instantiable class");
    }

    public static final String API_ROOT = "/api/v1";

    public static final String USERS = API_ROOT + "/users";

    public static final String TOKEN = API_ROOT + "/token";

    public static final String PHONES = API_ROOT + "/phones";


    public static String user(long id) {
        return USERS + '/' + id;
    }

}
