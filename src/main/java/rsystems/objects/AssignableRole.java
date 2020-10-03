package rsystems.objects;

public class AssignableRole {
    public Long GuildID;
    public String command;
    public Long RoleID;

    public AssignableRole(Long guildID, String command, Long roleID) {
        this.GuildID = guildID;
        this.command = command;
        this.RoleID = roleID;
    }
}
