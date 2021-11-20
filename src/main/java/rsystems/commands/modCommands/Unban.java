package rsystems.commands.modCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Unban extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.BAN_MEMBERS;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) throws SQLException {
        String[] args = content.split("\\s+");

        if(args.length >= 1){

            final Long userID = Long.parseLong(args[0]);
            event.getGuild().retrieveBanById(userID).queue(ban -> {
                event.getGuild().unban(ban.getUser()).reason("Requested by " + event.getAuthor().getAsTag()).queue(success -> {
                    reply(event,ban.getUser().getAsTag() + " has been unbanned");
                    message.addReaction("✅").queue();
                });
            }, failure -> {
                reply(event,"Could not find user on the ban list");
            });

        }
    }

    @Override
    public String getHelp() {
        return null;
    }
}
