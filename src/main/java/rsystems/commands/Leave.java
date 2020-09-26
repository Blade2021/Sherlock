package rsystems.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

public class Leave extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");

        // LEAVE GUILD COMMAND
        if (SherlockBot.commands.get(0).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            try{

                // Delete mute role added by BoT
                event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getId()).getMuteRoleID()).delete().reason("Leaving guild.  Removing bot added roles").queue();

            } catch (NullPointerException | PermissionException e){
                event.getChannel().sendMessage("An error occurred when attempting to clean guild roles").queue();
            } finally {
                event.getGuild().leave().queue();
            }
        }
    }
}
