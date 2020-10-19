package rsystems.objects;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.SherlockBot;

import java.util.ArrayList;

public class Command {
    protected int id;
    protected String command;
    protected ArrayList<String> alias = new ArrayList<>();
    protected int minArgCount;
    protected String description;
    protected String syntax;
    protected int rank;
    protected boolean deleteTrigger;

    public Command(String command) {
        this.command = command;
    }

    public Command(String command, int id) {
        this.id = id;
        this.command = command;
    }

    public Command(String command, String description, String syntax, int minimumArgCount, int rank) {
        this.command = command;
        this.description = description;
        this.syntax = syntax;
        this.minArgCount = minimumArgCount;
        this.rank = rank;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSyntax() {
        return syntax;
    }

    public void setSyntax(String syntax) {
        this.syntax = syntax;
    }

    public int getMinimumArgCount() {
        return minArgCount;
    }

    public void setMinimumArgCount(int minimumArgCount) {
        this.minArgCount = minimumArgCount;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public ArrayList<String> getAlias() {
        return alias;
    }

    public void setAlias(ArrayList<String> alias) {
        this.alias.addAll(alias);
    }

    public void clearAlias() {
        this.alias.clear();
    }

    public boolean isDeleteTrigger() {
        return deleteTrigger;
    }

    public void setDeleteTrigger(boolean deleteTrigger) {
        this.deleteTrigger = deleteTrigger;
    }

    public int getId() {
        return id;
    }

    public boolean checkCommand(Message message) {
        String guildID = message.getGuild().getId();
        String prefix = SherlockBot.guildMap.get(guildID).getPrefix();
        String formattedMessage = message.getContentDisplay().toLowerCase();

        Boolean commandMatch = false;

        if (formattedMessage.startsWith(prefix + this.command.toLowerCase())) {
            commandMatch = true;
        } else {
            for (String alias : this.getAlias()) {
                if (formattedMessage.startsWith(prefix + alias.toLowerCase())) {
                    commandMatch = true;
                }
            }
        }
        if (commandMatch) {
            if (this.rank > 0) {
                if (!checkPermissionLevel(this.rank, guildID, message.getMember())) {
                    try {
                        message.getChannel().sendMessage(message.getAuthor().getAsMention() + " You are not authorized for that command").queue();
                    } catch (NullPointerException | PermissionException e) {
                        System.out.println(String.format("An error occurred when trying to send a message. GUILD:%s CHANNEL:%s", message.getGuild().getId(), message.getChannel().getId()));
                    }
                    return false;
                } else {
                    //USER IS AUTHORIZED
                    return true;
                }
            } else {
                // PERMISSION FOR COMMAND IS ZERO
                return true;
            }
        }
        //COMMAND WAS NOT FOUND
        return false;
    }


    //Check if requester is authorized for command
    public boolean checkPermissionLevel(int binaryIndex, String guildID, Member requester) {
        boolean authorized = false;

        //MEMBER HAS ADMIN PERMISSIONS SO ALLOW ALL
        if (requester.hasPermission(Permission.ADMINISTRATOR)) {
            authorized = true;
        } else if((binaryIndex >= 32768) && (requester.hasPermission(Permission.ADMINISTRATOR))){
                authorized = true;
        } else {
            int modRoleValue = 0;
            String binaryString = null;
            char indexChar = '0';

            //Parse through each role that the member has
            for (Role r : requester.getRoles()) {

                //Does role fall into defined moderation roles
                if (SherlockBot.guildMap.get(guildID).modRoleMap.containsKey(r.getId())) {

                    //Get the role's Permission level
                    modRoleValue = SherlockBot.guildMap.get(guildID).modRoleMap.get(r.getId());

                    /*
                    Form a binary string based on the permission level integer found.
                    Example: 24 = 11000
                     */
                    binaryString = Integer.toBinaryString(modRoleValue);

                    //Reverse the string for processing
                    String reverseString = new StringBuilder(binaryString).reverse().toString();

                    try {
                        indexChar = reverseString.charAt(binaryIndex);
                    } catch (IndexOutOfBoundsException e) {

                    } finally {
                        if (indexChar == '1') {
                            authorized = true;
                        } else {
                            authorized = false;
                        }
                    }
                }
            }
        }

        return authorized;
    }


    public boolean helpCheck(String command) {
        String[] args = command.split("\\s+");
        if (args[0].equalsIgnoreCase(this.command)) {
            return true;
        } else {
            final Boolean[] returnValue = {false};
            this.alias.forEach(alias -> {
                if (args[0].equalsIgnoreCase(alias)) {
                    returnValue[0] = true;
                }
            });
            return returnValue[0];
        }
    }

}
