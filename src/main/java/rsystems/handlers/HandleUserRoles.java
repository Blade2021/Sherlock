package rsystems.handlers;

import net.dv8tion.jda.api.entities.Role;
import rsystems.SherlockBot;
import rsystems.objects.UserRoleReactionObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;

public class HandleUserRoles extends TimerTask {

    @Override
    public void run() {

        for(Map.Entry<Long, Map<Long,ArrayList<UserRoleReactionObject>>> entry: SherlockBot.reactionHandleMap.entrySet()){

            Long guildID = entry.getKey();
            for(Map.Entry<Long,ArrayList<UserRoleReactionObject>> valueEntry:entry.getValue().entrySet()){

                ArrayList<Role> roleList = new ArrayList<>();

                for(UserRoleReactionObject urr:valueEntry.getValue()){

                    if(SherlockBot.jda.getGuildById(guildID) != null){
                        if(SherlockBot.jda.getGuildById(guildID).getRoleById(urr.roleID) != null){
                            roleList.add(SherlockBot.jda.getRoleById(urr.roleID));
                        }
                    }

                }

               // if(SherlockBot.jda.getGuildById(guildID).getMemberById())

                //SherlockBot.jda.getGuildById(guildID).modifyMemberRoles(valueEntry.getKey(),roleList)
            }

        }

    }
}
