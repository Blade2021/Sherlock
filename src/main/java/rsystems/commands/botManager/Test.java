package rsystems.commands.botManager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Test extends Command {
    @Override
    public boolean isOwnerOnly() {
        return true;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {

        /*
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setContent("This is just some content");
        messageBuilder.setActionRows(ActionRow.of(Button.primary("next","Click here!"),Button.danger("previous","Don't click this")));
        event.getChannel().sendMessage(messageBuilder.build()).queue(success -> {
            event.getMessage().delete().queue();
        });

         */

        SherlockBot.slashCommandDispatcher.submitCommands(event.getGuild().getIdLong());
    }

    @Override
    public String getHelp() {
        return null;
    }
}
