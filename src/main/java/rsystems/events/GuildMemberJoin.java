package rsystems.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogChannel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class GuildMemberJoin extends ListenerAdapter {

    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        //Welcome Message Handler
        if ((SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMethod() != 0) && !(SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMessage().isEmpty())) {
            welcomeMessage(event);
        }

        //Auto Role Handler
        if (SherlockBot.database.getAutoRoles(event.getGuild().getIdLong()).size() > 0) {
            ArrayList<Long> autoRoles = new ArrayList<>();
            autoRoles.addAll(SherlockBot.database.getAutoRoles(event.getGuild().getIdLong()));

            ArrayList<Role> autoRoleArray = new ArrayList<>();
            for (Long roleID : autoRoles) {
                if (event.getGuild().getRoleById(roleID) != null) {
                    autoRoleArray.add(event.getGuild().getRoleById(roleID));
                }

            }

            try {
                event.getGuild().modifyMemberRoles(event.getMember(), autoRoleArray).queue();
            } catch (PermissionException | NullPointerException e) {
                System.out.println(String.format("An error occurred when trying to assign auto role(s) to member (%d).", event.getMember().getIdLong()));
            }

        }

    }

    private void welcomeMessage(GuildMemberJoinEvent event) {
        int welcomeMethod = SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMethod();

        // Guild Welcome Channel
        if (welcomeMethod == 1) {
            Long channelID = SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeChannelID();
            TextChannel welcomeChannel = null;

            // GET THE CHANNEL ID FOR THE MESSAGE
            if ((channelID == null) || (channelID == 0)) {
                welcomeChannel = event.getGuild().getDefaultChannel();
            } else {
                if (event.getGuild().getTextChannelById(channelID) != null) {
                    welcomeChannel = event.getGuild().getTextChannelById(channelID);
                }
            }

            //SEND THE WELCOME MESSAGE
            try {
                welcomeChannel.sendMessage(formattedWelcomeMSG(event.getGuild(), event.getMember())).queue(success -> {
                    if (SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMessageTimeout() > 0) {
                        success.delete().queueAfter(SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMessageTimeout(), TimeUnit.SECONDS);
                    }
                });
            } catch (NullPointerException e) {

            } catch (PermissionException e) {
                LogChannel logChannel = new LogChannel();
                logChannel.logAction(event.getGuild(), "Private Message Failed", "Tried to send a member a welcome message in welcome channel but failed due to permission.\n\nPermission: " + e.getPermission(), event.getMember(), 2);
            }


        }

        // Guild Welcome Channel
        if (welcomeMethod == 2) {
            try {
                event.getMember().getUser().openPrivateChannel().queue((privateChannel) -> {
                    privateChannel.sendMessage(formattedWelcomeMSG(event.getGuild(), event.getMember())).queue(success -> {
                        // do nothing currently
                    }, failure -> {
                        TextChannel fallbackWelcomeChannel = null;

                        Long channelID = SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeChannelID();
                        if ((channelID == null) || (channelID == 0)) {
                            fallbackWelcomeChannel = event.getGuild().getDefaultChannel();
                        } else {
                            fallbackWelcomeChannel = event.getGuild().getTextChannelById(channelID);
                        }

                        fallbackWelcomeChannel.sendMessage(formattedWelcomeMSG(event.getGuild(), event.getMember())).queue();

                    });
                });
            } catch (NullPointerException | PermissionException e) {
                LogChannel logChannel = new LogChannel();
                logChannel.logAction(event.getGuild(), "Private Message Failed", "Tried to send a member a welcome message in direct messages but failed due to privacy setings", event.getMember(), 2);
            }
        }

    }

    private String formattedWelcomeMSG(Guild guild, Member member) {

        String output = SherlockBot.guildMap.get(guild.getId()).getWelcomeMessage();


        output = output.replace("{user.name}", member.getEffectiveName());
        output = output.replace("{user.mention}", member.getAsMention());
        output = output.replace("{guild.name}", guild.getName());

        return output;
    }
}
