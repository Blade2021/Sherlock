package rsystems.events;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.objects.RoleReactionObject;
import rsystems.objects.UserRoleReactionObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class GuildReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(event.getMember().getUser().isBot()){
            return;
        }

        if(SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().containsKey(event.getMessageIdLong())){
            for(RoleReactionObject r:SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().get(event.getMessageIdLong())){

                try {
                    if(event.getReaction().getReactionEmote().isEmote()) {
                        if (event.getReactionEmote().getId().equalsIgnoreCase(r.reactionID)) {
                            try {

                                handleRoleTransaction(event.getGuild().getIdLong(),event.getMember().getIdLong(),r.roleID,true);

                                //event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(r.roleID)).reason("Requested via Reaction").queueAfter(2, TimeUnit.SECONDS);
                            } catch (PermissionException | NullPointerException e) {
                                System.out.println("Exception when adding role");
                            }
                        }
                    } else {
                        String check = EmojiParser.parseToAliases(event.getReaction().getReactionEmote().getEmoji());
                        if(check.equalsIgnoreCase(r.reactionID)){
                            try {
                                handleRoleTransaction(event.getGuild().getIdLong(),event.getMember().getIdLong(),r.roleID,true);

                                //event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(r.roleID)).reason("Requested via Reaction").queueAfter(2, TimeUnit.SECONDS);
                            } catch (PermissionException | NullPointerException ex) {
                                System.out.println("Exception when adding role");
                            }
                        }
                    }
                }catch(IllegalStateException e){
                    String check = EmojiParser.parseToAliases(event.getReaction().getReactionEmote().getEmoji());
                    if(check.equalsIgnoreCase(r.reactionID)){
                        try {
                            event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(r.roleID)).reason("Requested via Reaction").queueAfter(2, TimeUnit.SECONDS);
                        } catch (PermissionException | NullPointerException ex) {
                            System.out.println("Exception when adding role");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if(event.getMember().getUser().isBot()){
            return;
        }

        if(SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().containsKey(event.getMessageIdLong())){
            for(RoleReactionObject r:SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().get(event.getMessageIdLong())){
                try {

                    if(event.getReaction().getReactionEmote().isEmote()) {
                        if (event.getReactionEmote().getId().equalsIgnoreCase(r.reactionID)) {
                            try {
                                handleRoleTransaction(event.getGuild().getIdLong(),event.getMember().getIdLong(),r.roleID,false);

                                //event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(r.roleID)).reason("Requested via Reaction").queueAfter(2, TimeUnit.SECONDS);
                            } catch (PermissionException | NullPointerException e) {
                                System.out.println("Exception when removing role");
                            }
                        }
                    } else {
                        String check = EmojiParser.parseToAliases(event.getReaction().getReactionEmote().getEmoji());
                        if(check.equalsIgnoreCase(r.reactionID)){
                            try {
                                handleRoleTransaction(event.getGuild().getIdLong(),event.getMember().getIdLong(),r.roleID,false);

                                //event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(r.roleID)).reason("Requested via Reaction").queueAfter(2, TimeUnit.SECONDS);
                            } catch (PermissionException | NullPointerException ex) {
                                System.out.println("Exception when adding role");
                            }
                        }
                    }
                }catch(IllegalStateException e){
                }
            }
        }
    }

    private void handleRoleTransaction(final Long guildID, final Long userID, final Long roleID, final boolean addRole){
        if(SherlockBot.reactionHandleMap.containsKey(guildID)){
            //ArrayList<UserRoleReactionObject> list = SherlockBot.reactionHandleMap.get(guildID);
            //list.add(new UserRoleReactionObject(userID,roleID,addRole));

            //SherlockBot.reactionHandleMap.put(guildID,list);
        } else {
            ArrayList<UserRoleReactionObject> newList = new ArrayList<>();
            newList.add(new UserRoleReactionObject(userID, roleID, addRole));
            //SherlockBot.reactionHandleMap.put(guildID,newList);
        }
    }
}
