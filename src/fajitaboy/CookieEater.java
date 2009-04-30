package fajitaboy;

/**
 * Interface for classes who want to receive data from cookies. After calling
 * get(cookie) on the CookieJar the function recieveCookie will be called on the
 * CookieEater as soon as cookie data is available.
 *
 */
public interface CookieEater {

    /**
     * You will be called upon with cookies once proven worthy.
     *
     * @param cookie
     *            OMNOMNOM
     */
    void recieveCookie(String cookie);
}
