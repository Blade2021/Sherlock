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

    public Command(String command) {
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

    public boolean checkCommand(String message, String guildID) {
        String prefix = SherlockBot.guildMap.get(guildID).getPrefix();

        String formattedMessage = message.toLowerCase();
        if (formattedMessage.startsWith(prefix + this.command.toLowerCase())) {
            return true;
        } else {
            final Boolean[] returnValue = {false};
            this.alias.forEach(alias -> {
                if (formattedMessage.startsWith(prefix + alias.toLowerCase())) {
                    returnValue[0] = true;
                }
            });
            return returnValue[0];
        }
    }

    //Check if message had command, alias, AND correct auth level
    public int checkCommandMod(String message, String guildID, Member member) {
        String prefix = SherlockBot.guildMap.get(guildID).getPrefix();
        String formattedMessage = message.toLowerCase();
        if (formattedMessage.startsWith(prefix + this.command.toLowerCase())) {
            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                return 4;
            }
        } else {
            final int[] returnValue = {0};
            this.alias.forEach(alias -> {
                if (formattedMessage.startsWith(prefix + alias.toLowerCase())) {
                    if (member.hasPermission(Permission.ADMINISTRATOR)) {
                        returnValue[0] = 4;
                    }
                }
            });
            return returnValue[0];
        }
        return 0;
    }

    //Check if message had command, alias, AND correct auth level
    public boolean checkCommandMod(Message message) {
        String prefix = SherlockBot.guildMap.get(message.getGuild().getId()).getPrefix();
        String formattedMessage = message.getContentDisplay().toLowerCase();
        final Boolean[] authorized = {false};

        if (formattedMessage.startsWith(prefix + this.command.toLowerCase())) {
            if (message.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                return true;
            } else {
                if (!authorized[0]) {
                    message.getChannel().sendMessage(message.getAuthor().getAsMention() + " You are not authorized to use that command").queue();
                }
            }
        } else {
            final boolean[] returnValue = {false};
            this.alias.forEach(alias -> {
                if (formattedMessage.startsWith(prefix + alias.toLowerCase())) {
                    if (message.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        authorized[0] = true;
                        returnValue[0] = true;
                    } else {
                        if (!authorized[0]) {
                            message.getChannel().sendMessage(message.getAuthor().getAsMention() + " You are not authorized to use that command").queue();
                        }
                    }
                }
            });
            return returnValue[0];
        }
        return false;
    }

    //Check if message had command, alias, AND correct auth level
    public boolean checkCommandMod(Message message, int binaryIndex) {
        String prefix = SherlockBot.guildMap.get(message.getGuild().getId()).getPrefix();
        String formattedMessage = message.getContentDisplay().toLowerCase();

        boolean commandFound = false;
        if (formattedMessage.startsWith(prefix + this.command.toLowerCase())) {
            commandFound = true;
        } else {
            for (String s : this.alias) {
                if(formattedMessage.startsWith(prefix + s.toLowerCase())){
                    commandFound = true;
                }
            }
        }

        if(commandFound) {
            boolean authorized = false;

            if (message.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                authorized = true;
            } else {
                String guildID = message.getGuild().getId();
                int modRoleValue = 0;
                String binaryString = "";
                char indexChar = '0';


                for (Role r : message.getMember().getRoles()) {

                    if (SherlockBot.guildMap.get(guildID).modRoleMap.containsKey(r.getId())) {

                        modRoleValue = SherlockBot.guildMap.get(guildID).modRoleMap.get(r.getId());
                        binaryString = Integer.toBinaryString(modRoleValue);
                        String reverseString = new StringBuilder(binaryString).reverse().toString();
                        try{
                            indexChar = reverseString.charAt(binaryIndex);
                        } catch(IndexOutOfBoundsException e){

                        } finally {
                            if(indexChar == '1'){
                                authorized = true;
                            } else {
                                authorized = false;
                            }
                        }
                    }
                }
            }
            if(!authorized){
                try{
                    message.getChannel().sendMessage(message.getAuthor().getAsMention() + " You are not authorized for that command").queue();
                } catch(NullPointerException | PermissionException e){
                    System.out.println(String.format("An error occurred when trying to send a message. GUILD:%s CHANNEL:%s",message.getGuild().getId(),message.getChannel().getId()));
                }
            }

            return authorized;
        }

        return false;
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
