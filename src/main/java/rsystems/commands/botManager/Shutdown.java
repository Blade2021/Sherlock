package rsystems.commands.botManager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Shutdown extends Command {

    @Override
    public boolean isOwnerOnly() {
        return true;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        reply(event,"Shutting down...");
        handleEvent();
    }

    @Override
    public String getHelp() {
        return null;
    }

    private void handleEvent(){
        SherlockBot.jda.shutdown();
    }
}
