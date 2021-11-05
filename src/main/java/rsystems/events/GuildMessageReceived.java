package rsystems.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import rsystems.SherlockBot;
import rsystems.handlers.LogMessage;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuildMessageReceived extends ListenerAdapter {

    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {

        //Monitor users AND bots
        if (event.getMessage().getInvites().size() > 0) {
            try {
                List<Long> whiteListedGuilds = SherlockBot.database.getLongMultiple("InviteWhitelist", "TargetGuildID", "ChildGuildID", event.getGuild().getIdLong());

                for(String code:event.getMessage().getInvites()){
                    Invite.resolve(SherlockBot.jda,code).queue(resolvedInvite -> {
                        Long targetGuildID = resolvedInvite.getGuild().getIdLong();
                        if((event.getGuild().getIdLong() == targetGuildID) || (whiteListedGuilds.contains(targetGuildID))){
                            // ID is ok
                        } else {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTitle("Discord Link Detection");
                            builder.setDescription("User posted unauthorized discord link:\n" + resolvedInvite.getUrl());
                            builder.setColor(Color.yellow);
                            if(event.getMessage().getMember().getAsMention() != null){
                                builder.addField("User:",String.format(event.getMessage().getMember().getAsMention() + "\n%s\n%s",event.getAuthor().getAsTag(),event.getAuthor().getId()),true);
                            } else {
                                builder.addField("User:", String.format("n%s\n%s", event.getAuthor().getAsTag(), event.getAuthor().getId()), true);
                            }
                            builder.addField("Target Guild:",String.format("%s\n%d",resolvedInvite.getGuild().getName(),resolvedInvite.getGuild().getIdLong()),true);
                            builder.setTimestamp(Instant.now());

                            LogMessage.sendLogMessage(event.getGuild().getIdLong(),builder.build());

                            builder.clear();

                            event.getMessage().delete().reason("User posted discord invite link").queue(DeleteSuccess -> {

                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                embedBuilder.setDescription("Sorry, Only authorized Discord servers can have invite links posted here.  Please refrain from posting any other invite links as an automatic punishment will take place.");
                                embedBuilder.setColor(Color.yellow);
                                embedBuilder.setFooter("This action has been logged");
                                event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                                embedBuilder.clear();
                            });
                        }
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return;
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        // USERS ONLY

        final String guildPrefix = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getPrefix();
        String message = event.getMessage().getContentRaw();

        final boolean defaultPrefixFound = message.toLowerCase().startsWith(SherlockBot.defaultPrefix.toLowerCase());
        if ((defaultPrefixFound) || ((guildPrefix != null) && (message.toLowerCase().startsWith(guildPrefix.toLowerCase())))) {
            //PREFIX FOUND

            Long guildID = event.getGuild().getIdLong();

            String content = null;
            if (defaultPrefixFound) {
                content = message.substring(SherlockBot.defaultPrefix.length());
            } else {
                content = message.substring(guildPrefix.length());
            }

            try {
                if (SherlockBot.database.getLong("IgnoreChannelTable", "ChannelID", "ChildGuildID", event.getGuild().getIdLong(), "ChannelID", event.getChannel().getIdLong()) == null) {


                    //SELF ROLES
                    if (SherlockBot.database.getTableCount(event.getGuild().getIdLong(), "SelfRoles") > 0) {

                        Map<String, Long> guildSelfRoleMap = SherlockBot.database.getGuildSelfRoles(guildID);

                        //ITERATE THROUGH GUILD SELF ROLE MAP
                        for (Map.Entry<String, Long> entry : guildSelfRoleMap.entrySet()) {
                            //check content for trigger (Ignoring case)
                            if (entry.getKey().equalsIgnoreCase(content)) {
                                //ENTRY FOUND
                                Long roleID = entry.getValue();
                                handleSelfRoleEvent(event, roleID);
                            }
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return;
        }


        // SPAM MONITORING
        event.getChannel().getHistoryBefore(event.getMessage(), 12).queue(messageHistory -> {

            ArrayList<String> messages = new ArrayList<>();
            for (Message m : messageHistory.getRetrievedHistory()) {

                if (m.getAuthor().isBot()) {
                    continue;
                }

                if (m.getContentRaw().equalsIgnoreCase(event.getMessage().getContentRaw())) {
                    if (m.getAuthor().getIdLong() == event.getAuthor().getIdLong()) {
                        messages.add(m.getId());
                    }
                }

                if (messages.size() >= 3) {
                    messages.add(event.getMessageId());
                    event.getChannel().purgeMessagesById(messages);

                    if (SherlockBot.guildMap.get(event.getGuild().getIdLong()).getLogChannelID() != null) {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle("Spam Detection - " + event.getAuthor().getAsTag())
                                .setDescription(String.format("`Message:`\n%s\n\n" + TimeFormat.RELATIVE.now(), m.getContentRaw()));
                        builder.addField("User:", event.getAuthor().getAsMention(), false);
                        builder.setFooter(String.format("Tag: %s | ID: %s", event.getAuthor().getAsTag(), event.getAuthor().getId()));
                        builder.setColor(Color.decode("#FFCC37"));

                        LogMessage.sendLogMessage(event.getGuild().getIdLong(), builder.build());

                        builder.clear();
                    }

                    EmbedBuilder notification = new EmbedBuilder();
                    notification.setTimestamp(Instant.now())
                            .setTitle("Spam Detection")
                            .setDescription(String.format("%s has been muted for 1 minute\n\n", event.getMember().getEffectiveName()))
                            .addField("Reason:", "Similar-Messages / Spam", false)
                            .setFooter(String.format("%s | %s", event.getAuthor().getAsTag(), event.getAuthor().getId()));
                    notification.setColor(Color.decode("#9837FF"));

                    event.getChannel().sendMessageEmbeds(notification.build()).queue();
                    notification.clear();
                    break;
                }
            }
        });
    }

    private void handleSelfRoleEvent(final GuildMessageReceivedEvent event, final Long roleID) {

        Role role = event.getGuild().getRoleById(roleID);
        if (role != null) {

            try {

                if (event.getMember().getRoles().contains(role)) {
                    event.getGuild().removeRoleFromMember(event.getMember().getIdLong(), role).reason("Requested via SelfRole").queue(success -> {
                        event.getMessage().addReaction("\uD83D\uDC4D ").queue();
                    });
                } else {
                    event.getGuild().addRoleToMember(event.getMember().getIdLong(), role).reason("Requested via SelfRole").queue(success -> {
                        event.getMessage().addReaction("\uD83D\uDC4D ").queue();
                    });
                }

            } catch (PermissionException permissionException) {
                event.getMessage().reply("Missing Permissions: " + permissionException.getPermission().toString()).queue();
                event.getMessage().addReaction("⚠").queue();
            }
        }
    }


}
