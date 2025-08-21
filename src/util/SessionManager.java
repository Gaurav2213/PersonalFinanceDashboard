package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // jti -> expiryMillis
    private static final Map<String, Long> BLACKLIST = new ConcurrentHashMap<>();

    public static void blacklist(String jti, long expMillis) {
        if (jti == null) return; // defensive
        BLACKLIST.put(jti, expMillis);
    }

    public static boolean isBlacklisted(String jti) {
        if (jti == null) return false;
        Long exp = BLACKLIST.get(jti);
        if (exp == null) return false;

        long now = System.currentTimeMillis();
        if (now > exp) {            // expired entry -> cleanup
            BLACKLIST.remove(jti);
            return false;
        }
        return true;
    }
}

