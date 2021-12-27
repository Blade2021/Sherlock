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

public class Info extends Command {

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.decode("#1ABDF5"));

        embedBuilder.setTitle("Who is Sherlock?");
        embedBuilder.setDescription("Hello there!\nI am Sherlock, an overseer moderation BoT here to help keep the channels clear of bad intent.\n\nBe sure to check out any commands you have using `/commmands`");
        embedBuilder.setThumbnail(SherlockBot.bot.getEffectiveAvatarUrl());
        embedBuilder.setFooter("Currently Serving: " + SherlockBot.jda.getGuilds().size() + " servers");

        reply(event,embedBuilder.build());
    }

    @Override
    public String getHelp() {
        return "Get a little about info about Sherlock!";
    }
}
