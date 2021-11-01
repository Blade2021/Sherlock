package rsystems.commands.subscriberOnly;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class CopyChannel extends Command {
    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) throws SQLException {

        if(SherlockBot.database.getInt("SubscriberTable","SubLevel","ChildGuildID",event.getGuild().getIdLong()) >= 1){

            final String channelName = event.getChannel().getName() + "-copy";

            event.getChannel().createCopy().setName(channelName).queue(success -> {
                reply(event,"Your channel has been created here: " + success.getAsMention());
            });
        } else {
            reply(event, "Sorry that command is a \"Subscriber Only\".\nYou can join by subscribing here: (Insert link here)");
        }
    }

    @Override
    public String getHelp() {
        return null;
    }
}
