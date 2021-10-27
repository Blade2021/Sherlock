package rsystems.commands.modCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.objects.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GiveRole extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.MANAGE_ROLES;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) {
        List<Member> memberList = new ArrayList<>();
        Role role = null;
        String reason = null;

        boolean lookupUsingID = false;

        String[] args = content.split("\\s+");

        if(message.getMentionedMembers().size() >0){
            memberList.addAll(message.getMentionedMembers());
        } else {
            // Extract Member using ID Lookup
            
            lookupUsingID = true;
        }

        if(args.length >= 2){
            
            String indexContent = content;
            
            if(!lookupUsingID) {
                // Remove mentioned members from String
                for (Member member : memberList) {
                    indexContent = indexContent.replaceAll(member.getEffectiveName(), "");
                }

            }
            indexContent = indexContent.trim();
            int endOfRoleID = indexContent.indexOf(" ");
            
            Long roleID = null;
            if(endOfRoleID > 0){
                roleID = Long.valueOf(indexContent.substring(0,indexContent.indexOf(" ")));
            } else {
                roleID = Long.valueOf(indexContent);
            }
            
            if(event.getGuild().getRoleById(roleID) != null){
                role = event.getGuild().getRoleById(roleID);
            }
            
            reason = indexContent.substring(endOfRoleID);
            
            if(event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)){
                for(Member member:memberList){
                    event.getGuild().addRoleToMember(member.getIdLong(),role).reason(reason).queueAfter(10, TimeUnit.SECONDS);
                }
            }
            
        }

    }

    @Override
    public String getHelp() {
        return null;
    }
}
