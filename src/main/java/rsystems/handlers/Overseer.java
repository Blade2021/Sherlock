package rsystems.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import rsystems.SherlockBot;
import rsystems.objects.TrackerObject;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

public class Overseer {

    public void submitTracker(final Long guildID, final Long userID, final Integer type) {
        try {
            SherlockBot.database.insertTracker(guildID, userID, type);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        checkUser(guildID, userID);
    }

    private void checkUser(final Long guildID, final Long userID) {
        ArrayList<TrackerObject> trackers;
        try {
            trackers = SherlockBot.database.getTrackers(guildID, userID);

            if (trackers.size() > 2) {
                quarantineUser(guildID, userID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void quarantineUser(final Long guildID, final Long userID) {
        Guild guild = SherlockBot.jda.getGuildById(guildID);
        if (guild != null) {

            //Member member = guild.getMember(userID);
            Role quarantineRole = guild.getRoleById(SherlockBot.guildMap.get(guildID).getMuteRoleID());
            if (quarantineRole != null) {
                guild.retrieveMemberById(userID).queue(foundMember -> {

                    if(!foundMember.isOwner()) {

                        //Add the quarantine role to roles to ADD during role modification
                        ArrayList<Role> rolesToAdd = new ArrayList<>();
                        rolesToAdd.add(quarantineRole);

                        //Get a list of all roles that the user has currently
                        ArrayList<Role> rolesToRemove = new ArrayList<>(foundMember.getRoles());
                        StringBuilder roleString = new StringBuilder();

                        for (Role r : rolesToRemove) {
                            roleString.append("`").append(r.getName()).append("`").append("  :  ").append(r.getId()).append("\n");
                        }

                        guild.modifyMemberRoles(foundMember, rolesToAdd, rolesToRemove).queue(success -> {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTitle("Quarantined User");
                            builder.setDescription("User has been quarantined for exceeding allowed limits");
                            builder.addField("User Tag:", foundMember.getUser().getAsTag(), true);
                            builder.addField("User ID:", foundMember.getId(), true);
                            builder.addField("Removed Roles:", roleString.toString(), false);
                            builder.setThumbnail(foundMember.getEffectiveAvatarUrl());
                            builder.setTimestamp(Instant.now());
                            builder.setColor(SherlockBot.getColor("quarantine"));

                            LogMessage.sendLogMessage(guildID, builder.build());
                            builder.clear();
                        });
                    }
                });
            }
        }
    }
}
