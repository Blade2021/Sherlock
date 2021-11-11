package rsystems.commands.subscriberOnly;

import net.dv8tion.jda.api.entities.*;
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

            TextChannel targetChannel = event.getChannel();
            if(message.getMentionedChannels().size() > 0){
                if(message.getMentionedChannels().get(0) != null){
                    targetChannel = message.getMentionedChannels().get(0);
                }
            }

            String channelName = targetChannel.getName() + "-copy";

            final TextChannel finalTargetChannel = targetChannel;
            targetChannel.createCopy().setName(channelName).queue(success -> {
                reply(event,"Your channel has been created here: " + success.getAsMention());

                finalTargetChannel.retrieveWebhooks().queue((webhooks -> {
                    for(Webhook hook:webhooks){
                        if(hook.getType().getKey() == 2){
                            TextChannel hookChannel = hook.getGuild().getTextChannelById(hook.getSourceChannel().getIdLong());
                            hookChannel.follow(success).queue();
                        }
                    }
                }));

            });
        } else {
            reply(event, "Sorry that command is a \"Subscriber Only\".\nYou can join by subscribing here: (Insert link here)");
        }
    }

    @Override
    public String getHelp() {
        return "{prefix}{command} `[Mention Target Channel]`\n\n" + "\uD83D\uDC51 ***SUBSCRIBER EXCLUSIVE COMMAND** \uD83D\uDC51\n\nThis command will copy the current channel or mentioned channel.  Copying will replicate all permission overrides from the copied channel as well as description.";
    }
}
