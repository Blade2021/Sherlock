package rsystems.handlers;
/*

 */

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.objects.TrackerObject;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class Overseer {

    /**
     *
     * @param guildID
     * @param userID
     * @param type <p>0 = Warning  (Expires after 1 hour)</p>
     *             <p>1 = Medium Level (Expires after 2 hours)</p>
     *             <p>2 = High Level (Expires after 4 hours)</p>
     *             <p>3 = Very High Level (Expires after 12 hours)</p>
     * @param note
     */
    public void submitTracker(final Long guildID, final Long userID, final Integer type, final String note) {
        try {
            switch(type){
                case 0:
                    SherlockBot.database.insertTracker(guildID, userID, type,1,note);
                    break;
                case 1:
                    SherlockBot.database.insertTracker(guildID, userID, type,2,note);
                    break;
                case 3:
                    SherlockBot.database.insertTracker(guildID, userID, type,12,note);
                    break;
                default:
                    SherlockBot.database.insertTracker(guildID, userID, type,4,note);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        checkUser(guildID, userID);
    }

    private void checkUser(final Long guildID, final Long userID) {
        ArrayList<TrackerObject> trackers;
        try {
            trackers = SherlockBot.database.getTrackers(guildID, userID);

            int heatLevel = 0;

            for(TrackerObject trackerObject:trackers){

                Integer type = trackerObject.getType();
                switch(type){
                    case 3:
                        heatLevel = heatLevel + Integer.parseInt(Config.get("VeryHighTrigger"));
                    case 2:
                        heatLevel = heatLevel + Integer.parseInt(Config.get("HighTrigger"));
                        break;
                    case 1:
                        heatLevel = heatLevel + Integer.parseInt(Config.get("MidTrigger"));
                        break;
                    default:
                        heatLevel = heatLevel + Integer.parseInt(Config.get("LowTrigger"));
                        break;
                }
            }

            if(heatLevel >= Integer.parseInt(Config.get("QuarantineLimit"))){
                quarantineUser(guildID,userID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void quarantineUser(final Long guildID, final Long userID) {
        Guild guild = SherlockBot.jda.getGuildById(guildID);
        if (guild != null) {

            guild.timeoutForById(userID, Duration.ofDays(1)).queue();

            //Member member = guild.getMember(userID);
            Role quarantineRole = guild.getRoleById(SherlockBot.guildMap.get(guildID).getQuarantineRoleID());
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

                        guild.modifyMemberRoles(foundMember, rolesToAdd, rolesToRemove).reason("Quarantining Member for Investigation").queue(success -> {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTitle("Quarantined User");
                            builder.setDescription("User has been quarantined for exceeding allowed limits");
                            builder.addField("User Tag:", foundMember.getUser().getAsTag(), true);
                            builder.addField("User ID:", foundMember.getId(), true);
                            builder.addField("Removed Roles:", roleString.toString(), false);
                            builder.setThumbnail(foundMember.getEffectiveAvatarUrl());
                            builder.setTimestamp(Instant.now());
                            builder.setColor(SherlockBot.getColor(SherlockBot.colorType.QUARANTINE));

                            LogMessage.sendLogMessage(guildID, builder.build());
                            builder.clear();
                        });
                    }
                });
            }
        }
    }
}
