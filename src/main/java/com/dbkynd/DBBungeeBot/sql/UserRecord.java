package com.dbkynd.DBBungeeBot.sql;

public class UserRecord {
    String discordid;
    String minecraftname;
    String uuid;

    public UserRecord(String discordid, String minecraftname, String uuid) {
        this.discordid = discordid;
        this.minecraftname = minecraftname;
        this.uuid = uuid;
    }

    public String getDiscordId() {
        return discordid;
    }

    public String getMinecraftName() {
        return minecraftname;
    }

    public String getUUID() {
        return uuid;
    }
}
