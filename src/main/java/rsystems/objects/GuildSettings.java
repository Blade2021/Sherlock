package rsystems.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuildSettings {
    private String prefix;
    //private ArrayList<String> modRoles = new ArrayList<>();
    public String logChannelID;
    public int embedFilter = 0;
    public Map<String, Long> selfRoleMap = new HashMap<>();
    public Map<String, Integer> modRoleMap = new HashMap<>();
    public Map<String, Integer> exceptionMap = new HashMap<>();
    private ArrayList<String> blacklistedWords = new ArrayList<>();

    private Long welcomeChannelID;
    private String welcomeMessage;
    private int welcomeMethod;
    private int welcomeMessageTimeout;

    //Guild IDs
    private String muteRoleID;

    // CONSTRUCTOR
    public GuildSettings(String prefix) {
        this.prefix = prefix;
    }

    public String getLogChannelID() {
        return logChannelID;
    }

    public void setLogChannelID(String logChannelID) {
        this.logChannelID = logChannelID;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix(){
        return this.prefix;
    }

    public String getMuteRoleID() {
        return muteRoleID;
    }

    public void setMuteRoleID(String muteRoleID) {
        this.muteRoleID = muteRoleID;
    }

    public int getEmbedFilter() {
        return embedFilter;
    }

    public void setEmbedFilter(int embedFilter) {
        this.embedFilter = embedFilter;
    }


    public void addSelfRole(String roleCommand, Long roleID){
        selfRoleMap.putIfAbsent(roleCommand,roleID);
    }

    public void removeSelfRole(String roleCommand){
        selfRoleMap.remove(roleCommand);
    }

    public void addModRole(String modRoleID, int permissionLevel){
        modRoleMap.putIfAbsent(modRoleID,permissionLevel);
    }

    public void removeModRole(String roleCommand){
        modRoleMap.remove(roleCommand);
    }

    public int getModPermissionLevel(String modRoleID){
        return modRoleMap.get(modRoleID);
    }

    public boolean putModPermissionLevel(String modRoleID, int value){
        if(this.modRoleMap.containsKey(modRoleID)){
            this.modRoleMap.put(modRoleID,value);
        } else {
            modRoleMap.putIfAbsent(modRoleID,value);
        }
        return true;
    }

    public void addChannelException(String channelID, int exceptionValue){
        exceptionMap.putIfAbsent(channelID,exceptionValue);
    }

    public void removeChannelException(String channelID){
        exceptionMap.remove(channelID);
    }

    public int getChannelException(String channelID){
        return exceptionMap.get(channelID);
    }


    public ArrayList<String> getBlacklistedWords() {
        return blacklistedWords;
    }

    public void setBlacklistedWords(ArrayList<String> blacklistedWords) {
        this.blacklistedWords = blacklistedWords;
    }

    public void addBadWord(String word){
        this.blacklistedWords.add(word);
    }

    public void removeBadWord(String word){
        this.blacklistedWords.remove(word);
    }

    public Long getWelcomeChannelID() {
        return welcomeChannelID;
    }

    public void setWelcomeChannelID(Long welcomeChannelID) {
        this.welcomeChannelID = welcomeChannelID;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public int getWelcomeMethod() {
        return welcomeMethod;
    }

    public void setWelcomeMethod(int welcomeMethod) {
        this.welcomeMethod = welcomeMethod;
    }

    public int getWelcomeMessageTimeout() {
        return welcomeMessageTimeout;
    }

    public void setWelcomeMessageTimeout(int welcomeMessageTimeout) {
        this.welcomeMessageTimeout = welcomeMessageTimeout;
    }
}
