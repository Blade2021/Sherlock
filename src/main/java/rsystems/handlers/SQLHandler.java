package rsystems.handlers;

import org.mariadb.jdbc.MariaDbPoolDataSource;
import rsystems.Config;
import rsystems.objects.GuildSettings;
import rsystems.objects.InfractionObject;
import rsystems.objects.SelfRole;
import rsystems.objects.TimedEvent;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SQLHandler {

    protected static MariaDbPoolDataSource pool = null;

    public SQLHandler(String URL, String user, String pass) {

        try {
            pool = new MariaDbPoolDataSource(URL);
            pool.setUser(user);
            pool.setPassword(pass);
            pool.setMaxPoolSize(10);
            pool.setMinPoolSize(4);

            pool.initialize();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public SQLHandler(MariaDbPoolDataSource pool) {
        SQLHandler.pool = pool;
    }

    /*

                    GENERALIZED METHODS FOR SQL INTERACTION

     */
    public ArrayList<String> getStringList(String table, String columnName) {
        ArrayList<String> output = new ArrayList<>();

        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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

    public Long getLong(String table, String columnName, String identifierColumn, Long identifier) throws SQLException {
        Long output = null;

        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d",columnName,table,identifierColumn,identifier));

            while (rs.next()) {
                output = rs.getLong(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return output;
    }

    public Long getLong(String table, String columnName, String firstIdentityCol, Long firstId, String secondIdentityCol, int secondId) throws SQLException {
        Long output = null;

        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d AND %s = %d",columnName,table,firstIdentityCol,firstId,secondIdentityCol,secondId));

            while (rs.next()) {
                output = rs.getLong(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return output;
    }

    public int putValue(String tableName, String columnName, String identifierColumn, Long identifier, Long value) {
        int output = 0;
        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = null WHERE %s = %d", tableName, columnName, identifierColumn, identifier));
            output = st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return output;
    }

    public int putValue(String tableName, String columnName, String identifierColumn, Long identifier, int value) throws SQLException {
        int output = 0;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));

            output = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return output;
    }

    public int putValue(String tableName, String columnName, String firstIdentityCol, Long firstId, String secondIdentityCol, int secondId, Long value) throws SQLException {
        int output = 0;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d AND %s = %d", tableName, columnName, value, firstIdentityCol, firstId, secondIdentityCol, secondId));

            output = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return output;
    }

    public int putValue(String tableName, String columnName, String firstIdentityCol, Long firstId, String secondIdentityCol, int secondId, String value) throws SQLException {
        int output = 0;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = \"%s\" WHERE %s = %d AND %s = %d", tableName, columnName, value, firstIdentityCol, firstId, secondIdentityCol, secondId));

            output = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return output;
    }

    public int putValue(String tableName, String columnName, String identifierColumn, Long identifier, String value) {
        int output = 0;
        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();
            st.execute("INSERT INTO WelcomeTable (ChildGuildID) VALUES (" + Long.valueOf(guildID) + ")");
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public GuildSettings getGuildData(Long guildID) throws SQLException {

        GuildSettings guildSettings = new GuildSettings(guildID);

        Connection connection = pool.getConnection();

        try{
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Prefix, OwnerID, LogChannelID, MuteRoleID, EmbedFilter, OwnerID, GrantedSelfRoleCount, GrantedAutoRoleCount, WelcomeMessageSetting FROM Guilds WHERE GuildID = " + guildID);
            while (rs.next()) {
                guildSettings.setPrefix(rs.getString("Prefix"));
                guildSettings.setOwnerID(rs.getLong("OwnerID"));
                guildSettings.setLogChannelID(rs.getLong("LogChannelID"));
                guildSettings.setMuteRoleID(rs.getLong("MuteRoleID"));
                guildSettings.setEmbedFilterSetting(rs.getInt("EmbedFilter"));
                guildSettings.setGrantedSelfRoleCount(rs.getInt("GrantedSelfRoleCount"));
                guildSettings.setGrantedAutoRoleCount(rs.getInt("GrantedAutoRoleCount"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return guildSettings;
    }

    public Integer updateGuild(GuildSettings guildSettings) throws SQLException {

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("UPDATE Guilds SET OwnerID=%d, Prefix=\"%s\", LogChannelID=%d, MuteRoleID=%d, EmbedFilter=%d, WelcomeMessageSetting=%d " +
                    "where GuildID=%d", guildSettings.getOwnerID(),guildSettings.getPrefix(),guildSettings.getLogChannelID(),guildSettings.getMuteRoleID(),
                    guildSettings.getEmbedFilterSetting(),guildSettings.getWelcomeMessageSetting(),guildSettings.getGuildID()));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return 0;
    }

    // Add an self role to the database
    public Integer insertSelfRole(Long guildID, String command, Long roleID) throws SQLException {

        if(checkSize(guildID,"SelfRoles")) {

            Connection connection = pool.getConnection();

            try {
                Statement st = connection.createStatement();

                st.execute(String.format("INSERT INTO SelfRoles (ChildGuildID, RoleCommand, RoleID) VALUES (%d, \"%s\", %d)", guildID, command, roleID));
                return 200;
            } catch (SQLException throwables) {
                System.out.println("SQL Error");
                throwables.printStackTrace();
            } finally {
                connection.close();
            }
        } else {
            //DB Row Max limit reached
            return 201;
        }
        return 0;
    }

    // Remove an self role to the database
    public Integer removeSelfRole(Long guildID, String command) throws SQLException {

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM SelfRoles WHERE (ChildGuildID = %d) AND (RoleCommand = \"%s\")", guildID, command));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return 0;
    }

    public Map<String,Long> getGuildSelfRoles(Long guildID) throws SQLException {

        Connection connection = pool.getConnection();

        Map<String,Long> selfRoleMap = new HashMap<>();
        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleCommand, RoleID FROM SelfRoles WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                selfRoleMap.putIfAbsent(rs.getString("RoleCommand"),rs.getLong("RoleID"));
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }finally {
            connection.close();
        }

        return selfRoleMap;
    }

    //Get self Roles - Called by GuildLoader
    public ArrayList<SelfRole> getSelfRoles(Long guildID) throws SQLException {
        ArrayList<SelfRole> output = new ArrayList<>();
        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleCommand, RoleID FROM SelfRoles WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                output.add(new SelfRole(guildID, rs.getString("RoleCommand"), rs.getLong("RoleID")));
            }

            return output;

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }
        return null;
    }

    public Integer insertCaseEvent(Long guildID, InfractionObject caseObject) throws SQLException {

        int output = 0;
        int eventTypeIdentifier = -1;

        switch(caseObject.getEventType()){
            case WARNING:
                eventTypeIdentifier = 0;
                break;
            case RESERVED:
                eventTypeIdentifier = 1;
                break;
            case MUTE:
                eventTypeIdentifier = 2;
                break;
            case KICK:
                eventTypeIdentifier = 3;
                break;
            case TIMED_BAN:
                eventTypeIdentifier = 4;
                break;
            case BAN:
                eventTypeIdentifier = 5;
                break;
        }


        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("INSERT INTO CaseTable (ChildGuildID,CaseID,SubmissionDate,UserID,UserTag,ModID,ModTag,Reason,LogMessageID,EventType) VALUES (%d,%d,\"%s\",%d,\"%s\",%d,\"%s\",\"%s\",%d,%d)",
                    guildID,
                    caseObject.getCaseNumber(),
                    Timestamp.valueOf(caseObject.getSubmissionDate().toLocalDateTime()),
                    caseObject.getUserID(),
                    caseObject.getUserTag(),
                    caseObject.getModeratorID(),
                    caseObject.getModeratorTag(),
                    caseObject.getNote(),
                    caseObject.getMessageID(),
                    eventTypeIdentifier)
            );

            output = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return output;

    }

    public InfractionObject getCaseEvent(Long guildID, int caseID) throws SQLException {

        int output = 0;
        InfractionObject infractionObject = new InfractionObject(guildID,caseID);

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(String.format("SELECT ChildGuildID, CaseID,SubmissionDate,UserID,UserTag,ModID,ModTag,Reason,LogMessageID,EventType FROM CaseTable WHERE ChildGuildID = %d AND CaseID = %d",guildID,caseID));

            int eventTypeIdentifier = -1;

            while(rs.next()){
                infractionObject.setNote(rs.getString("Reason"));
                infractionObject.setModeratorTag(rs.getString("ModTag"));
                infractionObject.setModeratorID(rs.getLong("ModID"));
                infractionObject.setUserTag(rs.getString("UserTag"));
                infractionObject.setUserID(rs.getLong("UserID"));
                infractionObject.setMessageID(rs.getLong("LogMessageID"));
                eventTypeIdentifier = rs.getInt("EventType");
                infractionObject.setSubmissionDate(ZonedDateTime.ofInstant(rs.getTimestamp("Submission Date").toInstant(), ZoneId.of("UTC")));

                switch(eventTypeIdentifier){
                    case 0:
                        infractionObject.setEventType(InfractionObject.EventType.WARNING);
                        break;
                    case 1:
                        infractionObject.setEventType(InfractionObject.EventType.RESERVED);
                        break;
                    case 2:
                        infractionObject.setEventType(InfractionObject.EventType.MUTE);
                        break;
                    case 3:
                        infractionObject.setEventType(InfractionObject.EventType.KICK);
                        break;
                    case 4:
                        infractionObject.setEventType(InfractionObject.EventType.TIMED_BAN);
                        break;
                    case 5:
                        infractionObject.setEventType(InfractionObject.EventType.BAN);
                        break;
                }

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return infractionObject;

    }

    // Add an self role to the database
    public Integer insertAutoRole(Long guildID, Long roleID) throws SQLException {

        if(checkSize(guildID,"AutoRole")) {

            Connection connection = pool.getConnection();

            try {
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
            } finally {
                connection.close();
            }
        } else {
            //DB Row Max limit reached
            return 201;
        }
        return 0;
    }

    // Remove an self role to the database
    public Integer removeAutoRole(Long guildID, Long roleID) throws SQLException {

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM AutoRole WHERE (ChildGuildID = %d) AND (RoleID = %d)", guildID, roleID));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return 0;
    }

    //Get self Roles - Called by GuildLoader
    public ArrayList<Long> getAutoRoles(Long guildID) throws SQLException{
        ArrayList<Long> output = new ArrayList<>();

        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleID FROM AutoRole WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                output.add(rs.getLong("RoleID"));
            }

            return output;

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }
        return null;
    }

    public boolean insertTimedEvent(Long GuildID, Long UserID, int EventType, String reason, Long EventKey, int EventValue){

        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO ArchiveTable (ChildGuildID, ChannelID, PreviousCategory, PreviousPosition) VALUES (%d, %d, %d, %d)",GuildID,ChannelID,ParentCategory,ChannelPosition));
            return st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();
        }

        return 0;
    }

    public int insertReactionRole(Long guildID, Long messageID, String reactionID, Long roleID){

        try{
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO ReactionTable (ChildGuildID, MessageID, ReactionID, RoleID) VALUES (%d, %d, \"%s\", %d)",guildID, messageID, reactionID, roleID));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public int removeReactionRole(Long guildID, Long messageID, String reactionID){

        try{
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM ReactionTable WHERE (ChildGuildID = %d) AND (MessageID = %d) AND (ReactionID = \"%s\")",guildID, messageID, reactionID));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    /*

    OTHER METHODS....

     */

    public void logError(String guildID, String event){
        try {
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();
            st.execute("INSERT INTO FaultTable (ChildGuildID, Event) VALUES (" + Long.valueOf(guildID) + ", \"" + event + "\")");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void logError(String guildID, String event, int errorCode){
        try {
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();
            st.execute(String.format("INSERT INTO FaultTable (ChildGuildID, Event, ErrorCode) VALUES (%d, \"%s\", %d)",Long.valueOf(guildID),event,errorCode));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void logError(String guildID, String event, Long relateable){
        try {
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();
            st.execute("INSERT INTO FaultTable (ChildGuildID, Event, Relateables) VALUES (" + Long.valueOf(guildID) + ", \"" + event + "\", " + relateable + ")");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean insertInfraction(String guildID, Long violatorID, String violation, Long submitter) {
        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO ModRoleTable (ChildGuildID, ModRoleID, Permissions) VALUES (%d, %d, %d)", guildID, roleID, permissionLevel));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    // INSERT A BADWORD INTO THE DATABASE
    public Map<Long,Integer> getModRoles(Long guildID) {
        Map<Long,Integer> resultSet = new HashMap<>();

        try {
            Connection connection = pool.getConnection();
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT ModRoleID,Permissions FROM ModRoleTable WHERE ChildGuildID = " + guildID);
            while (rs.next()) {
                resultSet.put(rs.getLong("ModRoleID"),rs.getInt("Permissions"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }


    // REMOVE MOD ROLE
    public Integer removeModRole(Long guildID, Long roleID) {
        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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

    public int getTableCount(Long guildID, String tableName) throws SQLException {
        int rowCount = 0;

        Connection connection = pool.getConnection();

        try {
            //Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Count(ChildGuildID) from %s where ChildGuildID = %d", tableName, guildID));
            while(rs.next()){
                rowCount = rs.getInt(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return rowCount;
    }

    public Integer insertAutoTriggerDelete(Long guildID, Long triggerMessageID, Long responseMessageID) {
        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM %s WHERE %s = %d", TableName, IdentifierColumn, Identifier));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public Integer deleteRow(String TableName, String IdentifierColumn, String Identifier) {
        try {
            Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM %s WHERE %s = \"%s\"", TableName, IdentifierColumn, Identifier));
            return st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Long> archiveChannelList(Long categoryID, Long guildID){
        ArrayList<Long> output = new ArrayList<>();

        try {
            Connection connection = pool.getConnection();

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
            Connection connection = pool.getConnection();

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
