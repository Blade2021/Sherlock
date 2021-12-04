package rsystems.commands.modCommands;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;
import java.util.ArrayList;

public class Mute extends Command {
    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        String[] args = content.split("\\s+");

        if (args.length >= 2) {

            if (SherlockBot.guildMap.get(event.getGuild().getIdLong()).getMuteRoleID() != null) {

                final Long roleID = SherlockBot.getGuildSettings(event.getGuild().getIdLong()).getMuteRoleID();
                if(event.getGuild().getRoleById(roleID) != null) {
                    final Role muteRole = event.getGuild().getRoleById(roleID);

                    ArrayList<Member> membersToMute = new ArrayList<>();
                    String modifiedContent = content;


                    membersToMute.addAll(message.getMentionedMembers());

                    for (Member member : message.getMentionedMembers()) {
                        modifiedContent = modifiedContent.replaceAll(member.getEffectiveName(), "");
                    }

                    modifiedContent = modifiedContent.trim();

                    if (!modifiedContent.isEmpty()) {
                        Long userID = Long.parseLong(modifiedContent);
                        if ((userID != null) && (event.getGuild().getMemberById(userID) != null)) {
                            membersToMute.add(event.getGuild().getMemberById(userID));
                        }
                    }

                    for(Member member:membersToMute){
                        event.getGuild().addRoleToMember(member,muteRole).reason("Requested by " + message.getAuthor().getAsTag()).queue();
                    }
                }
            }
        }


    }

    @Override
    public String getHelp() {
        return null;
    }
}
