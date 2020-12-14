package rsystems.events;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.objects.RoleReactionObject;

import java.util.concurrent.TimeUnit;

public class GuildReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().containsKey(event.getMessageIdLong())){
            for(RoleReactionObject r:SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().values()){
                if((event.getMessageIdLong() == r.messageID) && (event.getReactionEmote().getIdLong() == r.reactionID)){
                    try {
                        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(r.roleID)).reason("Requested via Reaction").queueAfter(10, TimeUnit.SECONDS);
                    }catch(PermissionException | NullPointerException e){
                        System.out.println("Exception when adding role");
                    }
                }
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if(SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().containsKey(event.getMessageIdLong())){
            for(RoleReactionObject r:SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().values()){
                if((event.getMessageIdLong() == r.messageID) && (event.getReactionEmote().getIdLong() == r.reactionID)){
                    try {
                        event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(r.roleID)).reason("Requested via Reaction").queueAfter(10, TimeUnit.SECONDS);
                    }catch(PermissionException | NullPointerException e){
                        System.out.println("Exception when removing role");
                    }
                }
            }
        }
    }
}
