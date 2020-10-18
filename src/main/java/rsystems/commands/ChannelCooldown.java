package rsystems.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Character.isDigit;
import static rsystems.SherlockBot.database;

public class ChannelCooldown extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (SherlockBot.commands.get(13).checkCommandMod(event.getMessage(), 2)) {

            String[] args = event.getMessage().getContentRaw().split("\\s+");

            if ((args.length > 1) && (args[1].equalsIgnoreCase("reset"))) {

                /*
                Attempt to remove cooldown from channel if a cooldown was found on defined channel.
                 */
                if (endChannelCooldown(event.getGuild(), event.getChannel())) {
                    if (event.getChannel().getName().contains("\uD83E\uDD76")) {
                        int lastFlakeLocation = event.getChannel().getName().lastIndexOf("\uD83E\uDD76");
                        event.getChannel().getManager().setName(event.getChannel().getName().substring(0, lastFlakeLocation)).queueAfter(10, TimeUnit.SECONDS);
                    }
                    event.getMessage().addReaction("\uD83D\uDE07").queue();
                }

            } else {

                LocalDateTime expireDateTime = LocalDateTime.now().plusHours(1);

                if (event.getMessage().getContentDisplay().contains("-t ")) {

                    StringBuilder numberString = new StringBuilder();
                    //Parse the numberString into a integer
                    int number = 0;
                    String chronoType = "";

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
                            expireDateTime = expireDateTime.plus(number, ChronoUnit.MINUTES);
                            break;
                        case 'h':
                        case 'H':
                            expireDateTime = expireDateTime.plus(number, ChronoUnit.HOURS);
                            break;
                        case 'd':
                        case 'D':
                            expireDateTime = expireDateTime.plus(number, ChronoUnit.DAYS);
                            break;
                        default:
                            event.getChannel().sendMessage("Incorrect command formatting used").queue();
                            return;
                    }
                }

                event.getChannel().sendMessage("This channel is being put on a temporary cooldown.  Please use other channels.").queue();
                startChannelCooldown(event.getGuild(), event.getChannel(), expireDateTime);

                event.getChannel().getManager().setName(event.getChannel().getName() + "\uD83E\uDD76").queueAfter(10, TimeUnit.SECONDS);
            }

        }
    }

    private void startChannelCooldown(Guild guild, TextChannel channel, LocalDateTime expirationDate) {
        System.out.println("Starting cooldown sequence");

        LocalDateTime currentDateTime = LocalDateTime.now();


        /*
        if (!channel.getRolePermissionOverrides().contains(guild.getPublicRole())) {
            try{
                channel.createPermissionOverride(guild.getPublicRole()).setDeny(Permission.MESSAGE_WRITE).queue();
                database.insertTimedEvent(guild.getIdLong(), channel.getIdLong(), 2, "Channel Cooldown", guild.getPublicRole().getIdLong(), 0, currentDateTime, expirationDate);
            } catch(IllegalStateException e){
                System.out.println("Could not create override for " + guild.getPublicRole());
            }
        }

         */


        for (PermissionOverride permissionOverride : channel.getRolePermissionOverrides()) {
                int currentState = 0;

                if(permissionOverride.getRole().isPublicRole()){
                    System.out.println("debug");
                }

                if (permissionOverride.getAllowed().contains(Permission.MESSAGE_WRITE)) {
                    currentState = 1;
                } else if (permissionOverride.getDenied().contains(Permission.MESSAGE_WRITE)) {
                    currentState = 2;
                }

                System.out.println("Attempting to set override for Role:" + permissionOverride.getRole().getName());
                database.insertTimedEvent(guild.getIdLong(), channel.getIdLong(), 2, "Channel Cooldown", permissionOverride.getRole().getIdLong(), currentState, currentDateTime, expirationDate);
                channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).deny(Permission.MESSAGE_WRITE).queue();
        }
    }

    public boolean endChannelCooldown(Guild guild, TextChannel channel) {
        System.out.println("Resetting cooldown for Channel");
        HashMap<Long, Integer> eventMap = new HashMap<>();
        eventMap.putAll(database.removeCooldown(guild.getIdLong(), channel.getIdLong()));
        if (eventMap.size() > 0) {
            System.out.println("debug size:" + eventMap.size());
            for (PermissionOverride permissionOverride : channel.getPermissionOverrides()) {
                if (permissionOverride.isRoleOverride()) {
                    System.out.println("Attempting to reset override for Role:" + permissionOverride.getRole().getName());
                    try {
                        if (eventMap.get(permissionOverride.getIdLong()) == 1) {
                            channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).grant(Permission.MESSAGE_WRITE).queue();
                        } else if (eventMap.get(permissionOverride.getIdLong()) == 2) {
                            channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).deny(Permission.MESSAGE_WRITE).queue();
                        } else {
                            channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).clear(Permission.MESSAGE_WRITE).queue();
                        }
                    } catch (NullPointerException e) {
                        System.out.println("Did not find a permission override for ID: " + permissionOverride.getId());
                    }
                }
            }
            return true;
        }
        return false;
    }
}
