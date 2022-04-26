package rsystems.handlers;

import org.mariadb.jdbc.MariaDbPoolDataSource;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.objects.GuildSettings;
import rsystems.objects.InfractionObject;
import rsystems.objects.TimedEvent;
import rsystems.objects.TrackerObject;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLHandler {

    protected static MariaDbPoolDataSource pool = null;

    public SQLHandler(MariaDbPoolDataSource pool) {
        SQLHandler.pool = pool;
    }

    /*
    GETTERS
     */

    public String getString(String table, String columnName, String identifierColumn, Long identifier) throws SQLException {
        String returnValue = null;

        Connection connection = pool.getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s = %d", columnName, table, identifierColumn, identifier));

            while (rs.next()) {
                returnValue = rs.getString(columnName.toUpperCase());
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public String getString(String table, String columnName, String identifierColumn, String identifier) throws SQLException {
        String returnValue = null;

        Connection connection = pool.getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s = '%s'", columnName, table, identifierColumn, identifier));

            while (rs.next()) {
                returnValue = rs.getString(columnName);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public Integer getInt(String table, String columnName, String identifierColumn, Long identifier) throws SQLException {
        Integer returnValue = null;

        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d", columnName, table, identifierColumn, identifier));

            while (rs.next()) {
                returnValue = rs.getInt(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return returnValue;
    }

    /**
     * @param table
     * @param columnName
     * @param identifierColumn
     * @param identifier
     * @return Null or Long of found value
     * @throws SQLException
     */
    public Long getLong(String table, String columnName, String identifierColumn, Long identifier) throws SQLException {
        Long returnValue = null;

        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d", columnName, table, identifierColumn, identifier));

            while (rs.next()) {
                returnValue = rs.getLong(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public Long getLong(String table, String columnName, String firstIdentityCol, Long firstId, String secondIdentityCol, int secondId) throws SQLException {
        Long returnValue = null;

        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d AND %s = %d", columnName, table, firstIdentityCol, firstId, secondIdentityCol, secondId));

            while (rs.next()) {
                returnValue = rs.getLong(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public Long getLong(String table, String columnName, String firstIdentityCol, Long firstId, String secondIdentityCol, Long secondId) throws SQLException {
        Long returnValue = null;
        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d AND %s = %d", columnName, table, firstIdentityCol, firstId, secondIdentityCol, secondId));

            while (rs.next()) {
                returnValue = rs.getLong(columnName);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public List<Long> getLongMultiple(String table, String columnName, String identifierColumn, Long identifier) throws SQLException {
        ArrayList<Long> returnValue = new ArrayList<>();

        Connection connection = pool.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s where %s = %d", columnName, table, identifierColumn, identifier));

            while (rs.next()) {
                returnValue.add(rs.getLong(columnName));
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public Integer putValue(String tableName, String columnName, String identifierColumn, Long identifier, Long value) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));
            returnValue = st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer putValue(String tableName, String columnName, String identifierColumn, Long identifier, String value) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = '%s' WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));
            returnValue = st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    /**
     * @param tableName
     * @param columnName
     * @param identifierColumn
     * @param identifier
     * @param value
     * @return
     */
    public Integer putValue(String tableName, String columnName, String identifierColumn, int identifier, Long value) throws SQLException {
        int returnValue = 0;
        Connection connection = pool.getConnection();

        try {

            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));
            returnValue = st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer putValueNull(String tableName, String columnName, String identifierColumn, Long identifier) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = null WHERE %s = %d", tableName, columnName, identifierColumn, identifier));
            returnValue = st.getUpdateCount();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer putValue(String tableName, String columnName, String identifierColumn, Long identifier, int value) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d", tableName, columnName, value, identifierColumn, identifier));

            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer putValue(String tableName, String columnName, String firstIdentityCol, Long firstId, String secondIdentityCol, int secondId, Long value) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = %d WHERE %s = %d AND %s = %d", tableName, columnName, value, firstIdentityCol, firstId, secondIdentityCol, secondId));

            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer putValue(String tableName, String columnName, String firstIdentityCol, Long firstId, String secondIdentityCol, int secondId, String value) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("UPDATE %s SET %s = \"%s\" WHERE %s = %d AND %s = %d", tableName, columnName, value, firstIdentityCol, firstId, secondIdentityCol, secondId));

            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public void addWelcomeRow(Long guildID, String message) throws SQLException {

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();
            st.execute(String.format("INSERT INTO WelcomeTable (ChildGuildID, WelcomeMessage) VALUES (%d, '%s')", guildID, message));
            //return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            //return false;
        } finally {
            connection.close();
        }
    }

    public GuildSettings getGuildData(Long guildID) throws SQLException {

        GuildSettings guildSettings = new GuildSettings(guildID);

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Prefix, OwnerID, LogChannelID, QuarantineRoleID, EmbedFilter, OwnerID, GrantedSelfRoleCount, GrantedAutoRoleCount, WelcomeMessageSetting, InviteFilterEnabled FROM GuildTable WHERE GuildID = " + guildID);
            while (rs.next()) {
                guildSettings.setPrefix(rs.getString("Prefix"));
                guildSettings.setOwnerID(rs.getLong("OwnerID"));
                guildSettings.setLogChannelID(rs.getLong("LogChannelID"));
                guildSettings.setQuarantineRoleID(rs.getLong("QuarantineRoleID"));
                guildSettings.setEmbedFilterSetting(rs.getInt("EmbedFilter"));
                guildSettings.setGrantedSelfRoleCount(rs.getInt("GrantedSelfRoleCount"));
                guildSettings.setGrantedAutoRoleCount(rs.getInt("GrantedAutoRoleCount"));
                guildSettings.setWelcomeMessageSetting(rs.getInt("WelcomeMessageSetting"));
                guildSettings.setInviteFilterEnabled(rs.getInt("InviteFilterEnabled"));
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
        Integer returnValue = null;

        try {
            Statement st = connection.createStatement();

            StringBuilder updateString = new StringBuilder();
            updateString.append("UPDATE GuildTable SET ");

            if(guildSettings.getOwnerID() != null){
                updateString.append(String.format("OwnerID=%d, ", guildSettings.getOwnerID()));
            }

            if(guildSettings.getPrefix() != null){
                updateString.append(String.format("Prefix='%s', ", guildSettings.getPrefix()));
            } else {
                updateString.append(" Prefix=NULl, ");
            }

            st.execute(String.format("%s LogChannelID=%d, QuarantineRoleID=%d, EmbedFilter=%d, WelcomeMessageSetting=%d, InviteFilterEnabled=%d " +
                            "where GuildID=%d", updateString, guildSettings.getLogChannelID(), guildSettings.getQuarantineRoleID(),
                    guildSettings.getEmbedFilterSetting(), guildSettings.getWelcomeMessageSetting(),guildSettings.isInviteFilterEnabled(), guildSettings.getGuildID()));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer insertGuild(Long guildID, Long guildOwnerID) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = null;

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO GuildTable SET GuildID=%d, OwnerID=%d", guildID, guildOwnerID));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer insertCaseEvent(Long guildID, InfractionObject caseObject) throws SQLException {

        int returnValue = 0;
        int eventTypeIdentifier = -1;

        switch (caseObject.getEventType()) {
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

            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;

    }

    public Integer insertBanEvent(final Long guildID, final Long bannedUserID, final Long moderatorID, final Long repliedMessageID) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();
            st.execute(String.format("INSERT INTO BanTable (ChildGuildID,BannedUserID,ModeratorID,ReplyMessageID) VALUES (%d, %d, %d, %d)",
                    guildID, bannedUserID, moderatorID, repliedMessageID
            ));

            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    /*
     *******************************************
     ************** AUTO ROLES *****************
     *******************************************
     */

    // Add an self role to the database
    public Integer insertAutoRole(Long guildID, Long roleID) throws SQLException {
        
        Integer returnValue = 0;

        if (checkSize(guildID, "AutoRoles")) {

            Connection connection = pool.getConnection();

            try {
                Statement st = connection.createStatement();

                ResultSet rs = st.executeQuery(String.format("SELECT RoleID FROM AutoRoles WHERE ChildGuildID = %d AND RoleID = %d", guildID, roleID));
                int results = 0;
                while (rs.next()) {
                    results++;
                }
                if (results > 0) {
                    returnValue = 400;
                } else {
                    st.execute(String.format("INSERT INTO AutoRoles (ChildGuildID, RoleID) VALUES (%d, %d)", guildID, roleID));
                    returnValue = 200;
                }
            } catch (SQLException throwables) {
                returnValue = 0;
            } finally {
                connection.close();
            }
        } else {
            //DB Row Max limit reached
            returnValue = 201;
        }
        return returnValue;
    }

    // Remove an self role to the database
    public Integer removeAutoRole(Long guildID, Long roleID) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = 0;

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM AutoRoles WHERE (ChildGuildID = %d) AND (RoleID = %d)", guildID, roleID));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    //Get self Roles - Called by GuildLoader
    public ArrayList<Long> getAutoRoles(Long guildID) throws SQLException {
        ArrayList<Long> returnValue = new ArrayList<>();
        Connection connection = pool.getConnection();

        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleID FROM AutoRoles WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                Long nextInput = rs.getLong("RoleID");
                returnValue.add(nextInput);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }
        return returnValue;
    }


    /*
     *******************************************
     ************** SELF ROLES *****************
     *******************************************
     */

    /**
     * Add a self role to the database
     *
     * @param guildID The guild to be referenced
     * @param command The trigger for the self role
     * @param roleID  The ID of the Role
     * @return <p>200 - OK</p>
     * <p>201 - Trigger already in use</p>
     * <p>202 - Max count reached</p>
     * <p>400 - Database unresponsive</p>
     * @throws SQLException
     */
    public Integer insertSelfRole(Long guildID, String command, Long roleID) throws SQLException {

        Integer returnValue = 0;

        if(getString("SelfRoles","RoleCommand","RoleCommand",command) != null){
            returnValue = 201;
        } else {

            Connection connection = pool.getConnection();

            try {
                Statement st = connection.createStatement();
                st.execute(String.format("INSERT INTO SelfRoles (ChildGuildID, RoleCommand, RoleID) VALUES (%d, \"%s\", %d)", guildID, command, roleID));

                if (st.getUpdateCount() > 0) {
                    returnValue = 200;
                } else {
                    returnValue = 201;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                returnValue = 400;
            } finally {
                connection.close();
            }
        }

        return returnValue;
    }

    // Remove an self role to the database
    public Integer removeSelfRole(Long guildID, String command) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = 0;

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM SelfRoles WHERE (ChildGuildID = %d) AND (RoleCommand = \"%s\")", guildID, command));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Map<String, Long> getGuildSelfRoles(Long guildID) throws SQLException {

        Connection connection = pool.getConnection();

        Map<String, Long> selfRoleMap = new HashMap<>();
        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT RoleCommand, RoleID FROM SelfRoles WHERE ChildGuildID = " + guildID);

            while (rs.next()) {
                selfRoleMap.putIfAbsent(rs.getString("RoleCommand"), rs.getLong("RoleID"));
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        } finally {
            connection.close();
        }

        return selfRoleMap;
    }


    /*
     *******************************************
     ************** OTHER FUNCTIONS *****************
     *******************************************
     */

    public ArrayList<TimedEvent> timedEventList() throws SQLException {
        ArrayList<TimedEvent> events = new ArrayList<>();

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT EventType, ChildGuildID, EventID, EndDate FROM TimedEvents WHERE Expired = 0");
            while (rs.next()) {
                if (!rs.getString("EndDate").equalsIgnoreCase("0000-00-00 00:00:00")) {
                    events.add(new TimedEvent(rs.getInt("EventType"), rs.getLong("ChildGuildID"), rs.getLong("EventID"), rs.getString("EndDate")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throwables.getErrorCode();

        } finally {
            connection.close();
        }

        return events;
    }

    public boolean expireTimedEvent(Long GuildID, Long UserID) {

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


    public Integer handleIgnoreChannel(Long GuildID, Long ChannelID, Boolean AddChannel) throws SQLException {
        int returnValue = 0;

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            Long channelFound = this.getLong("IgnoreChannelTable", "ChannelID", "ChannelID", ChannelID);

            //ResultSet rs = st.executeQuery(String.format("SELECT %s FROM %s where %s = %d",columnName,table,identifierColumn,identifier)); ")

            if ((channelFound != null) && (!AddChannel)) {
                st.execute(String.format("DELETE FROM IgnoreChannelTable WHERE ChildGuildID = %d AND ChannelID = %d", GuildID, ChannelID));
            } else if ((channelFound == null) && (AddChannel)) {
                st.execute(String.format("INSERT INTO IgnoreChannelTable (ChildGuildID,ChannelID) VALUES (%d,%d)", GuildID, ChannelID));
            }

            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public ArrayList<String> getSoftFilteredWords(Long guildID) throws SQLException {
        ArrayList<String> badWordsList = new ArrayList<>();
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT Word FROM LanguageFilter WHERE ChildGuildID = " + guildID);
            while (rs.next()) {
                badWordsList.add(rs.getString("Word"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return badWordsList;
    }

    public Map<String, Integer> getHardFilteredWords(Long guildID) throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Word, Resolution FROM HardFilter WHERE ChildGuildID = %d",guildID));
            while (rs.next()) {

                map.putIfAbsent(rs.getString("Word"),rs.getInt("Resolution"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return map;
    }

    public ArrayList<String> getList(String tableName,String columnName) throws SQLException {
        ArrayList<String> list = new ArrayList<>();
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT %s FROM %s",columnName,tableName));
            while (rs.next()) {
                list.add(rs.getString(columnName));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return list;
    }

    public ArrayList<String> getList(Long guildID,String tableName,String columnName) throws SQLException {
        ArrayList<String> list = new ArrayList<>();
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT %s FROM %s WHERE ChildGuildID = %d",columnName,tableName,guildID));
            while (rs.next()) {
                list.add(rs.getString(columnName));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return list;
    }

    public ArrayList<Long> getListLong(Long guildID,String tableName,String columnName) throws SQLException {
        ArrayList<Long> list = new ArrayList<>();
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT %s FROM %s WHERE ChildGuildID = %d",columnName,tableName,guildID));
            while (rs.next()) {
                list.add(rs.getLong(columnName));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return list;
    }

    public Map<Long, String> getMap(Long guildID,String tableName,String firstColumn,String secondColumn) throws SQLException {
        Map<Long, String> map = new HashMap<>();
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT %s, %s FROM %s WHERE ChildGuildID = %d",firstColumn,secondColumn,tableName,guildID));
            while (rs.next()) {

                map.putIfAbsent(rs.getLong(firstColumn),rs.getString(secondColumn));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return map;
    }

    // INSERT A BADWORD INTO THE DATABASE
    public Integer addHardFilterWord(Long guildID, String filterWord, Integer resolution) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = 0;
        try {

            Statement st = connection.createStatement();

            if(resolution > 3){
                resolution = 3;
            }

            st.execute(String.format("INSERT INTO HardFilter (ChildGuildID, Word, Resolution) VALUES (%d, \"%s\", %d)", guildID, filterWord, resolution));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    // REMOVE A BLACKLISTED WORD FROM THE DATABASE
    public Integer removeHardFilterWord(Long guildID, String word) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = 0;

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM HardFilter WHERE (ChildGuildID = %d) AND (Word = \"%s\")", guildID, word));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    // INSERT A BADWORD INTO THE DATABASE
    public Integer addSoftFilterWord(Long guildID, String filterWord) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = null;
        try {

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO LanguageFilter (ChildGuildID, Word) VALUES (%d, \"%s\")", guildID, filterWord));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    // REMOVE A BLACKLISTED WORD FROM THE DATABASE
    public Integer removeSoftFilterWord(Long guildID, String word) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = null;

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM LanguageFilter WHERE (ChildGuildID = %d) AND (Word = \"%s\")", guildID, word));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    // INSERT A CHANNEL ID INTO THE DATABASE
    public Integer insertAutoPushChannel(Long guildID, Long channelID) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = 0;
        try {

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO AutoPush (ChildGuildID, ChannelID) VALUES (%d, %d)", guildID, channelID));
            returnValue = st.getUpdateCount();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    // REMOVE A CHANNEL ID FROM THE DATABASE
    public Integer removeAutoPushChannel(Long guildID, Long channelID) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = null;

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM AutoPush WHERE (ChildGuildID = %d) AND (ChannelID = %d)", guildID, channelID));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    // CHECK FOR CHANNEL ID IN THE DATABASE

    /**
     * Checks the database for a given channelID to see if it should automatically push any announcements
     * @param guildID
     * @param channelID
     * @return Channel was found
     * @throws SQLException
     */
    public Boolean checkForChannelID(final Long guildID, final Long channelID) throws SQLException {

        Connection connection = pool.getConnection();
        int returnCount = 0;

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT ChannelID FROM AutoPush WHERE (ChildGuildID = %d) AND (ChannelID = %d)", guildID, channelID));
            while (rs.next()) {
                returnCount++;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        if(returnCount > 0){
            return true;
        } else {
            return false;
        }
    }


    public Integer whiteListServer(Long guildID, Long serverID, String note) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = null;

        try {

            Statement st = connection.createStatement();

            st.execute(String.format("INSERT INTO InviteWhitelist (ChildGuildID, TargetGuildID, Note) VALUES (%d, %d, '%s')", guildID, serverID, note));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    // REMOVE A BLACKLISTED WORD FROM THE DATABASE
    public Integer deWhiteListServer(Long guildID, Long serverID) throws SQLException {

        Connection connection = pool.getConnection();
        Integer returnValue = null;

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM InviteWhitelist WHERE (ChildGuildID = %d) AND (TargetGuildID = %d)", guildID, serverID));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    private boolean checkSize(Long guildID, String tableName) throws SQLException {
        int rowCount = 0;

        Connection connection = pool.getConnection();
        Boolean returnValue = false;

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Count(ChildGuildID) from %s where ChildGuildID = %d", tableName, guildID));
            while (rs.next()) {
                rowCount = rs.getInt(1);
            }

            if (rowCount < Integer.parseInt(Config.get("max_row_count"))) {
                returnValue = true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return returnValue;
    }

    private boolean checkSize(Long guildID, String tableName, Integer max) throws SQLException {
        int rowCount = 0;
        boolean returnValue = false;

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Count(ChildGuildID) from %s where ChildGuildID = %d", tableName, guildID));
            while (rs.next()) {
                rowCount = rs.getInt(1);
            }

            if (rowCount < max) {
                returnValue = true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public int getTableCount(Long guildID, String tableName) throws SQLException {
        int rowCount = 0;

        Connection connection = pool.getConnection();

        try {
            //Connection connection = pool.getConnection();

            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT Count(ChildGuildID) from %s where ChildGuildID = %d", tableName, guildID));
            while (rs.next()) {
                rowCount = rs.getInt(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return rowCount;
    }

    public Integer deleteRow(String TableName, String IdentifierColumn, Long Identifier) throws SQLException {

        Integer returnValue = null;
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM %s WHERE %s = %d", TableName, IdentifierColumn, Identifier));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public Integer deleteRow(String TableName, String FirstIdentifierColumn, Long FirstIdentifier, String SecondIdentifierColumn, Long SecondIdentifier) throws SQLException {

        Integer returnValue = null;
        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            st.execute(String.format("DELETE FROM %s WHERE %s = %d AND %s = %d", TableName, FirstIdentifierColumn, FirstIdentifier, SecondIdentifierColumn, SecondIdentifier));
            returnValue = st.getUpdateCount();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return returnValue;
    }

    public ArrayList<TrackerObject> getTrackers(final Long guildID, final Long userID) throws SQLException {
        ArrayList<TrackerObject> trackers = new ArrayList<>();

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT StartDateTime, ExpireDatetime, Type FROM Tracker where ChildGuildID = %d AND UserID = %d", guildID, userID));
            while (rs.next()) {
                trackers.add(new TrackerObject(rs.getTimestamp("StartDateTime").toLocalDateTime(), rs.getTimestamp("ExpireDateTime").toLocalDateTime(), rs.getInt("Type")));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return trackers;
    }

    public void insertTracker(final Long guildID, final Long userID, final Integer type, final Integer expireHoursOffset, final String note) throws SQLException {

        final LocalDateTime currentDateTime = LocalDateTime.now();
        final LocalDateTime expireDateTime = currentDateTime.plusHours(expireHoursOffset);

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            st.executeUpdate(String.format("INSERT INTO Tracker (ChildGuildID, UserID, StartDateTime, ExpireDateTime, Type, Note) VALUES (%d, %d, '%s', '%s', %d, '%s')", guildID, userID, currentDateTime, expireDateTime, type, note));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

    }

    public void checkForExpiredTrackers() throws SQLException {

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            st.executeUpdate("DELETE FROM Tracker WHERE ExpireDatetime < (DATE_SUB(NOW(), INTERVAL 1 HOUR))");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

    }

    public Integer submitModeratorRole(final Long guildID, final Long roleID, final Integer permLevel) throws SQLException {
        Integer returnValue = 0;

        if (this.getInt("ModRoleTable", "Permissions", "ModRoleID", roleID) == null) {

            Connection connection = pool.getConnection();
            try {

                Statement st = connection.createStatement();

                st.executeUpdate(String.format("INSERT INTO ModRoleTable (ChildGuildID, ModRoleID, Permissions) VALUES (%d, %d, %d)", guildID, roleID, permLevel));
                returnValue = st.getUpdateCount();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.close();
            }
        }
        return returnValue;
    }

    public Map<Long, Integer> getModRoles(Long guildID) throws SQLException {
        Map<Long, Integer> resultSet = new HashMap<>();

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery("SELECT ModRoleID,Permissions FROM ModRoleTable WHERE ChildGuildID = " + guildID);
            while (rs.next()) {
                resultSet.put(rs.getLong("ModRoleID"), rs.getInt("Permissions"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
        return resultSet;
    }

    public String nextActivity(Integer currentIndex) throws SQLException {
        String returnValue = null;

        Connection connection = pool.getConnection();
        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT ID, Activity FROM ActivityList WHERE ID > %d LIMIT 1",currentIndex));
            while(rs.next()){
                returnValue = rs.getString("Activity");
                SherlockBot.activityIndex = rs.getInt("ID");
            }

            if(returnValue == null){
                rs = st.executeQuery("SELECT ID, Activity FROM ActivityList ORDER BY ID");
                while(rs.next()){
                    returnValue = rs.getString("Activity");
                    SherlockBot.activityIndex = rs.getInt("ID");
                    break;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }

        return returnValue;
    }

    public void logCommandUsage(String commandName) throws SQLException {

        Connection connection = pool.getConnection();

        try {
            Statement st = connection.createStatement();

            ResultSet rs = st.executeQuery(String.format("SELECT UsageCount FROM CommandTracker WHERE Name = \"%s\"", commandName));

            boolean commandFound = false;
            while (rs.next()) {
                commandFound = true;
                int newCount = rs.getInt(1) + 1;

                st.execute(String.format("UPDATE CommandTracker SET UsageCount = %d, LastUsed = current_timestamp WHERE Name = \"%s\"", newCount, commandName));
            }
            if (!commandFound) {
                st.execute(String.format("INSERT INTO CommandTracker (Name, UsageCount) VALUES (\"%s\", 1)", commandName));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            connection.close();
        }
    }


}
