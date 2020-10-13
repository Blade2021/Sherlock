package rsystems.objects;

public class SelfRole {
    public Long GuildID;
    public String command;
    public Long RoleID;

    public SelfRole(Long guildID, String command, Long roleID) {
        this.GuildID = guildID;
        this.command = command;
        this.RoleID = roleID;
    }
}
