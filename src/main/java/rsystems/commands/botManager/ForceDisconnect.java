package rsystems.commands.botManager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class ForceDisconnect extends Command {
    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {
        if(sender.getIdLong() == Long.parseLong(Config.get("OWNER_ID"))){
            handleEvent(sender,message,content);
        } else {
            reply(event,"You are not authorized for that");
        }
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) throws SQLException {
        if(sender.getIdLong() == Long.parseLong(Config.get("OWNER_ID"))){
            handleEvent(sender,message,content);
        } else {
            reply(event,"You are not authorized for that");
        }
    }

    @Override
    public String getHelp() {
        return null;
    }

    private boolean handleEvent(final User sender, final Message message, final String content){
        String[] args = content.split("\\s+");

        if(args.length >= 1) {

            final Long removeGuildID = Long.valueOf(args[0]);
            if(SherlockBot.jda.getGuildById(removeGuildID) != null){
                SherlockBot.jda.getGuildById(removeGuildID).leave().queue();
                return true;
            }

        } else {
            return false;
        }

        return false;
    }
}
