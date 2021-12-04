package rsystems.commands.publicCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.awt.*;
import java.sql.SQLException;

public class Help extends Command {
    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#21ff67"));

        final String[] args = content.split("\\s+");
        if((args.length < 1) || args[0].isEmpty()){
            builder.setTitle("Help Command");
            builder.setDescription(this.getHelp());

            reply(event,builder.build());
        } else {

            boolean commandFound = false;

            for(Command c: SherlockBot.dispatcher.getCommands()){

                if(c.getName().equalsIgnoreCase(args[0])){

                    commandFound = true;

                    if(c.getHelp() != null){
                        builder.setTitle("Help | " + c.getName());

                        String helpString = c.getHelp();
                        String prefix = "!sL";
                        if(SherlockBot.guildMap.get(event.getGuild().getIdLong()).getPrefix() != null){
                            prefix = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getPrefix();
                        }
                        helpString = helpString.replaceAll("\\{prefix}",prefix);
                        helpString = helpString.replaceAll("\\{command}",String.format("%s",c.getName()));

                        builder.setDescription(helpString);

                        if(c.getDiscordPermission() != null){
                            builder.addField("Discord Perm:",c.getDiscordPermission().getName(),true);
                        } else {
                            if(c.getPermissionIndex() != null) {
                                builder.addBlankField(true);
                            }
                        }

                        if(c.getPermissionIndex() != null){
                            builder.addField("Mod Permission:",c.getPermissionIndex().toString(),true);
                        }

                        if(c.getAliases().length > 0){
                            builder.appendDescription("\n\n");
                            for(String s:c.getAliases()){
                                builder.appendDescription(s).appendDescription(", ");
                            }
                        }
                    } else {
                        builder.setTitle("404 - Not Found   :(");
                        builder.setDescription("Well this is embarrassing.\uD83D\uDE33\nLooks like we don't have any documentation setup for that command at this time.");
                        builder.setFooter("Please check back later or submit a request via GitHub");
                    }
                }
            }

            if (commandFound) {
                reply(event, builder.build());
                builder.clear();
            } else {
                int argSize = args[0].length();

                if(argSize/2 > 4){
                    argSize = 4;
                } else {
                    argSize = argSize/2;
                }
                final String lookupString = args[0].substring(0,argSize);

                for(Command c:SherlockBot.dispatcher.getCommands()){
                    if(c.getName().toLowerCase().startsWith(lookupString.toLowerCase())){
                        reply(event,"Did you mean " + c.getName());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getHelp() {
        return "Prints helpful information about a command.\n\n" +
                "**Helpful Notes**:\n" +
                "All required arguments to a command are wrapped in \"( )\"\n" +
                "Any \"optional\" arguments are wrapped in \"[ ]\"\n\n" +
                "Please use this help function as needed to understand what each command does.";
    }
}
