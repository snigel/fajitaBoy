package fajitaboy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * Class for managing cookies for the FajitaBoy. All but the get and put methods
 * are meant to be used only by the JS. Everything is overly complicated since
 * applet isn't allowed to call JS functions with arguments.
 */
public class CookieJar {

    /** Cookie name. */
    private String name;
    /** Cookie data. */
    private String data;

    /** Applet with JS pages. */
    private FajitaBoy fajitaBoy;

    /** Stores references between cookie names and cookie owners. */
    private Hashtable<String, CookieEater> cookieOwners;

    /**
     * Constructor.
     * 
     * @param fb
     *            FajitaBoy
     */
    public CookieJar(final FajitaBoy fb) {
        cookieOwners = new Hashtable<String, CookieEater>();
        fajitaBoy = fb;
        name = "";
        data = "";
    }

    /**
     * Used by JS when reading from cookie.
     * 
     * @param name
     *            owner
     * @param cookieData
     *            data
     */
    public final void setCookieData(final String name, final String cookieData) {
        if (cookieOwners.containsKey(name)) {
            cookieOwners.get(name).recieveCookie(cookieData);
        }
    }

    /**
     * Used by JS when saving to cookie.
     * 
     * @return data to save
     */
    public final String getCookieData() {
        return data;
    }

    /**
     * Used by JS when saving to cookie.
     * 
     * @return cookie name
     */
    public final String getCookieName() {
        return name;
    }

    /**
     * Saves some data in a cookie with specified name.
     * 
     * @param cookieName
     *            String
     * @param value
     *            String
     */
    public final void put(final String cookieName, final String value) {
        name = cookieName;
        data = value;

        executeJS("saveCookie()");
    }

    /**
     * Loads some data from a cookie with specified name.
     * 
     * @param cookieName
     *            String
     * @param owner
     *            CookieEater
     */
    public final void get(final String cookieName, final CookieEater owner) {
        cookieOwners.put(cookieName, owner);
        name = cookieName;
        data = "";

        executeJS("loadCookie()");
    }

    /**
     * Runs some javascript on the fajita-page.
     * 
     * @param script
     *            JS-string to run
     */
    private void executeJS(final String script) {
        try {
            fajitaBoy.getAppletContext().showDocument(
                    new URL("javascript:" + script), "_self");

        } catch (MalformedURLException e) {
            System.out.println("Invalid Javascript.");
        }
    }
}
