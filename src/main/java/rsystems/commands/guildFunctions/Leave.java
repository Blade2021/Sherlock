package rsystems.commands.guildFunctions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Leave extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {

        Long quarantineRoleID = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getQuarantineRoleID();

        if ((quarantineRoleID != null) && (event.getGuild().getRoleById(quarantineRoleID) != null)){

            event.getGuild().getRoleById(quarantineRoleID).delete().reason("Leaving guild.  Removing auto-created mute role").queue(Success -> {
                event.getGuild().leave().queue();
            });

        } else {
            event.getGuild().leave().queue();
        }
    }

    @Override
    public String getHelp() {
        //todo help docs
        return null;
    }
}
