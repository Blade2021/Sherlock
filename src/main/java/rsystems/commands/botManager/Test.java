package rsystems.commands.botManager;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Test extends Command {
    private final String guildID = "386701951662030858";
    private final String channelID = "904358912738861167";


    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) throws SQLException {
        TextChannel newsChannel = SherlockBot.jda.getGuildById(guildID).getTextChannelById(channelID);
        newsChannel.follow(event.getChannel()).queue();
    }

    @Override
    public String getHelp() {
        return null;
    }
}
