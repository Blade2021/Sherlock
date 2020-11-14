package rsystems.handlers;

import org.mariadb.jdbc.MariaDbConnection;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.objects.InfractionObject;
import rsystems.objects.SelfRole;
import rsystems.objects.TimedEvent;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLHandler {

    protected static Connection connection = null;
    protected static MariaDbConnection connection1 = null;
    protected static MariaDbPoolDataSource dataSource = null;
    private final String DatabaseURL;
    private final String DatabaseUser;
    private final String DatabaseUserPass;

    public SQLHandler(String DatabaseURL, String DatabaseUser, String DatabaseUserPass) {
        this.DatabaseURL = DatabaseURL;
        this.DatabaseUser = DatabaseUser;
        this.DatabaseUserPass = DatabaseUserPass;

        //dataSource = new MariaDbPoolDataSource(String.format("%s?user=%s&password=%s&maxPoolSize=10",DatabaseURL,DatabaseUser,DatabaseUserPass));
        connect();
    }

    /*

                    GENERALIZED METHODS FOR SQL INTERACTION

     */
    public void connect() {
        try {

            connection = DriverManager.getConnection(DatabaseURL, DatabaseUser, DatabaseUserPass);
            //dataSource.getConnection();

            while(connection.isClosed()){
                try {
                    Thread.sleep(100);
                }catch(InterruptedException e){
                    //do nothing
                }
            }
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
        }

        return output;
    }

    public Long getLong(String table, String columnName, String identifierColumn, Long identifier) {
        Long output = null;

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d",columnName,table,identifierColumn,identifier));

            while (rs.next()) {
                output = rs.getLong(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
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

    public int putValueNull(String tableName, String columnName, String identifierColumn, Long identifier) {
        int output = 0;
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = null WHERE %s = %d", tableName, columnName, identifierColumn, identifier));
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
                Thread.sleep(1000);
            }

            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = \"%s\" WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));
            output = st.getUpdateCount();

        } catch (SQLException | InterruptedException throwables) {
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
            st.execute(String.format("INSERT INTO %s (ChildGuildID, Event, ReceivingUserID, SendingUserID) VALUES (%d %s, %d, %d)",tableName, Long.valueOf(GuildID),event,senderColumn,receiverColumn));
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
                    GRAB SELF ROLES FROM DATABASE
             */
            ArrayList<SelfRole> selfRoles;
            selfRoles = getSelfRoles(Long.valueOf(guildID));
            for (SelfRole role : selfRoles) {
                SherlockBot.guildMap.get(guildID).selfRoleMap.put(role.command, role.RoleID);
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
                //System.out.println(rowsFound);
            }

            if(rowsFound == 0){
                System.out.println("Adding welcome row to GuildTable");
                addWelcomeRow(guildID);
            } else {

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

            }

            rs = st.executeQuery("SELECT ModRoleID, Permissions FROM ModRoleTable WHERE ChildGuildID = " + Long.valueOf(guildID));
            while(rs.next()){
                SherlockBot.guildMap.get(guildID).addModRole(String.valueOf(rs.getLong("ModRoleID")),rs.getInt("Permissions"));
            }

            rs = st.executeQuery("SELECT ChannelID, Type FROM ExceptionTable WHERE ChildGuildID = " + Long.valueOf(guildID));
            while(rs.next()){
                SherlockBot.guildMap.get(guildID).addChannelException(String.valueOf(rs.getLong("ChannelID")),rs.getInt("Type"));
            }


            rs = st.executeQuery("SELECT ArchiveCat FROM GuildTable WHERE GuildID = " + Long.valueOf(guildID));
            while(rs.next()){
                SherlockBot.guildMap.get(guildID).setArchiveCategory(rs.getLong("ArchiveCat"));
            }

            System.out.println("Guild data loaded | GuildID" + guildID);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    // Add an self role to the database
    public Integer insertSelfRole(Long guildID, String command, Long roleID) {

        if(checkSize(guildID,"SelfRoles")) {

            try {
                if ((connection == null) || (connection.isClosed())) {
                    connect();
                }

                Statement st = connection.createStatement();

                st.execute(String.format("INSERT INTO SelfRoles (ChildGuildID, RoleCommand, RoleID) VALUES (%d, \"%s\", %d)", guildID, command, roleID));
                return 200;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            //DB Row Max limit reached
            return 201;
        }
        return 0;
    }

    // Remove an self role to the database
    public Integer removeSelfRole(Long guildID, String command) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM SelfRoles WHERE (ChildGuildID = %d) AND (RoleCommand = \"%s\")", guildID, command));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    //Get self Roles - Called by GuildLoader
    public ArrayList<SelfRole> getSelfRoles(Long guildID) {
        ArrayList<SelfRole> output = new ArrayList<>();
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleCommand, RoleID FROM SelfRoles WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                output.add(new SelfRole(guildID, rs.getString("RoleCommand"), rs.getLong("RoleID")));
            }

            return output;

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return null;
    }

    // Add an self role to the database
    public Integer insertAutoRole(Long guildID, Long roleID) {

        if(checkSize(guildID,"AutoRole")) {

            try {
                if ((connection == null) || (connection.isClosed())) {
                    connect();
                }

                Statement st = connection.createStatement();

                ResultSet rs = st.executeQuery(String.format("SELECT RoleID FROM AutoRole WHERE ChildGuildID = %d AND RoleID = %d", guildID, roleID));
                int results = 0;
                while(rs.next()){
                    results++;
                }
                if(results > 0) {
                    return 400;
                } else {
                    st.execute(String.format("INSERT INTO AutoRole (ChildGuildID, RoleID) VALUES (%d, %d)", guildID, roleID));
                    return 200;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            //DB Row Max limit reached
            return 201;
        }
        return 0;
    }

    // Remove an self role to the database
    public Integer removeAutoRole(Long guildID, Long roleID) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM AutoRole WHERE (ChildGuildID = %d) AND (RoleID = %d)", guildID, roleID));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    //Get self Roles - Called by GuildLoader
    public ArrayList<Long> getAutoRoles(Long guildID) {
        ArrayList<Long> output = new ArrayList<>();
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleID FROM AutoRole WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                output.add(rs.getLong("RoleID"));
            }

            return output;

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return null;
    }

    public boolean insertTimedEvent(Long GuildID, Long UserID, int EventType, String reason, Long EventKey, int EventValue){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO TimedEvents (ChildGuildID, EventID, EventType, Reason, Event_SubKey, Event_SubValue) VALUES (%d, %d, %d, \"%s\", %d, %d)", GuildID, UserID, EventType, reason, EventKey, EventValue));
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return false;
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

    public ArrayList<TimedEvent> timedEventList(){
        ArrayList<TimedEvent> events = new ArrayList<>();

        try{
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT EventType, ChildGuildID, EventID, EndDate FROM TimedEvents WHERE Expired = 0");
            while(rs.next()){
                if(!rs.getString("EndDate").equalsIgnoreCase("0000-00-00 00:00:00")){
                    events.add(new TimedEvent(rs.getInt("EventType"),rs.getLong("ChildGuildID"),rs.getLong("EventID"),rs.getString("EndDate")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();

        }

        return events;
    }

    public boolean expireTimedEvent(Long GuildID, Long UserID){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.executeQuery(String.format("UPDATE TimedEvents set Expired = 1 WHERE (ChildGuildID = %d) and (EventID = %d)", GuildID, UserID));
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return false;
    }

    public HashMap<Long,Integer> retractEvent(Long GuildID, Long EventID){
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

    public int setArchiveCategory(Long GuildID, Long CategoryID){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("UPDATE GuildTable SET ArchiveCat = %d WHERE GuildID = %d",CategoryID, GuildID));

            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return 0;
    }

    public Long getArchiveCategory(Long GuildID){
        Long output = null;
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT ArchiveCat FROM GuildTable WHERE GuildID = %d",GuildID));
            while(rs.next()){
                output = rs.getLong("ArchiveCat");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return output;
    }

    public int storeArchiveChannel(Long GuildID, Long ChannelID, Long ParentCategory, int ChannelPosition){

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO ArchiveTable (ChildGuildID, ChannelID, PreviousCategory, PreviousPosition) VALUES (%d, %d, %d, %d)",GuildID,ChannelID,ParentCategory,ChannelPosition));
            return st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return 0;
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

    public ArrayList<InfractionObject> getInfractionList(String guildID, String violatorID) {
        ArrayList<InfractionObject> infractionObjects = new ArrayList<>();

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Violation, UserNote, DateSubmitted, Submitter FROM InfractionTable WHERE ChildGuildID = %d AND ViolatorID = %d order by DateSubmitted desc LIMIT 10",Long.valueOf(guildID),Long.valueOf(violatorID)));
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

    // INSERT A BADWORD INTO THE DATABASE
    public Integer insertModRole(Long guildID, Long roleID, int permissionLevel) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO ModRoleTable (ChildGuildID, ModRoleID, Permissions) VALUES (%d, %d, %d)", guildID, roleID, permissionLevel));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }


    // REMOVE MOD ROLE
    public Integer removeModRole(Long guildID, Long roleID) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM ModRoleTable WHERE (ChildGuildID = %d) AND (ModRoleID = %d)", guildID, roleID));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    // INSERT A CHANNEL EXCEPTION INTO DATABASE
    public Integer insertException(Long guildID, Long channelID, int type) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO ExceptionTable (ChildGuildID, ChannelID, Type) VALUES (%d, %d, %d)", guildID, channelID, type));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    // REMOVE MOD ROLE
    public Integer removeException(Long guildID, Long channelID) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM ExceptionTable WHERE (ChildGuildID = %d) AND (ChannelID = %d)", guildID, channelID));
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

    public Integer insertAutoTriggerDelete(Long guildID, Long triggerMessageID, Long responseMessageID) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO TriggerTable (ChildGuildID, TriggerMessageID, ResponseMessageID) VALUES (%d, %d, %d)", guildID, triggerMessageID, responseMessageID));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public Integer deleteRow(String TableName, String IdentifierColumn, Long Identifier) {
        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM %s WHERE %s = %d", TableName, IdentifierColumn, Identifier));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Long> archiveChannelList(Long categoryID, Long guildID){
        ArrayList<Long> output = new ArrayList<>();

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT ChannelID FROM ArchiveTable WHERE PreviousCategory = %d AND ChildGuildID = %d", categoryID, guildID));
            while(rs.next()){
                output.add(rs.getLong("ChannelID"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return output;
    }


    public Long triggerMessageLookup(Long guildID, Long messageID){
        Long output = null;

        try {
            if ((connection == null) || (connection.isClosed())) {
                connect();
            }

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT ResponseMessageID FROM TriggerTable WHERE ChildGuildID = %d AND TriggerMessageID = %d",guildID, messageID));
            while(rs.next()){
                output = rs.getLong("ResponseMessageID");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return output;
    }
}
