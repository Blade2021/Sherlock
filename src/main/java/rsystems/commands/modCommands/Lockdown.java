package rsystems.commands.modCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Lockdown extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {

        final Integer preVerifyLevel = event.getGuild().getVerificationLevel().getKey();
        if(event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)){
            event.getGuild().getManager().setVerificationLevel(Guild.VerificationLevel.VERY_HIGH).queue();
        }

        final Integer preExplicitFilter = event.getGuild().getExplicitContentLevel().getKey();
        if(event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)){
            event.getGuild().getManager().setExplicitContentLevel(Guild.ExplicitContentLevel.ALL).queue();
        }

    }

    @Override
    public String getHelp() {
        return null;
    }
}
