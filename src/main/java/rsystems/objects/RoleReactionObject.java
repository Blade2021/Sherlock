package rsystems.objects;

public class RoleReactionObject {
    public Long messageID;
    public String reactionID;
    public Long roleID;
    public boolean removeRole = false;

    public RoleReactionObject(Long messageID, String reactionID, Long roleID) {
        this.messageID = messageID;
        this.reactionID = reactionID;
        this.roleID = roleID;
    }

    public RoleReactionObject(Long messageID, String reactionID, Long roleID, boolean removeRole) {
        this.messageID = messageID;
        this.reactionID = reactionID;
        this.roleID = roleID;
        this.removeRole = removeRole;
    }
}
