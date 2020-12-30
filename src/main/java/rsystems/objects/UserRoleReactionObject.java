package rsystems.objects;

public class UserRoleReactionObject {

    public Long userID;
    public Long roleID;
    public boolean addingRole;

    public UserRoleReactionObject(Long userID, Long roleID, boolean addingRole) {
        this.userID = userID;
        this.roleID = roleID;
        this.addingRole = addingRole;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Long getRoleID() {
        return roleID;
    }

    public void setRoleID(Long roleID) {
        this.roleID = roleID;
    }

    public boolean isAddingRole() {
        return addingRole;
    }

    public void setAddingRole(boolean addingRole) {
        this.addingRole = addingRole;
    }
}
