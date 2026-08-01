package io.github.seasonalsmp.seasonalsmp.whitelist;

import java.util.Collections;
import java.util.Map;

public class WhitelistLookupResult {
    public final boolean found;
    public final String message;
    public final Map<String, Object> data;

    private WhitelistLookupResult(boolean found, String message, Map<String, Object> data) {
        this.found = found;
        this.message = message;
        this.data = data != null ? data : Collections.emptyMap();
    }

    public static WhitelistLookupResult found(Map<String, Object> data) {
        return new WhitelistLookupResult(true, "Player found.", data);
    }

    public static WhitelistLookupResult notFound(String message) {
        return new WhitelistLookupResult(false, message, Collections.emptyMap());
    }
}
