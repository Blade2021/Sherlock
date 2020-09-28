package rsystems.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogChannel;

import java.util.List;
import java.util.Objects;

public class Mute extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
        }

        // MUTE COMMAND
        if (SherlockBot.commands.get(2).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            //Get a list of members
            List<Member> MentionedMembers = event.getMessage().getMentionedMembers();
            MentionedMembers.forEach(member -> {
                //Add Mute Role
                try {
                    event.getGuild().addRoleToMember(member, Objects.requireNonNull(event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getId()).getMuteRoleID()))).reason("Called by: " + event.getAuthor().getAsTag()).queue();
                } catch (HierarchyException e) {
                    event.getChannel().sendMessage("That user is above me in rank!").queue();
                } catch (NullPointerException e){
                    event.getChannel().sendMessage("Could not find mute role").queue();
                }
            });

            LogChannel logChannel = new LogChannel();
            logChannel.logAction(event.getGuild(),"Muted User",MentionedMembers,event.getMember());

        }

        // UNMUTE COMMAND
        if (SherlockBot.commands.get(3).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            //Get a list of members

            List<Member> MentionedMembers = event.getMessage().getMentionedMembers();
            try {
                //Add role to be removed to collection
                MentionedMembers.forEach(member -> {
                    //Remove Mute Role
                    event.getGuild().removeRoleFromMember(member, Objects.requireNonNull(event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getId()).getMuteRoleID()))).reason("Called by: " + event.getAuthor().getAsTag()).queue();
                });
            }catch(NullPointerException e){

            }

            LogChannel logChannel = new LogChannel();
            logChannel.logAction(event.getGuild(),"Unmuted User",MentionedMembers,event.getMember());

        }
    }


}
