package rsystems.commands.botManager;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Test extends Command {
    @Override
    public boolean isOwnerOnly() {
        return true;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) throws SQLException {

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setContent("This is just some content");
        messageBuilder.setActionRows(ActionRow.of(Button.primary("next","Click here!"),Button.danger("previous","Don't click this")));
        event.getChannel().sendMessage(messageBuilder.build()).queue(success -> {
            event.getMessage().delete().queue();
        });

    }

    @Override
    public String getHelp() {
        return null;
    }
}
