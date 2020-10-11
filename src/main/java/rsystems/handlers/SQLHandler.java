package rsystems.handlers;

import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.objects.AssignableRole;
import rsystems.objects.InfractionObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

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
            connection = DriverManager.getConnection(DatabaseURL, DatabaseUser, DatabaseUserPass);

            if (connection.isValid(30)) {
                System.out.println("Database connected");
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }


    }

    public ArrayList<String> getStringList(String table, String columnName) {
        ArrayList<String> output = new ArrayList<>();

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT " + columnName + " FROM " + table);
            while (rs.next()) {
                output.add(rs.getString(columnName));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return output;
    }

    public String getString(String table, String columnID, Integer rowID) {
        String output = "";

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT " + columnID.toUpperCase() + " FROM " + table.toUpperCase() + " WHERE ID = " + rowID);

            while (rs.next()) {
                output = rs.getString(columnID.toUpperCase());
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            // do nothing at the moment
        }

        return output;
    }

    public String getString(String table, String columnID, String identfierColumn, Long identfier) {
        String output = "";

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT"));

            while (rs.next()) {
                output = rs.getString(columnID.toUpperCase());
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            // do nothing at the moment
        }

        return output;
    }

    public Integer getInt(String table, String columnName, String identifierColumn, Long identifier) {
        int output = 0;

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d",columnName,table,identifierColumn,identifier));

            while (rs.next()) {
                output = rs.getInt(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            // do nothing at the moment
        }

        return output;
    }


    public int putValue(String tableName, String columnName, String identifierColumn, Long identifier, Long value) {
        int output = 0;
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));
            output = st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return output;
    }

    public int putValue(String tableName, String columnName, String identifierColumn, Long identifier, int value) {
        int output = 0;
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));

            output = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return output;
    }

    public int putValue(String tableName, String columnName, String identifierColumn, Long identifier, String value) {
        int output = 0;
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = \"%s\" WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));
            output = st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return output;
    }

    public void putString(String tableName, Long GuildID, String event, Long senderColumn, Long receiverColumn) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute(String.format("INSERT INTO %s (ChildGuildID, Event, ReceivingUserID, SendingUserID) VALUES (%d %s, %d, %d)"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /*

                    GUILD RELATED INTERACTIONS

     */
    public boolean addGuild(String guildID, String ownerID) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute("INSERT INTO GuildTable (GuildID, OwnerID) VALUES (" + Long.valueOf(guildID) + ", " + Long.valueOf(ownerID) + ")");
            st.execute("INSERT INTO WelcomeTable (ChildGuildID) VALUES (" + Long.valueOf(guildID) + ")");
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean removeGuild(String guildID) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute("DELETE FROM GuildTable WHERE GuildID = " + Long.valueOf(guildID));
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean addWelcomeRow(String guildID) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute("INSERT INTO WelcomeTable (ChildGuildID) VALUES (" + Long.valueOf(guildID) + ")");
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public void loadGuildData(String guildID) {
        String prefix = "!";
        String logChannelID = null;
        String muteRoleID = null;
        int embedFilter = 0;

        try {
            /*
                    GRAB GUILD SETTINGS FROM DATABASE
             */
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Prefix, LogChannelID, MuteRoleID, EmbedFilter FROM GuildTable WHERE GuildID = " + Long.valueOf(guildID));
            while (rs.next()) {
                prefix = rs.getString("Prefix");
                logChannelID = String.valueOf(rs.getLong("LogChannelID"));
                muteRoleID = String.valueOf(rs.getLong("MuteRoleID"));
                embedFilter = rs.getInt("EmbedFilter");
            }

            SherlockBot.guildMap.get(guildID).setPrefix(prefix);
            SherlockBot.guildMap.get(guildID).setMuteRoleID(muteRoleID);
            SherlockBot.guildMap.get(guildID).setLogChannelID(String.valueOf(logChannelID));

            /*
                    GRAB ASSIGNABLE ROLES FROM DATABASE
             */
            ArrayList<AssignableRole> assignableRoles = new ArrayList<>();
            assignableRoles = getAssignableRoles(Long.valueOf(guildID));
            for (AssignableRole role : assignableRoles) {
                SherlockBot.guildMap.get(guildID).assignableRoleMap.put(role.command, role.RoleID);
            }

            SherlockBot.guildMap.get(guildID).setBlacklistedWords(getBadWords(Long.valueOf(guildID)));
            SherlockBot.guildMap.get(guildID).setEmbedFilter(embedFilter);

            /*
            WELCOME MESSAGE VARIABLES
             */
            Long welcomeChannelID = null;
            String welcomeMessage = null;
            int welcomeMethod = 0;
            int welcomeMessageTimeout = 30;

            //Check to make sure row exists, if not insert one.
            rs = st.executeQuery(String.format("SELECT ChildGuildID FROM WelcomeTable where ChildGuildID = %d",Long.valueOf(guildID)));
            int rowsFound = 0;
            while(rs.next()){
                rowsFound++;
                System.out.println(rowsFound);
            }

            if(rowsFound == 0){
                addWelcomeRow(guildID);
            }


            rs = st.executeQuery("SELECT WelcomeChannelID, WelcomeMessage, WelcomeMethod, MessageTimeout FROM WelcomeTable WHERE ChildGuildID = " + Long.valueOf(guildID));
            while (rs.next()) {
                welcomeChannelID = rs.getLong("WelcomeChannelID");
                welcomeMessage = rs.getString("WelcomeMessage");
                welcomeMethod = rs.getInt("WelcomeMethod");
                welcomeMessageTimeout = rs.getInt("MessageTimeout");
            }
            SherlockBot.guildMap.get(guildID).setWelcomeChannelID(welcomeChannelID);
            SherlockBot.guildMap.get(guildID).setWelcomeMessage(welcomeMessage);
            SherlockBot.guildMap.get(guildID).setWelcomeMethod(welcomeMethod);
            SherlockBot.guildMap.get(guildID).setWelcomeMessageTimeout(welcomeMessageTimeout);

            System.out.println("Guild data loaded | GuildID" + guildID);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    // Add an assignable role to the database
    public Integer insertAssignableRole(Long guildID, String command, Long roleID) {

        if(checkSize(guildID,"AssignableRoles")) {

            try {
                if ((connection == null) || (connection.isClosed())) {
                    connect();
                }

                Statement st = connection.createStatement();

                st.execute(String.format("INSERT INTO AssignableRoles (ChildGuildID, RoleCommand, RoleID) VALUES (%d, \"%s\", %d)", guildID, command, roleID));
                return 200;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            return 201;
        }
        return 0;
    }

    // Remove an assignable role to the database
    public Integer removeAssignableRole(Long guildID, String command) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM AssignableRoles WHERE (ChildGuildID = %d) AND (RoleCommand = \"%s\")", guildID, command));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    //Get Assignable Roles - Called by GuildLoader
    public ArrayList<AssignableRole> getAssignableRoles(Long guildID) {
        ArrayList<AssignableRole> output = new ArrayList<>();
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleCommand, RoleID FROM AssignableRoles WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                output.add(new AssignableRole(guildID, rs.getString("RoleCommand"), rs.getLong("RoleID")));
            }

            return output;

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            // do nothing at the moment
        }
        return null;
    }

    public boolean insertTimedEvent(Long GuildID, Long UserID, int EventType, String reason, LocalDateTime startDatetime, LocalDateTime endDateTime){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO TimedEvents (ChildGuildID, EventID, EventType, Reason, StartDate, EndDate) VALUES (%d, %d, %d, \"%s\", \"%s\", \"%s\")", GuildID, UserID, EventType, reason, startDatetime.toString(), endDateTime.toString()));
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return false;
    }

    public boolean insertTimedEvent(Long GuildID, Long UserID, int EventType, String reason, Long EventKey, int EventValue, LocalDateTime startDatetime, LocalDateTime endDateTime){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO TimedEvents (ChildGuildID, EventID, EventType, Reason, Event_SubKey, Event_SubValue, StartDate, EndDate) VALUES (%d, %d, %d, \"%s\", %d, %d, \"%s\", \"%s\")", GuildID, UserID, EventType, reason, EventKey, EventValue, startDatetime.toString(), endDateTime.toString()));
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return false;
    }

    public int getTimedEventsQuantity(Long GuildID, Long UserID){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT EventType FROM TimedEvents WHERE (ChildGuildID = %d) and (EventID = %d) and (EndDate < NOW())", GuildID, UserID));
            int rowCount = 0;
            while(rs.next()){
                rowCount++;
            }
            return rowCount;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }
        return 0;
    }
    public boolean expireTimedEvent(Long GuildID, Long UserID){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.executeQuery(String.format("UPDATE TimedEvents set Expired = 1 WHERE (ChildGuildID = %d) and (EventID = %d) and (EndDate > NOW())", GuildID, UserID));
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return false;
    }

    public HashMap<Long,Integer> removeCooldown(Long GuildID, Long EventID){
        HashMap<Long,Integer> output = new HashMap<>();

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Event_SubKey, Event_SubValue FROM TimedEvents WHERE (ChildGuildID = %d) and (EventID = %d) and (Expired=0)", GuildID, EventID));
            while(rs.next()){
                output.put(rs.getLong("Event_SubKey"),rs.getInt("Event_SubValue"));
            }

            st.executeUpdate(String.format("DELETE FROM TimedEvents WHERE (ChildGuildID = %d) AND (EventID = %d)",GuildID,EventID));

            return output;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return null;
    }

    /*

    OTHER METHODS....

     */

    public void logError(String guildID, String event){
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute("INSERT INTO FaultTable (ChildGuildID, Event) VALUES (" + Long.valueOf(guildID) + ", \"" + event + "\")");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void logError(String guildID, String event, int errorCode){
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute(String.format("INSERT INTO FaultTable (ChildGuildID, Event, ErrorCode) VALUES (%d, \"%s\", %d)",Long.valueOf(guildID),event,errorCode));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void logError(String guildID, String event, Long relateable){
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute("INSERT INTO FaultTable (ChildGuildID, Event, Relateables) VALUES (" + Long.valueOf(guildID) + ", \"" + event + "\", " + relateable + ")");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean insertInfraction(String guildID, Long violatorID, String violation, Long submitter) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute("INSERT INTO InfractionTable (ChildGuildID, ViolatorID, Violation, Submitter) VALUES (" + Long.valueOf(guildID) + ", " + violatorID + ", \"" + violation + "\", " + submitter + ")");
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public ArrayList<InfractionObject> infractions(String guildID, String violatorID) {
        ArrayList<InfractionObject> infractionObjects = new ArrayList<>();

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Violation, UserNote, DateSubmitted, Submitter FROM InfractionTable WHERE ChildGuildID = " + Long.valueOf(guildID) + " AND ViolatorID = " + Long.valueOf(violatorID));
            while (rs.next()) {
                infractionObjects.add(new InfractionObject(rs.getString("Violation"), rs.getString("UserNote"), rs.getDate("DateSubmitted"), rs.getLong("Submitter")));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return infractionObjects;
    }

    public ArrayList<String> getBadWords(Long guildID) {
        ArrayList<String> badWordsList = new ArrayList<>();

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Word FROM LanguageFilter WHERE ChildGuildID = " + guildID);
            while (rs.next()) {
                badWordsList.add(rs.getString("Word"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return badWordsList;
    }

    // INSERT A BADWORD INTO THE DATABASE
    public Integer insertBadWord(Long guildID, String badWord) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO LanguageFilter (ChildGuildID, Word) VALUES (%d, \"%s\")", guildID, badWord));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    // REMOVE A BLACKLISTED WORD FROM THE DATABASE
    public Integer removeBadWord(Long guildID, String badWord) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM LanguageFilter WHERE (ChildGuildID = %d) AND (Word = \"%s\")", guildID, badWord));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }


    private boolean checkSize(Long guildID, String tableName){
        int rowCount = 0;

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Count(ChildGuildID) from %s where ChildGuildID = %d", tableName, guildID));
            while(rs.next()){
                rowCount = rs.getInt(1);
            }

            if(rowCount < Integer.parseInt(Config.get("max_row_count"))){
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }
}
