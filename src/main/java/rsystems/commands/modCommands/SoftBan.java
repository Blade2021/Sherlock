package rsystems.commands.modCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.objects.Command;

import java.util.concurrent.TimeUnit;

public class SoftBan extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.KICK_MEMBERS;
    }

    @Override
    public Integer getPermissionIndex() {
        return 128;
    }

    private static final String[] ALIASES = new String[] {"kick"};

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) {
        if(message.getMentionedMembers().size() > 0){
            for(Member member:message.getMentionedMembers()){
                try{
                    User user = member.getUser();
                    if(message.getAuthor() == user){
                        reply(event,"You cannot call this command on yourself.");
                        break;
                    } else {
                        event.getGuild().ban(member, 7, "Softban requested by: " + message.getAuthor().getAsTag()).queueAfter(5, TimeUnit.SECONDS, success -> {
                            message.addReaction("✅").queue();
                            event.getGuild().unban(user).queueAfter(5, TimeUnit.SECONDS);
                        });
                    }
                } catch (PermissionException e){
                    reply(event,"A permission error occurred: " + e.getPermission().toString());
                    break;
                }
            }
        }
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getAliases(){
        return ALIASES;
    }

}
