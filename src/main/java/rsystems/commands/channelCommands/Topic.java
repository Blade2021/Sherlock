package rsystems.commands.channelCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.objects.Command;

import java.awt.*;

public class Topic extends Command {

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT)) {
            TextChannel textChannel = event.getTextChannel();

            handleEvent(event,textChannel);

        } else if((event.isFromType(ChannelType.GUILD_PUBLIC_THREAD)) || (event.isFromType(ChannelType.GUILD_PRIVATE_THREAD))) {

            TextChannel textChannel = event.getGuild().getTextChannelById(event.getThreadChannel().getParentMessageChannel().getId());

            if(textChannel != null){
                handleEvent(event,textChannel);
            }

        } else {
            reply(event, "This command is for message channels only.");
        }

    }

    private void handleEvent(final MessageReceivedEvent event, final TextChannel textChannel){
        if (textChannel.getTopic() != null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(textChannel.getName() + " - Topic")
                    .setDescription(textChannel.getTopic())
                    .setColor(Color.CYAN)
                    .setFooter("Called by: " + event.getMember().getUser().getAsTag(), event.getAuthor().getEffectiveAvatarUrl());

            channelReply(event,embedBuilder.build());
        }
    }

    @Override
    public String getHelp() {
        return "Prints the calling channel's Topic";
    }
}
