package rsystems.commands.guildFunctions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;
import java.util.ArrayList;

public class WatchChannel extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.MANAGE_SERVER;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        //Long channelID
        //Mentioned Channel

        String[] args = content.split("\\s+");

        if (args.length >= 1) {
            ArrayList<TextChannel> requestedIgnoreList = new ArrayList<>();

            if (message.getMentionedChannels().size() >= 1) {
                for (TextChannel requestedChannel : message.getMentionedChannels()) {
                    //Don't allow other guild channels to be put into database
                    if (requestedChannel.getGuild().getIdLong() == event.getGuild().getIdLong()) {
                        requestedIgnoreList.add(requestedChannel);
                    }
                }
            } else {
                if (event.getGuild().getTextChannelById(args[1]) != null) {
                    requestedIgnoreList.add(event.getGuild().getTextChannelById(args[1]));
                }
            }

            if (requestedIgnoreList.size() > 0) {
                for (TextChannel targetChannel : requestedIgnoreList) {
                    if (SherlockBot.database.handleIgnoreChannel(event.getGuild().getIdLong(), targetChannel.getIdLong(), false) >= 1) {
                        message.addReaction("✅").queue();
                    }
                }
            }

        }
    }

    @Override
    public String getHelp() {
        return "Set a channel(s) to be watched by Sherlock.\n" +
                "This will remove any ignore configurations from said channel\n\n" +
                "{prefix}{command} [Mention Channel(s)]\n" +
                "{prefix}{command} [ChannelID]";
    }
}
