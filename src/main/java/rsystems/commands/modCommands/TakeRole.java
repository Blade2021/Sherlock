package rsystems.commands.modCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.objects.Command;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TakeRole extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.MANAGE_ROLES;
    }

    @Override
    public Integer getPermissionIndex() {
        return 8;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws HierarchyException {

        String[] args = content.split("\\s+");

        if (args.length >= 2) {

            List<Member> memberList = new ArrayList<>();
            String modifiedContent = content;
            Long roleID = null;

            if (message.getMentionedMembers().size() == 0) {
                // DON'T ALLOW COMMAND TO BE USED WITHOUT MENTIONABLES
                // Mentionables is now a word.  If you ever come across this, let me know.  I'll give you a gold star.

                Long memberID = getLongFromArgument(args[0]);
                if ((memberID != null) && (event.getGuild().getMemberById(memberID) != null)) {
                    memberList.add(event.getGuild().getMemberById(memberID));
                } else {
                    reply(event, "Could not find member with ID: `" + memberID + "`");
                }

                roleID = getLongFromArgument(args[1]);

            } else {
                // MENTIONABLES WERE FOUND!  HOORAY!

                memberList = message.getMentionedMembers();
                for (Member member : message.getMentionedMembers()) {
                    modifiedContent = modifiedContent.replaceAll("<@!" + member.getId() + ">", "");
                }

                modifiedContent = modifiedContent.trim();

                if (modifiedContent.contains(" ")) {
                    roleID = getLongFromArgument(modifiedContent.substring(0, modifiedContent.indexOf(" ")));
                } else {
                    roleID = getLongFromArgument(modifiedContent);
                }


            }

            if ((roleID != null) && (event.getGuild().getRoleById(roleID) != null) && (memberList.size() > 0)) {
                // Was Role successfully found?

                Role role = event.getGuild().getRoleById(roleID);


                for (Member member : memberList) {
                    try {
                        event.getGuild().removeRoleFromMember(member.getId(), role).reason(String.format("Requested by %s", message.getAuthor().getAsTag())).queueAfter(3, TimeUnit.SECONDS, Success -> {
                            message.addReaction("✅").queue();
                        });
                    } catch (HierarchyException e) {
                        Role highestSelfRole = event.getGuild().getSelfMember().getRoles().get(0);

                        EmbedBuilder embedBuilder = new EmbedBuilder();

                        if(highestSelfRole.getIdLong() == roleID){
                            embedBuilder.setDescription(String.format("%s `<%d>` cannot be given or taken as it is the highest role of this BOT.\n",role,role.getIdLong(),highestSelfRole));
                        } else {
                            embedBuilder.setDescription(String.format("%s `<%d>` is above %s `<%d>` in the roles hierarchy.\n\nUnable to process request.", role, role.getIdLong(), highestSelfRole, highestSelfRole.getIdLong()));
                        }
                        embedBuilder.setColor(Color.decode("#FF6145"));
                        embedBuilder.setFooter(String.format("%s called by %s <%d>",this.getName(),event.getAuthor().getAsTag(),event.getAuthor().getIdLong()));

                        reply(event,embedBuilder.build());
                        embedBuilder.clear();
                        //reply(event, String.format("%s `<%d>` is above or same level as %s `<%d>`in the roles hierarchy.\nUnable to process request.", role,role.getIdLong(),highestSelfRole,highestSelfRole.getIdLong()));
                        break;
                    }

                    catch (PermissionException e) {
                        reply(event, String.format("Encountered Permission error: %s while attempting to run command.", e.getPermission()));
                        break;
                    }
                }
            }
        }

    }

    @Override
    public String getHelp() {
        return "{prefix}{command} `(Mention Members)` `(RoleID)`\n{prefix}{command} `(MemberID)` `(RoleID)`\n\nTake a role from a member or members of the server.";
    }
}
