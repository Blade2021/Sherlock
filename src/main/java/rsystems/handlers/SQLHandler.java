package rsystems.handlers;

import rsystems.SherlockBot;
import rsystems.objects.InfractionObject;

import java.sql.*;
import java.util.ArrayList;

public class SQLHandler {

    protected static Connection connection = null;
    private final String DatabaseURL;
    private final String DatabaseUser;
    private final String DatabaseUserPass;

    public SQLHandler(String DatabaseURL, String DatabaseUser, String DatabaseUserPass) {
        this.DatabaseURL = DatabaseURL;
        this.DatabaseUser = DatabaseUser;
        this.DatabaseUserPass = DatabaseUserPass;
        connect();
    }

    /*

            GENERALIZED METHODS FOR SQL INTERACTION

     */
    public void connect() {
        try {
            connection = DriverManager.getConnection(DatabaseURL,DatabaseUser,DatabaseUserPass);

            if(connection.isValid(30)){
                System.out.println("Database connected");
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }


    }

    public ArrayList<String> getStringList(String table, String columnName){
        ArrayList<String> output = new ArrayList<>();

        try{
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT " + columnName + " FROM " + table);
            while(rs.next()){
                output.add(rs.getString(columnName));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return output;
    }

    public String getString(String table, String columnID, Integer rowID){
        String output = "";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT " + columnID.toUpperCase() + " FROM " + table.toUpperCase() + " WHERE ID = " + rowID);

            while (rs.next()) {
                output = rs.getString(columnID.toUpperCase());
            }

        } catch(SQLException throwables){
            System.out.println(throwables.getMessage());
        } finally {
            // do nothing at the moment
        }

        return output;
    }

    public String getString(String table, String columnID, String identfierColumn, Long identfier){
        String output = "";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT"));

            while (rs.next()) {
                output = rs.getString(columnID.toUpperCase());
            }

        } catch(SQLException throwables){
            System.out.println(throwables.getMessage());
        } finally {
            // do nothing at the moment
        }

        return output;
    }

    public void updateLong(String tableName, String columnName, String identifierColumn, Long identifier, Long value){
        try{
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d",tableName,columnName,value,identifierColumn,identifier));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void putString(String tableName, Long GuildID, String event, Long senderColumn, Long receiverColumn){
        try{
            Statement st = connection.createStatement();
            st.execute(String.format("INSERT INTO %s (ChildGuildID, Event, ReceivingUserID, SendingUserID) VALUES (%d %s, %d, %d)"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /*

            GUILD RELATED INTERACTIONS

     */
    public boolean addGuild(String guildID, String ownerID){
        try{
            Statement st = connection.createStatement();
            st.execute("INSERT INTO GuildTable (GuildID, OwnerID) VALUES (" + Long.valueOf(guildID) + ", " + Long.valueOf(ownerID) + ")");
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean removeGuild(String guildID){
        try{
            Statement st = connection.createStatement();
            st.execute("DELETE FROM GuildTable WHERE GuildID = " + Long.valueOf(guildID));
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public void loadGuildData(String guildID){
        String prefix = "!";
        String logChannelID = null;
        String muteRoleID = null;

        try{
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Prefix, LogChannelID, MuteRoleID FROM GuildTable WHERE GuildID = " + Long.valueOf(guildID));
            while(rs.next()){
                prefix = rs.getString("Prefix");
                logChannelID = String.valueOf(rs.getLong("LogChannelID"));
                muteRoleID = String.valueOf(rs.getLong("MuteRoleID"));
            }

            SherlockBot.guildMap.get(guildID).setPrefix(prefix);
            SherlockBot.guildMap.get(guildID).setMuteRoleID(muteRoleID);
            SherlockBot.guildMap.get(guildID).setLogChannelID(String.valueOf(logChannelID));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Integer insertAssignableRole(Long guildID, String command, Long roleID){
        try{
            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO AssignableRoles (ChildGuildID, RoleCommand, RoleID) VALUES (%d, \"%s\", %d)",guildID,command,roleID));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public Integer removeAssignableRole(Long guildID, String command){
        try{
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM AssignableRoles WHERE (ChildGuildID = %d) AND (RoleCommand = \"%s\")",guildID,command));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    /*

    OTHER METHODS....

     */

    public boolean insertInfraction(String guildID, Long violatorID, String violation, Long submitter){
        try{
            Statement st = connection.createStatement();
            st.execute("INSERT INTO InfractionTable (ChildGuildID, ViolatorID, Violation, Submitter) VALUES (" + Long.valueOf(guildID) + ", " + violatorID + ", \"" + violation + "\", " + submitter + ")");
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public ArrayList<InfractionObject> infractions(String guildID, String violatorID){
        ArrayList<InfractionObject> infractionObjects = new ArrayList<>();

        try{
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Violation, UserNote, DateSubmitted, Submitter FROM InfractionTable WHERE ChildGuildID = " + Long.valueOf(guildID) + " AND ViolatorID = " + Long.valueOf(violatorID));
            while(rs.next()){
                infractionObjects.add(new InfractionObject(rs.getString("Violation"),rs.getString("UserNote"),rs.getDate("DateSubmitted"),rs.getLong("Submitter")));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return infractionObjects;
    }

}
