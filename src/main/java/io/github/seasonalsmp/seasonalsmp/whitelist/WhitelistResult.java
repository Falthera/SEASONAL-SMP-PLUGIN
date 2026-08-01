package io.github.seasonalsmp.seasonalsmp.whitelist;

public class WhitelistResult {
    public final boolean success;
    public final String message;
    public final String username;
    public final String uuid;

    private WhitelistResult(boolean success, String message, String username, String uuid) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.uuid = uuid;
    }

    public static WhitelistResult success(String username, String uuid) {
        return new WhitelistResult(true, "Player whitelisted successfully.", username, uuid);
    }

    public static WhitelistResult error(String message) {
        return new WhitelistResult(false, message, null, null);
    }
}
