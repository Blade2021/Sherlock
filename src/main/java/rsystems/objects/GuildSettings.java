package rsystems.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuildSettings {
    private String prefix;
    private ArrayList<String> modRoles = new ArrayList<>();
    public String logChannelID;
    public int embedFilter = 0;
    public Map<String, Long> assignableRoleMap = new HashMap<>();
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

    public ArrayList<String> getModRoles() {
        return modRoles;
    }

    public void setModRoles(ArrayList<String> modRoles) {
        this.modRoles = modRoles;
    }

    public boolean addModRole(String roleID){
        try{
            //Role roleCheck = leById(roleID);
            this.modRoles.add(roleID);
            return true;
        } catch(NullPointerException e){
            // do nothing...
        }
        return false;
    }

    public boolean removeModRole(String roleID){
        for(String modID:this.modRoles){
            if(modID.equalsIgnoreCase(roleID)){
               //todo complete this
                return true;
            }
        }
        return false;
    }

    public void addAssignableRole(String roleCommand, Long roleID){
        assignableRoleMap.putIfAbsent(roleCommand,roleID);
    }

    public void removeAssignableRole(String roleCommand){
        assignableRoleMap.remove(roleCommand);
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
