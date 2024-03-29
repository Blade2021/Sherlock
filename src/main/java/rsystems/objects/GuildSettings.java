package rsystems.objects;

import rsystems.SherlockBot;

import java.sql.SQLException;

public class GuildSettings {
    private Long guildID;
    private String prefix;
    private Long ownerID;
    private Long logChannelID;
    private Long quarantineRoleID;
    private int embedFilterSetting;
    private int grantedSelfRoleCount;
    private int grantedAutoRoleCount;
    private int welcomeMessageSetting;
    private int inviteFilterEnabled;

    public GuildSettings(Long guildID) {
        this.guildID = guildID;
    }

    public Long getGuildID() {
        return guildID;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(Long ownerID) {
        this.ownerID = ownerID;
    }

    public Long getLogChannelID() {
        return logChannelID;
    }

    public void setLogChannelID(Long logChannelID) {
        this.logChannelID = logChannelID;
    }

    public Long getQuarantineRoleID() {
        return quarantineRoleID;
    }

    public void setQuarantineRoleID(Long quarantineRoleID) {
        this.quarantineRoleID = quarantineRoleID;
    }

    public int getEmbedFilterSetting() {
        return embedFilterSetting;
    }

    public void setEmbedFilterSetting(int embedFilterSetting) {
        this.embedFilterSetting = embedFilterSetting;
    }

    public int getGrantedSelfRoleCount() {
        return grantedSelfRoleCount;
    }

    public void setGrantedSelfRoleCount(int grantedSelfRoleCount) {
        this.grantedSelfRoleCount = grantedSelfRoleCount;
    }

    public int getGrantedAutoRoleCount() {
        return grantedAutoRoleCount;
    }

    public void setGrantedAutoRoleCount(int grantedAutoRoleCount) {
        this.grantedAutoRoleCount = grantedAutoRoleCount;
    }

    public int getWelcomeMessageSetting() {
        return welcomeMessageSetting;
    }

    public void setWelcomeMessageSetting(int welcomeMessageSetting) {
        this.welcomeMessageSetting = welcomeMessageSetting;
    }

    public int isInviteFilterEnabled() {
        return inviteFilterEnabled;
    }

    public void setInviteFilterEnabled(int inviteFilterEnabled) {
        this.inviteFilterEnabled = inviteFilterEnabled;
    }

    public void save(){
        try {
            SherlockBot.database.updateGuild(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
