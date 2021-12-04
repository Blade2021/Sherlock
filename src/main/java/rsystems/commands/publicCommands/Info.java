package rsystems.commands.publicCommands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Info extends Command {

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        reply(event,"I am currently serving " + SherlockBot.jda.getGuilds().size() + " servers");
    }

    @Override
    public String getHelp() {
        return null;
    }
}
