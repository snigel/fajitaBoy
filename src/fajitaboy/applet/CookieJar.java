package fajitaboy.applet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import fajitaboy.FajitaBoy;

/**
 * Class for managing cookies for the FajitaBoy. All but the get and put methods
 * are meant to be used only by the JS. Everything is overly complicated since
 * applet isn't allowed to call JS functions with arguments.
 */
public class CookieJar {

    /** Applet with JS pages. */
    private FajitaBoy fajitaBoy;

    /** All cookies. */
    private HashMap<String, String> cookies;

    /** Cookie version. */
    private final String version = "fbcv001";

    /** Cookie name. */
    private final String cookieName = "fajitaSettings";

    /**
     * Constructor.
     * 
     * @param fb FajitaBoy
     */
    public CookieJar(final FajitaBoy fb) {
        cookies = new HashMap<String, String>();
        fajitaBoy = fb;

        executeJS("loadCookie()");
    }

    /**
     * Used by JS when reading from cookie.
     * 
     * @param name owner
     * @param cookieData data
     */
    public final void _JS_setCookie(String cookieData) {
        String versionCheck = cookieName + "=" + version;
        String key, value;
        String[] rawCookies;
        int cStart, cEnd, cName;

        if (!cookieData.startsWith(versionCheck, 0)) {
            System.out.println("Cookie version missmatch!");
            return;
        }

        cStart = cookieData.indexOf("&") + 1;
        cEnd = cookieData.indexOf(";");

        if (cStart == 0) {
            return;
        }

        if (cEnd == -1) {
            cEnd = cookieData.length();
        }
        rawCookies = cookieData.substring(cStart, cEnd).split("&");
        for (int i = 0; i < rawCookies.length; i++) {
            cName = rawCookies[i].indexOf("#");
            key = rawCookies[i].substring(0, cName);
            value = rawCookies[i].substring(cName + 1);

            cookies.put(key, value);
        }
    }

    /**
     * Used by JS when saving to cookie.
     * 
     * @return data to save
     */
    public final String _JS_getCookie() {
        String data = cookieName + "=" + version;
        String name, value;

        Iterator<String> it = cookies.keySet().iterator();

        while (it.hasNext()) {
            name = it.next();
            value = cookies.get(name);

            data += "&" + name + "#" + value;
        }
        return data;
    }

    /**
     * Saves some data in a cookie with specified name.
     * 
     * @param name String
     * @param value String
     */
    public final void put(final String name, final String value) {
        cookies.put(name, value);
        executeJS("saveCookie()");
    }

    /**
     * Loads some data from a cookie with specified name.
     * 
     * @param name String
     * @return value String
     */
    public final String get(final String name) {
        if (cookies.containsKey(name)) {
            return cookies.get(name);
        }

        return null;
    }

    /**
     * Runs some javascript on the fajita-page.
     * 
     * @param script JS-string to run
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
