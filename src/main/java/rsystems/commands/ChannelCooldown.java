package rsystems.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static rsystems.SherlockBot.database;

public class ChannelCooldown extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (SherlockBot.commands.get(13).checkCommandMod(event.getMessage(),2)) {

            String[] args = event.getMessage().getContentRaw().split("\\s+");

            if((args.length > 1) && (args[1].equalsIgnoreCase("reset"))){
                endChannelCooldown(event.getGuild(), event.getChannel());

                if (event.getChannel().getName().contains("\uD83E\uDD76")) {
                    int lastFlakeLocation = event.getChannel().getName().lastIndexOf("\uD83E\uDD76");
                    event.getChannel().getManager().setName(event.getChannel().getName().substring(0, lastFlakeLocation)).queueAfter(10,TimeUnit.SECONDS);
                }

                event.getMessage().addReaction("\uD83D\uDE07").queue();

                return;
            } else {

                event.getChannel().sendMessage("This channel is being put on a temporary cooldown.  Please use other channels.").queue();
                startChannelCooldown(event.getGuild(), event.getChannel(), 1, 'h');

                event.getChannel().getManager().setName(event.getChannel().getName() + "\uD83E\uDD76").queueAfter(10, TimeUnit.SECONDS);
            }

        }
    }

    private void startChannelCooldown(Guild guild, TextChannel channel, int timeValue, char timeType) {
        System.out.println("Starting cooldown sequence");

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime expireDateTime = currentDateTime.plusHours(1);

        if(!channel.getPermissionOverrides().contains(guild.getPublicRole())){
            channel.createPermissionOverride(guild.getPublicRole()).setDeny(Permission.MESSAGE_WRITE).queue();
            database.insertTimedEvent(guild.getIdLong(), channel.getIdLong(), 2, "Channel Cooldown", guild.getPublicRole().getIdLong(), 0, currentDateTime, expireDateTime);
        }

        for (PermissionOverride permissionOverride : channel.getPermissionOverrides()) {
            if (permissionOverride.isRoleOverride()) {
                int currentState = 0;

                if (permissionOverride.getAllowed().contains(Permission.MESSAGE_WRITE)) {
                    currentState = 1;
                } else if (permissionOverride.getDenied().contains(Permission.MESSAGE_WRITE)) {
                    currentState = 2;
                }

                System.out.println("Attempting to set override for Role:" + permissionOverride.getRole().getName());
                database.insertTimedEvent(guild.getIdLong(), channel.getIdLong(), 2, "Channel Cooldown", permissionOverride.getRole().getIdLong(), currentState, currentDateTime, expireDateTime);
                channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).setDeny(Permission.MESSAGE_WRITE).queue();
            }
        }
    }

    private void endChannelCooldown(Guild guild, TextChannel channel) {
        System.out.println("Resetting cooldown for Channel");
        HashMap<Long, Integer> eventMap = new HashMap<>();
        eventMap.putAll(database.removeCooldown(guild.getIdLong(), channel.getIdLong()));
        for (PermissionOverride permissionOverride : channel.getPermissionOverrides()) {
            if (permissionOverride.isRoleOverride()) {
                System.out.println("Attempting to reset override for Role:" + permissionOverride.getRole().getName());
                try {
                    if (eventMap.get(permissionOverride.getIdLong()) == 1) {
                        channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).setAllow(Permission.MESSAGE_WRITE).queue();
                    } else if (eventMap.get(permissionOverride.getIdLong()) == 2) {
                        channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).setDeny(Permission.MESSAGE_WRITE).queue();
                    } else {
                        channel.upsertPermissionOverride(permissionOverride.getPermissionHolder()).clear(Permission.MESSAGE_WRITE).queue();
                    }
                } catch (NullPointerException e) {
                    System.out.println("Did not find a permission override for ID: " + permissionOverride.getId());
                }
            }
        }
    }
}
