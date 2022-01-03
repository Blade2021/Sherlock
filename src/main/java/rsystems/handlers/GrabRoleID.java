package rsystems.handlers;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class GrabRoleID {

    public static Long getRoleIDFromMessage(Message message, String content){
        Long roleID = null;

        String modifiedContent = content;
        if(message.getMentionedMembers().size() >0){
            for(Member member:message.getMentionedMembers()){
                modifiedContent = modifiedContent.replaceAll(member.getAsMention(),"");
            }
        }

        modifiedContent = modifiedContent.trim();

        if(modifiedContent.indexOf(" ") > 0){
            try{
                roleID = Long.valueOf(modifiedContent.substring(0,modifiedContent.indexOf(" ")));
            } catch (NumberFormatException e){
                return null;
            }
        } else {
            try{
                roleID = Long.valueOf(modifiedContent);
            } catch (NumberFormatException e){
                return null;
            }
        }

        return roleID;
    }
}
