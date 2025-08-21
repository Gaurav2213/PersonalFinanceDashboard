
package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // token -> expiryMillis
    private static final Map<String, Long> BLACKLIST = new ConcurrentHashMap<>();

    //used to blacklist the token 
    public static void blacklist(String token, long expMillis) {
        BLACKLIST.put(token, expMillis);
    }

    //used to validate the token if expired or not 
    public static boolean isBlacklisted(String token) {
        Long exp = BLACKLIST.get(token);
        if (exp == null) return false;
        if (System.currentTimeMillis() > exp) { // cleanup after expiry
            BLACKLIST.remove(token);
            return false;
        }
        return true;
    }
}
