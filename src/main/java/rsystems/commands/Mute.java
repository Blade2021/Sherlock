package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogChannel;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Character.isDigit;
import static rsystems.SherlockBot.database;

public class Mute extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
            return;
        }

        if (event.isFromGuild()) {
            String[] args = event.getMessage().getContentRaw().split("\\s+");

            /*
                            MUTE USER COMMAND
             */
            if (SherlockBot.commands.get(2).checkCommandMod(event.getMessage(),0)) {
                LogChannel logChannel = new LogChannel();
                ArrayList<Member> mutedUsers = new ArrayList<>();

                /*
                GET A LIST OF MEMBERS
                 */
                List<Member> MentionedMembers = event.getMessage().getMentionedMembers();
                if (MentionedMembers.size() > 0) {
                    for (Member m : MentionedMembers) {
                        if (muteUser(event.getGuild(), m, event.getChannel(), event.getMember())) {
                            mutedUsers.add(m);
                        }
                    }

                } else {
                    // Try to mute user using USERID
                    try {
                        if ((args.length > 1) && (event.getGuild().getSelfMember().canInteract(Objects.requireNonNull(event.getGuild().getMemberById(args[1]))))) {
                            if (muteUser(event.getGuild(), event.getGuild().getMemberById(args[1]), event.getChannel(), event.getMember())) {
                                mutedUsers.add(event.getGuild().getMemberById(args[1]));
                            }
                        }
                    } catch (NullPointerException e) {
                        System.out.println("Could not find user to mute");
                        event.getMessage().addReaction("⚠").queue();
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage("What in tarnation is that>?").queue();
                        event.getMessage().addReaction("⚠").queue();
                    }
                }

                /*
                Start initiating variables for output to logger & notification call
                 */
                boolean timedEvent = false;
                // Grab the first date
                LocalDateTime currentDateTime = LocalDateTime.now();
                // Initiate the new date variable
                LocalDateTime newDateTime = null;
                StringBuilder numberString = new StringBuilder();
                //Parse the numberString into a integer
                int number = 0;
                String chronoType = "";


                if (event.getMessage().getContentDisplay().contains("-t ")) {
                    //Grab the string AFTER -t, excluding the first 3 characters (Length of -t )
                    String timeArgument = event.getMessage().getContentDisplay().substring(event.getMessage().getContentDisplay().indexOf("-t ") + 3);
                    char chronoUnit = 0;


                    // Parse through the string character by character until a digit is not found, then move the char for processing
                    for (Character c : timeArgument.toCharArray()) {
                        if (isDigit(c)) {
                            numberString.append(c);
                        } else if(c == ' '){
                            // do nothing (skip spaces)
                        } else {
                            chronoUnit = c;
                            break;
                        }
                    }
                    //Parse the numberString into a integer for incrementing the date
                    number = Integer.parseInt(numberString.toString());

                    //See what type of unit to use
                    switch (chronoUnit) {
                        case 'm':
                        case 'M':
                            newDateTime = currentDateTime.plus(number, ChronoUnit.MINUTES);
                            timedEvent = true;
                            chronoType = "Minutes";
                            break;
                        case 'h':
                        case 'H':
                            newDateTime = currentDateTime.plus(number, ChronoUnit.HOURS);
                            timedEvent = true;
                            chronoType = "Hours";
                            break;
                        case 'd':
                        case 'D':
                            newDateTime = currentDateTime.plus(number, ChronoUnit.DAYS);
                            timedEvent = true;
                            chronoType = "Days";
                            break;
                        default:
                            event.getChannel().sendMessage("Incorrect command formatting used").queue();
                            return;
                    }
                }

                /*
                Get a reason if applicable.
                 */
                String reason = "No reason given";
                if (event.getMessage().getContentDisplay().contains("-r ")) {

                    int timeParamLoc = 0;
                    if (event.getMessage().getContentDisplay().contains("-t ")) {
                        timeParamLoc = event.getMessage().getContentDisplay().indexOf("-t ");
                    }
                    int reasonParamLoc = event.getMessage().getContentDisplay().indexOf("-r ");
                    if(reasonParamLoc < timeParamLoc){
                        reason = event.getMessage().getContentDisplay().substring(reasonParamLoc + 3,timeParamLoc);
                    } else {
                        reason = event.getMessage().getContentDisplay().substring(reasonParamLoc + 3);
                    }

                    if(reason.length() > 60){
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Reason length is too long").queue();
                        event.getMessage().addReaction("❌").queue();
                        return;
                    }

                }

                //Add all members to database if mute is timed
                for (Member m : mutedUsers) {
                    if(event.getGuild().getSelfMember().canInteract(m)){

                        if(timedEvent){
                            if(database.insertTimedEvent(event.getGuild().getIdLong(), m.getIdLong(), 1, reason, currentDateTime, newDateTime)){
                                event.getMessage().addReaction("\uD83D\uDD50").queue();
                                logChannel.logMuteAction(event.getGuild(),reason,m.getUser(),event.getMember(),number,chronoType);
                            }
                        } else {
                            logChannel.logMuteAction(event.getGuild(), reason, m.getUser(), event.getMember());
                        }

                        try {
                            final boolean finalTimedEvent = timedEvent;
                            final int finalNumber = number;
                            final String finalChronoType = chronoType;
                            final String finalReason = reason;
                            m.getUser().openPrivateChannel().queue((channel) -> {
                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                embedBuilder.setTitle("Notification")
                                        .setColor(Color.RED)
                                        .addField("Guild",event.getGuild().getName(),false)
                                        .addField("Reason",finalReason,false)
                                        .addField("Moderator",event.getMember().getEffectiveName(),true)
                                        .addField("Moderator ID",event.getMember().getId(),true);

                                if(finalTimedEvent){
                                    embedBuilder.setDescription(String.format("You have been muted in a guild server for %d %s", finalNumber, finalChronoType));
                                } else {
                                    embedBuilder.setDescription("You have been muted in a guild server.");
                                }
                                channel.sendMessage(embedBuilder.build()).queue(null,failure -> {
                                    logChannel.logAction(event.getGuild(),"Direct Message Failed", "Attempted to send direct message to user but failed due to Privacy Settings", m,1);
                                });
                                embedBuilder.clear();
                            });
                        } catch (NullPointerException e){

                        } catch (ErrorResponseException e){
                            if(e.getErrorCode() == 50007){
                                logChannel.logAction(event.getGuild(), "Direct Message Failed", "Attempted to send direct message to user but failed due to Privacy Settings", m,1);
                            }
                        }
                    }
                }
            }


            /*
                            UN-MUTE COMMAND
             */
            if (SherlockBot.commands.get(3).checkCommandMod(event.getMessage(),0)) {

                LogChannel logChannel = new LogChannel();
                ArrayList<Member> qualifiedMembers = new ArrayList<>();

                //Get a list of members
                if(event.getMessage().getMentionedMembers().size() > 0){
                    qualifiedMembers.addAll(event.getMessage().getMentionedMembers());
                } else {

                    try {
                        if ((args.length > 1) && (event.getGuild().getSelfMember().canInteract(Objects.requireNonNull(event.getGuild().getMemberById(args[1]))))) {
                            if (muteUser(event.getGuild(), event.getGuild().getMemberById(args[1]), event.getChannel(), event.getMember())) {
                                event.getMessage().addReaction("✅").queue();
                                qualifiedMembers.add(event.getGuild().getMemberById(args[1]));
                            }
                        }
                    } catch (NullPointerException e) {
                        System.out.println("Could not find user to mute");
                        event.getMessage().addReaction("⚠").queue();
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage("What in tarnation is that>?").queue();
                        event.getMessage().addReaction("⚠").queue();
                    }
                }

                for(Member m: qualifiedMembers){
                    if(database.getTimedEventsQuantity(event.getGuild().getIdLong(),m.getIdLong()) > 0){
                        database.expireTimedEvent(event.getGuild().getIdLong(),m.getIdLong());
                    }

                    try{
                        event.getGuild().removeRoleFromMember(m,event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getId()).getMuteRoleID())).reason("Called by: " + event.getAuthor().getAsTag()).queue();
                        event.getMessage().addReaction("✅").queue();
                    } catch(PermissionException e){
                        logChannel.logAction(event.getGuild(),"Missing Permissions","Unable to remove mute role from user",m);
                        event.getMessage().addReaction("⚠").queue();
                    } catch(NullPointerException e){
                        event.getMessage().addReaction("⚠").queue();
                    }
                }
            }
        }
    }

    private boolean muteUser(Guild guild, Member member, MessageChannel channel, Member requester) {
        try {
            guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(SherlockBot.guildMap.get(guild.getId()).getMuteRoleID()))).reason("Called by: " + requester.getUser().getAsTag()).queue();
            return true;
        } catch (HierarchyException e) {
            channel.sendMessage("That user is above me in rank!").queue();
        } catch (NullPointerException e) {
            channel.sendMessage("Could not find mute role").queue();
        } catch (NumberFormatException e) {
            channel.sendMessage("What in tarnation was that>?").queue();
        }
        return false;
    }

}
