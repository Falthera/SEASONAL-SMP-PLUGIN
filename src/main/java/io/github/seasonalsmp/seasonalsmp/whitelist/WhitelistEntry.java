package io.github.seasonalsmp.seasonalsmp.whitelist;

import java.time.LocalDateTime;

public class WhitelistEntry {
    private String discordId;
    private String username;
    private String uuid;
    private LocalDateTime whitelistedAt;

    public WhitelistEntry(String discordId, String username, String uuid, LocalDateTime whitelistedAt) {
        this.discordId = discordId;
        this.username = username;
        this.uuid = uuid;
        this.whitelistedAt = whitelistedAt;
    }

    public String getDiscordId() {
        return discordId;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getWhitelistedAt() {
        return whitelistedAt;
    }
}
