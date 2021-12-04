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

        if(args.length >= 1) {
            TextChannel targetChannel = null;

            if (message.getMentionedChannels().size() >= 1) {
                targetChannel = message.getMentionedChannels().get(0);
            } else {
                if(event.getGuild().getTextChannelById(args[1]) != null){
                    targetChannel = event.getGuild().getTextChannelById(args[1]);
                }
            }

            if(targetChannel != null){
                if(SherlockBot.database.getLong("IgnoreChannelTable","ChannelID","ChildGuildID",event.getGuild().getIdLong(),"ChannelID",targetChannel.getIdLong()) != null) {
                    if(SherlockBot.database.handleIgnoreChannel(event.getGuild().getIdLong(),targetChannel.getIdLong(),false) >= 1){
                        message.addReaction("✅").queue();
                    }
                }
            }

        }
    }

    @Override
    public String getHelp() {
        return null;
    }
}
