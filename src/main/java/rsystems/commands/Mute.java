package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
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
        }

        if (event.isFromGuild()) {
            String[] args = event.getMessage().getContentRaw().split("\\s+");

            // MUTE COMMAND
            if (SherlockBot.commands.get(2).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
                LogChannel logChannel = new LogChannel();

                ArrayList<Member> mutedUsers = new ArrayList<>();

                //Get a list of members
                List<Member> MentionedMembers = event.getMessage().getMentionedMembers();
                if (MentionedMembers.size() > 0) {
                    for (Member m : MentionedMembers) {
                        if (muteUser(event.getGuild(), m, event.getChannel(), event.getMember())) {
                            event.getMessage().addReaction("✅").queue();
                            logChannel.logAction(event.getGuild(), "Muted User", MentionedMembers, event.getMember());
                            mutedUsers.add(m);
                        }
                    }

                } else {
                    // Try to mute user using USERID
                    try {
                        if ((args.length > 1) && (event.getGuild().getSelfMember().canInteract(Objects.requireNonNull(event.getGuild().getMemberById(args[1]))))) {
                            if (muteUser(event.getGuild(), event.getGuild().getMemberById(args[1]), event.getChannel(), event.getMember())) {
                                event.getMessage().addReaction("✅").queue();
                                mutedUsers.add(event.getGuild().getMemberById(args[1]));
                                logChannel.logAction(event.getGuild(), "Muted User", event.getGuild().getMemberById(args[1]).getUser());
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


                boolean timedEvent = false;

                if (event.getMessage().getContentDisplay().contains("-t")) {
                    //Grab the string AFTER -t, excluding the first 3 characters (Length of -t )
                    String timeArgument = event.getMessage().getContentDisplay().substring(event.getMessage().getContentDisplay().indexOf("-t") + 3);

                    //Initiate the string builder to store the number of integers found.
                    StringBuilder numberString = new StringBuilder();
                    int timeValue = 0; //Store the type of time scale

                    // Parse through the string character by character until a digit is not found, then check the char for what timescale to use
                    for (Character c : timeArgument.toCharArray()) {
                        if (isDigit(c)) {
                            numberString.append(c);
                        } else {
                            if (c.toString().equalsIgnoreCase("m")) {
                                timeValue = 1;
                            } else if (c.toString().equalsIgnoreCase("h")) {
                                timeValue = 2;
                            } else if (c.toString().equalsIgnoreCase("d")) {
                                timeValue = 3;
                            } else {
                                event.getChannel().sendMessage("Incorrect command formatting used").queue();
                            }
                        }
                    }
                    //Parse the numberString into a integer
                    int finalNumber = Integer.parseInt(numberString.toString());

                    // Grab the first date
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    // Initiate the new date variable
                    LocalDateTime newDateTime;
                    switch (timeValue) {
                        case 1:
                            newDateTime = currentDateTime.plus(finalNumber, ChronoUnit.MINUTES);
                            timedEvent = true;
                            break;
                        case 2:
                            newDateTime = currentDateTime.plus(finalNumber, ChronoUnit.HOURS);
                            timedEvent = true;
                            break;
                        case 3:
                            newDateTime = currentDateTime.plus(finalNumber, ChronoUnit.DAYS);
                            timedEvent = true;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + timeValue);
                    }

                    //Add all members to database if mute is timed
                    for (Member m : mutedUsers) {
                        if(database.insertTimedEvent(event.getGuild().getIdLong(), m.getIdLong(), 1, currentDateTime, newDateTime)){
                            event.getMessage().addReaction("\uD83D\uDD50").queue();
                        }
                    }
                }

                //todo Reformat code to allow proper looping
                //todo Change notification method to support timed values if applicable

                //Add all members to database if mute is timed
                for (Member m : mutedUsers) {
                    if(event.getGuild().getSelfMember().canInteract(m)){
                        try {
                            m.getUser().openPrivateChannel().queue(channel -> {
                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                embedBuilder.setTitle("Notification")
                                        .setDescription("You have been muted in a guild server")
                                        .setColor(Color.RED)
                                        .addField("Guild",event.getGuild().getName(),false)
                                        .addField("Moderator",event.getMember().getEffectiveName(),true)
                                        .addField("Moderator ID",event.getMember().getId(),true);
                                channel.sendMessage(embedBuilder.build()).queue();
                                embedBuilder.clear();
                            });
                        } catch (NullPointerException e){

                        } catch (ErrorResponseException e){
                            if(e.getErrorCode() == 50007){
                                //todo log error to channel
                            }
                        }
                    }
                }
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
                } catch (NullPointerException e) {

                }

                LogChannel logChannel = new LogChannel();
                logChannel.logAction(event.getGuild(), "Unmuted User", MentionedMembers, event.getMember());

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
