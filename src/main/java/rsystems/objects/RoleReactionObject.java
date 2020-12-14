package rsystems.objects;

public class RoleReactionObject {
    public Long messageID;
    public Long reactionID;
    public Long roleID;

    public RoleReactionObject(Long messageID, Long reactionID, Long roleID) {
        this.messageID = messageID;
        this.reactionID = reactionID;
        this.roleID = roleID;
    }
}
