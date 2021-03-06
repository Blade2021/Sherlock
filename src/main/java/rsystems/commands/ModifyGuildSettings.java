package rsystems.commands;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.objects.RoleReactionObject;

import java.awt.*;
import java.util.List;
import java.util.*;

import static rsystems.SherlockBot.database;

public class ModifyGuildSettings extends ListenerAdapter {

    private Map<String, Integer> userMap = new HashMap<>();  //User ID, TextChannel of origin
    private Map<String, String> channelGuildMap = new HashMap<>();  //User ID, TextChannel of origin

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
            return;
        }
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        //CHANGE PREFIX
        if (SherlockBot.commandMap.get(8).checkCommand(event.getMessage())) {
            try {
                database.putValue("GuildTable", "Prefix", "GuildID", event.getGuild().getIdLong(), args[1]);
                SherlockBot.guildMap.get(event.getGuild().getId()).setPrefix(args[1]);
                event.getMessage().addReaction("✅").queue();
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                try {
                    event.getMessage().addReaction("⚠").queue();
                } catch (Exception global_exception) {
                    global_exception.printStackTrace();
                }
            }
        }

        // GET/SET LOG CHANNEL ID
        if (SherlockBot.commandMap.get(6).checkCommand(event.getMessage())) {
            try {
                if (args.length > 1) {
                    String newID = ""; // Initialize a variable for storing ID

                    //If a text channel is mentioned
                    if (event.getMessage().getMentionedChannels().size() > 0) {
                        if (event.getMessage().getMentionedChannels().get(0) != null) {
                            newID = event.getMessage().getMentionedChannels().get(0).getId();
                        }

                    } else {
                        // No channel was mentioned, try using the ID call
                        try {
                            if (event.getGuild().getTextChannelById(args[1]).getId() != null) {
                                newID = args[1];
                            } else {
                                event.getMessage().addReaction("⚠").queue();
                                System.out.println("Null found when setting LogChannel | " + event.getGuild().getId());
                                return;
                            }
                        } catch (NumberFormatException e) {
                            event.getMessage().addReaction("⚠").queue();
                            System.out.println("NumberFormatException | " + event.getGuild().getId());
                            return;
                        }
                    }

                    database.putValue("GuildTable", "LogChannelID", "GuildID", event.getGuild().getIdLong(), Long.valueOf(newID));
                    SherlockBot.guildMap.get(event.getGuild().getId()).setLogChannelID(newID);
                    event.getMessage().addReaction("✅").queue();
                } else {
                    String logChannelID = SherlockBot.guildMap.get(event.getGuild().getId()).getLogChannelID();
                    if ((logChannelID != null) && !(logChannelID.equalsIgnoreCase("0")) && (event.getGuild().getTextChannelById(logChannelID).canTalk())) {
                        event.getChannel().sendMessage(event.getGuild().getTextChannelById(logChannelID).getAsMention() + "\nLogChannelID: " + logChannelID).queue();
                    } else {
                        event.getMessage().reply("No log channel has been set  \uD83E\uDDD0").queue();
                    }
                }
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                try {
                    event.getMessage().addReaction("⚠").queue();
                    System.out.println("Catch error when setting logChannel | " + event.getGuild().getId());
                } catch (Exception global_exception) {
                    global_exception.printStackTrace();
                }
            }
        }

        //CHANGE Embed filter level
        if (SherlockBot.commandMap.get(14).checkCommand(event.getMessage())) {
            try {
                if (args.length <= 1) {
                    event.getChannel().sendMessage(String.format("%s The current embed filter setting: %d\nPlease use `%sEmbedFilter [0-3]` to set a different value.",
                            event.getAuthor().getAsMention(),
                            SherlockBot.guildMap.get(event.getGuild().getId()).getEmbedFilter(),
                            SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix()))
                            .queue();
                } else {
                    try {
                        SherlockBot.guildMap.get(event.getGuild().getId()).setEmbedFilter(Integer.parseInt(args[1]));
                        database.putValue("GuildTable", "EmbedFilter", "GuildID", event.getGuild().getIdLong(), Integer.parseInt(args[1]));
                        event.getMessage().addReaction("✅").queue();
                        return;
                    } catch (NumberFormatException | NullPointerException e) {
                        event.getMessage().addReaction("⚠").queue();
                    }
                }
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                try {
                    event.getMessage().addReaction("⚠").queue();
                } catch (Exception global_exception) {
                    global_exception.printStackTrace();
                }
            }
        }

        /*
                        SELF ROLES CONFIGURATION
         */

        /*
        ADD ROLE TO SELF ROLES OF GUILDMAP
         */
        if (SherlockBot.commandMap.get(5).checkCommand(event.getMessage())) {
            try {
                if ((args.length >= 3) && (event.getGuild().getRoleById(args[2]) != null)) {

                    // Add role to self roles of guildmap
                    SherlockBot.guildMap.get(event.getGuild().getId()).addSelfRole(args[1], Long.valueOf(args[2]));
                    int databaseStatusCode = database.insertSelfRole(event.getGuild().getIdLong(), args[1], Long.valueOf(args[2]));
                    System.out.println("Database Status Code: " + databaseStatusCode);
                    if (databaseStatusCode == 200) {
                        // Success (1+ updated)
                        event.getMessage().addReaction("✅").queue();
                        return;
                    } else {
                        switch (databaseStatusCode) {
                            case 201:
                                event.getMessage().reply("ERROR 201: You have hit the maximum number of self roles.  Please try to remove some before continuing.").queue();
                                break;
                            case 400:
                                event.getMessage().reply("ERROR 400: Database had an unknown error.  This event has been logged.").queue();
                        }
                        // Unsuccessful (0 rows updated)
                        database.logError(event.getGuild().getId(), "Failed to add self role", databaseStatusCode);
                    }
                } else {
                    // Request failed internal checks
                    if (args.length < 3) {

                        event.getMessage().reply("Not enough arguments supplied.").queue();

                    } else if (event.getGuild().getRoleById(args[2]) == null) {

                        // Cannot find role associated with ID provided
                        event.getMessage().reply("I could not find a role associated with that ID.").queue();

                    }
                    event.getMessage().addReaction("⚠").queue();
                }
            } catch (NullPointerException e) {
                event.getMessage().addReaction("⚠").queue();
            } catch (NumberFormatException e) {
                event.getMessage().reply("What in tarnation is that>?").queue();
                event.getMessage().addReaction("⚠").queue();
            }
        }

        /*
        REMOVE ROLE FROM SELF ROLES OF GUILDMAP
         */
        if (SherlockBot.commandMap.get(4).checkCommand(event.getMessage())) {
            if (args.length >= 1) {
                if (SherlockBot.guildMap.get(event.getGuild().getId()).selfRoleMap.containsKey(args[1])) {
                    SherlockBot.guildMap.get(event.getGuild().getId()).removeSelfRole(args[1]);

                    int rowUpdateCount = database.removeSelfRole(event.getGuild().getIdLong(), args[1]);

                    // Success (1+ updated)
                    if (rowUpdateCount > 0) {
                        System.out.println(String.format("Guild: %s | Role CMD: %s | Count: %d", event.getGuild().getId(), args[1], rowUpdateCount));
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        // Unsuccessful (0 rows updated)
                        event.getMessage().addReaction("❌").queue();
                        database.logError(event.getGuild().getId(), "Failed to remove self role", 400);
                    }
                }
            }
        }

        /*
                        LANGUAGE FILTER CONFIGURATION
         */

        /*
        ADD WORD TO LANGUAGE FILTER
         */
        if (SherlockBot.commandMap.get(10).checkCommand(event.getMessage())) {

            try {
                event.getAuthor().openPrivateChannel().queue((channel) -> {
                    try {
                        event.getMessage().addReaction("\uD83D\uDCE8 ").queue();
                        event.getMessage().reply("Please check your private messages for further information").queue();
                    } catch (NullPointerException e) {

                    }

                    channel.sendMessage("Please type the words to ADD here, one word per message.  All characters will be recorded.  Type done when finished").queue();
                    userMap.putIfAbsent(event.getAuthor().getId(), 1);
                    channelGuildMap.putIfAbsent(event.getAuthor().getId(), event.getGuild().getId());
                });
            } catch (NullPointerException e) {
                System.out.println("Failed to initiate private channel with USER:" + event.getAuthor().getId());
            }
        }

        /*
        REMOVE WORD FROM LANGUAGE FILTER
         */
        if (SherlockBot.commandMap.get(11).checkCommand(event.getMessage())) {
            try {
                event.getAuthor().openPrivateChannel().queue((channel) -> {
                    try {
                        event.getMessage().addReaction("\uD83D\uDCE8 ").queue();
                        event.getMessage().reply("Please check your private messages for further information").queue();
                    } catch (NullPointerException e) {

                    }

                    channel.sendMessage("Please type the words to REMOVE here, one word per message.  All characters will be recorded.  Type done when finished").queue();
                    userMap.putIfAbsent(event.getAuthor().getId(), 2);
                    channelGuildMap.putIfAbsent(event.getAuthor().getId(), event.getGuild().getId());
                });
            } catch (NullPointerException e) {
                System.out.println("Failed to initiate private channel with USER:" + event.getAuthor().getId());
            }
        }

        /*
        GET A LIST OF BADWORDS FROM LANGUAGE FILTER
         */
        if (SherlockBot.commandMap.get(12).checkCommand(event.getMessage())) {
            try {
                event.getAuthor().openPrivateChannel().queue((channel) -> {

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Listed Bad Words")
                            .setColor(Color.RED)
                            .addField("Guild:", event.getGuild().getName(), true)
                            .addField("Guild ID:", event.getGuild().getId(), true)
                            .addField("Bad Words:", SherlockBot.guildMap.get(event.getGuild().getId()).getBlacklistedWords().toString(), false);

                    channel.sendMessage(embedBuilder.build()).queue(success -> {
                        try {
                            event.getMessage().addReaction("\uD83D\uDCE8 ").queue();
                            event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Please check your private messages for further information").queue();
                        } catch (NullPointerException e) {

                        }
                    });

                    embedBuilder.clear();
                });
            } catch (NullPointerException e) {
                System.out.println("Failed to initiate private channel with USER:" + event.getAuthor().getId());
            }
        }

        /*
        ADD MOD ROLE
         */
        if (SherlockBot.commandMap.get(25).checkCommand(event.getMessage())) {
            if (args.length >= 3) {

                try {
                    if (event.getGuild().getRoleById(args[1]) == null) {
                        return;
                    }

                    SherlockBot.guildMap.get(event.getGuild().getId()).addModRole(args[1], Integer.parseInt(args[2]));
                    if (database.insertModRole(event.getGuild().getIdLong(), Long.valueOf(args[1]), Integer.parseInt(args[2])) >= 1) {
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        event.getMessage().addReaction("❌").queue();
                    }

                } catch (NumberFormatException e) {
                    return;
                }
            }
        }

        /*
        REMOVE MOD ROLE
         */
        if (SherlockBot.commandMap.get(26).checkCommand(event.getMessage())) {
            if (args.length >= 2) {

                try {
                    if (event.getGuild().getRoleById(args[1]) == null) {
                        return;
                    }

                    SherlockBot.guildMap.get(event.getGuild().getId()).removeModRole(args[1]);
                    if (database.removeModRole(event.getGuild().getIdLong(), Long.valueOf(args[1])) >= 1) {
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        event.getMessage().addReaction("❌").queue();
                    }

                } catch (NumberFormatException e) {
                    return;
                }
            }

        }

        /*
        UPDATE MOD PERMISSIONS
         */
        if (SherlockBot.commandMap.get(27).checkCommand(event.getMessage())) {
            if (args.length >= 3) {

                try {
                    if (event.getGuild().getRoleById(args[1]) == null) {
                        return;
                    }

                    int newValue = Integer.parseInt(args[2]);
                    Long identifier = Long.valueOf(args[1]);

                    if (SherlockBot.guildMap.get(event.getGuild().getId()).putModPermissionLevel(args[1], newValue)) {
                        if (database.putValue("ModRoleTable", "Permissions", "ModRoleID", identifier, newValue) >= 1) {
                            event.getMessage().addReaction("✅").queue();
                        } else {
                            // Unsuccessful (0 rows updated)
                            event.getMessage().addReaction("❌").queue();
                        }
                    } else {
                        // Unsuccessful (0 rows updated)
                        event.getMessage().addReaction("❌").queue();
                    }

                } catch (NumberFormatException e) {
                    return;
                }
            }
        }

        /*
        GET MOD ROLES
         */
        if (SherlockBot.commandMap.get(28).checkCommand(event.getMessage())) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

            Iterator it = SherlockBot.guildMap.get(event.getGuild().getId()).modRoleMap.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry entry = (Map.Entry) it.next();


                try {
                    String roleName = event.getGuild().getRoleById((String) entry.getKey()).getName();
                    String permissionBinary = Integer.toBinaryString((Integer) entry.getValue());
                    String reverseBinString = new StringBuilder(permissionBinary).reverse().toString();
                    StringBuilder rolePermissions = new StringBuilder();

                    for (int i = 0; i < reverseBinString.length(); i++) {
                        if (reverseBinString.charAt(i) == '1') {
                            switch (i) {
                                case 0:
                                    rolePermissions.append("Mute/Unmute").append("\n");
                                    break;
                                case 1:
                                    rolePermissions.append("Infractions").append("\n");
                                    break;
                                case 2:
                                    rolePermissions.append("Channel Cooldown").append("\n");
                                    break;
                                case 3:
                                    rolePermissions.append("Archive Channels").append("\n");
                                    break;
                                case 4:
                                    rolePermissions.append("Clear").append("\n");
                                    break;
                                case 5:
                                    rolePermissions.append("Auto Role Assignment").append("\n");
                                    break;
                                case 6:
                                    rolePermissions.append("Assignable Roles").append("\n");
                                    break;
                                case 7:
                                    rolePermissions.append("Mute/Unmute").append("\n");
                                    break;
                                case 8:
                                    rolePermissions.append("Mute/Unmute").append("\n");
                                    break;
                                case 9:
                                    rolePermissions.append("Mute/Unmute").append("\n");
                                    break;
                            }
                        }
                    }

                    embedBuilder.addField("Role: " + roleName, String.format("**ID:** %s\n**Permission Value:** %s", entry.getKey().toString(), entry.getValue().toString()
                    ), true);
                    embedBuilder.addField("Permissions", rolePermissions.toString(), true);
                    if (it.hasNext()) {
                        embedBuilder.addField("", "", false);
                    }

                } catch (NullPointerException e) {
                    System.out.println("null found");
                    continue;
                }
            }

            embedBuilder.setTitle("Mod Role Table")
                    .setFooter("Called by: " + event.getMember().getEffectiveName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setColor(Color.CYAN);

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            embedBuilder.clear();
        }

        /*
        ADD EXCEPTION
         */

        if (SherlockBot.commandMap.get(29).checkCommand(event.getMessage())) {
            if (args.length >= 3) {

                try {
                    if (event.getGuild().getTextChannelById(args[1]) != null) {

                        SherlockBot.guildMap.get(event.getGuild().getId()).addChannelException(args[1], Integer.parseInt(args[2]));
                        if (database.insertException(event.getGuild().getIdLong(), Long.valueOf(args[1]), Integer.parseInt(args[2])) >= 1) {
                            event.getMessage().addReaction("✅").queue();
                        } else {
                            event.getMessage().addReaction("❌").queue();
                        }
                    }

                } catch (NumberFormatException e) {
                    return;
                }
            }
        }

        /*
        REMOVE EXCEPTION
         */
        if (SherlockBot.commandMap.get(30).checkCommand(event.getMessage())) {
            if (args.length >= 2) {

                try {
                    SherlockBot.guildMap.get(event.getGuild().getId()).removeChannelException(args[1]);
                    if (database.removeException(event.getGuild().getIdLong(), Long.valueOf(args[1])) >= 1) {
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        event.getMessage().addReaction("❌").queue();
                    }

                } catch (NumberFormatException e) {
                    return;
                }
            }
        }

        /*
        GET EXCEPTIONS
         */
        if (SherlockBot.commandMap.get(31).checkCommand(event.getMessage())) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            StringBuilder channelNameString = new StringBuilder();
            StringBuilder channelIDString = new StringBuilder();
            StringBuilder exceptionValueString = new StringBuilder();

            for (Map.Entry<String, Integer> entry : SherlockBot.guildMap.get(event.getGuild().getId()).exceptionMap.entrySet()) {
                try {
                    String channelName = event.getGuild().getTextChannelById(entry.getKey()).getName();
                    channelNameString.append(channelName).append("\n");

                    channelIDString.append(entry.getKey()).append("\n");
                    exceptionValueString.append(entry.getValue()).append("\n");
                } catch (NullPointerException e) {
                    continue;
                }
            }

            embedBuilder.setTitle("Channel Exception List")
                    .addField("Channel Name:", channelNameString.toString(), true)
                    .addField("Channel ID:", channelIDString.toString(), true)
                    .addField("Exception Value", exceptionValueString.toString(), true)
                    .setFooter("Called by: " + event.getMember().getEffectiveName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setColor(Color.CYAN);

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            embedBuilder.clear();
        }

        /*
        SET ARCHIVE CATEGORY
         */
        if (SherlockBot.commandMap.get(32).checkCommand(event.getMessage())) {
            if (args.length >= 2) {

                try {
                    if (event.getGuild().getCategoryById(args[1]) != null) {
                        Long catChannelID = event.getGuild().getCategoryById(args[1]).getIdLong();

                        SherlockBot.guildMap.get(event.getGuild().getId()).setArchiveCategory(catChannelID);
                        if (database.setArchiveCategory(event.getGuild().getIdLong(), catChannelID) > 0) {
                            event.getMessage().addReaction("✅").queue();
                        }
                    } else {
                        //User did not mention a channel
                        if (event.getGuild().getCategoriesByName(args[1], true) != null) {
                            Long catChannelID = event.getGuild().getCategoriesByName(args[1], true).get(0).getIdLong();

                            SherlockBot.guildMap.get(event.getGuild().getId()).setArchiveCategory(catChannelID);
                            if (database.setArchiveCategory(event.getGuild().getIdLong(), catChannelID) > 0) {
                                event.getMessage().addReaction("✅").queue();
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.println("Could not find category when searching for term: " + args[1]);
                } catch (NumberFormatException e) {
                    if (event.getGuild().getCategoriesByName(args[1], true) != null) {
                        Long catChannelID = event.getGuild().getCategoriesByName(args[1], true).get(0).getIdLong();

                        SherlockBot.guildMap.get(event.getGuild().getId()).setArchiveCategory(catChannelID);
                        if (database.setArchiveCategory(event.getGuild().getIdLong(), catChannelID) > 0) {
                            event.getMessage().addReaction("✅").queue();
                        }
                    }
                }
            } else {
                event.getMessage().reply(" You did not provide a categoryID or give a category name.").queue();
            }
        }


        /*
        GET ARCHIVE CATEGORY
         */
        if (SherlockBot.commandMap.get(33).checkCommand(event.getMessage())) {
            if (SherlockBot.guildMap.get(event.getGuild().getId()).getArchiveCategoryID() != null) {

                try {
                    Long archiveCategoryID = SherlockBot.guildMap.get(event.getGuild().getId()).getArchiveCategoryID();
                    event.getMessage().reply(String.format("Current Archive Category: %s\nCategory ID: %d", event.getGuild().getCategoryById(archiveCategoryID).getName(), archiveCategoryID)).queue();
                } catch (NullPointerException e) {

                }
            } else {
                event.getMessage().reply("You do not have a archive category set.").queue();
            }
        }


        /*
        ADD AUTO ROLE
         */
        if (SherlockBot.commandMap.get(37).checkCommand(event.getMessage())) {
            if (args.length >= 2) {

                try {
                    if (event.getGuild().getRoleById(args[1]) != null) {

                        if (database.insertAutoRole(event.getGuild().getIdLong(), Long.valueOf(args[1])) == 200) {
                            event.getMessage().addReaction("✅").queue();
                        } else {
                            event.getMessage().addReaction("❌").queue();
                        }

                        return;
                    }

                } catch (NumberFormatException e) {
                }

                // Role was not found using ID
                try {
                    for (Role r : event.getGuild().getRoles()) {
                        if (r.getName().equalsIgnoreCase(args[1])) {
                            if (database.insertAutoRole(event.getGuild().getIdLong(), r.getIdLong()) >= 1) {
                                event.getMessage().addReaction("✅").queue();
                            } else {
                                event.getMessage().addReaction("❌").queue();
                            }
                        }
                    }
                } catch (NullPointerException | PermissionException e) {

                }
            }
        }

        /*
        REMOVE AUTO ROLE
         */
        if (SherlockBot.commandMap.get(38).checkCommand(event.getMessage())) {
            if (args.length >= 2) {

                try {
                    if (event.getGuild().getRoleById(args[1]) != null) {

                        if (database.removeAutoRole(event.getGuild().getIdLong(), Long.valueOf(args[1])) >= 1) {
                            event.getMessage().addReaction("✅").queue();
                        } else {
                            event.getMessage().addReaction("❌").queue();
                        }

                        return;
                    }

                } catch (NumberFormatException e) {
                }

                // Role was not found using ID
                try {
                    for (Role r : event.getGuild().getRoles()) {
                        if (r.getName().equalsIgnoreCase(args[1])) {
                            if (database.removeAutoRole(event.getGuild().getIdLong(), r.getIdLong()) >= 1) {
                                event.getMessage().addReaction("✅").queue();
                            } else {
                                event.getMessage().addReaction("❌").queue();
                            }
                        }
                    }
                } catch (NullPointerException | PermissionException e) {

                }
            }
        }

        /*
        GET AUTO ROLES
         */
        if (SherlockBot.commandMap.get(39).checkCommand(event.getMessage())) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            StringBuilder roleName = new StringBuilder();
            StringBuilder roleID = new StringBuilder();

            ArrayList<Long> roles = SherlockBot.database.getAutoRoles(event.getGuild().getIdLong());

            for (Long id : roles) {
                try {
                    roleName.append(event.getGuild().getRoleById(id).getName()).append("\n");
                    roleID.append(id).append("\n");
                } catch (NullPointerException e) {
                    continue;
                }
            }

            embedBuilder.setTitle("Auto Role List")
                    .addField("Role Name:", roleName.toString(), true)
                    .addField("Role ID:", roleID.toString(), true)
                    .setFooter("Called by: " + event.getMember().getEffectiveName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setColor(Color.CYAN);

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            embedBuilder.clear();
        }


        /*
        ADD REACTION ROLE
         */
        if (SherlockBot.commandMap.get(40).checkCommand(event.getMessage())) {
            String message = event.getMessage().getContentDisplay();

            //Replace all mentioned channels of message String
            for (TextChannel channel : event.getMessage().getMentionedChannels()) {
                message = message.replace(channel.getName(), "");
            }

            //Create local argument list from Message
            String[] localArgs = message.split("\\s+");

            //Set the channel to look for the destination message
            TextChannel messageChannel = event.getChannel();
            if (event.getMessage().getMentionedChannels().size() > 0) {
                messageChannel = event.getMessage().getMentionedChannels().get(0);
            }


            //Try to find the destination message
            messageChannel.retrieveMessageById(localArgs[1]).queue(messageID -> {

                //Try to grab role from second argument provided
                final Role role = event.getGuild().getRoleById(localArgs[2]);

                //Role was found
                if (role != null) {

                    //Destination message was found, See if Requesting message had emotes in message
                    if (!event.getMessage().getEmotes().isEmpty()) {
                    // EMOTE WAS FOUND

                        //Set the emote to the first emote found.  (only allow one insertion at a time)
                        final Emote emote = event.getMessage().getEmotes().get(0);

                        //Try to add the reaction to the message
                        messageID.addReaction(emote).queue(successAddition -> {
                            SherlockBot.guildMap.get(event.getGuild().getId()).insertReactionRole(messageID.getIdLong(),emote.getId(),role.getIdLong());
                            if(SherlockBot.database.insertReactionRole(event.getGuild().getIdLong(),messageID.getIdLong(),emote.getId(),role.getIdLong()) >= 1){
                                //Success
                                event.getMessage().addReaction("✅").queue();
                            }
                        });
                    } else {

                        // IS REACTION AN EMOJI???
                        List<String> emojiList = EmojiParser.extractEmojis(event.getMessage().getContentRaw());
                        if (!emojiList.isEmpty()) {
                            messageID.addReaction(emojiList.get(0)).queue(success -> {
                                SherlockBot.guildMap.get(event.getGuild().getId()).insertReactionRole(messageID.getIdLong(),EmojiParser.parseToAliases(emojiList.get(0)),role.getIdLong());
                                if(SherlockBot.database.insertReactionRole(event.getGuild().getIdLong(),messageID.getIdLong(),EmojiParser.parseToAliases(emojiList.get(0)),role.getIdLong()) >= 1){
                                    //Success
                                    event.getMessage().addReaction("✅").queue();
                                }

                            });
                        }
                    }

                }
            });


        }

        /*
        REMOVE REACTION ROLE
         */
        if (SherlockBot.commandMap.get(41).checkCommand(event.getMessage())) {

            if (args.length >= 1) {
                Long messageID = Long.valueOf(args[1]);

                if (SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().containsKey(messageID)) {

                    ArrayList<RoleReactionObject> roleList = SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().get(messageID);

                    Iterator it = roleList.iterator();
                    while (it.hasNext()) {
                        RoleReactionObject object = (RoleReactionObject) it.next();
                        if (args[1].equalsIgnoreCase(object.reactionID)) {
                            it.remove();

                            SherlockBot.database.removeReactionRole(event.getGuild().getIdLong(),messageID,object.reactionID);
                        }
                    }

                    if (!roleList.isEmpty()) {
                        SherlockBot.guildMap.get(event.getGuild().getId()).getReactionMap().put(messageID, roleList);
                    }
                }
            }
        }

        /*
        CHECK AUTHORIZED
         */
        if (SherlockBot.commandMap.get(19).checkCommand(event.getMessage())) {
            Boolean authorized = false;
            int index = Integer.parseInt(args[1]);

            int modRoleValue = 0;
            String binaryString = "";
            char indexChar = '0';

            if (args.length < 3) {
                for (Role r : event.getMember().getRoles()) {
                    if (SherlockBot.guildMap.get(event.getGuild().getId()).modRoleMap.containsKey(r.getId())) {

                        modRoleValue = SherlockBot.guildMap.get(event.getGuild().getId()).modRoleMap.get(r.getId());
                        binaryString = Integer.toBinaryString(modRoleValue);
                        String reverseString = new StringBuilder(binaryString).reverse().toString();
                        try {
                            indexChar = reverseString.charAt(index);
                        } catch (IndexOutOfBoundsException e) {
                        }

                    }
                }
            } else {
                if (SherlockBot.guildMap.get(event.getGuild().getId()).modRoleMap.containsKey(args[2])) {
                    modRoleValue = SherlockBot.guildMap.get(event.getGuild().getId()).modRoleMap.get(args[2]);
                    binaryString = Integer.toBinaryString(modRoleValue);
                    String reverseString = new StringBuilder(binaryString).reverse().toString();
                    try {
                        indexChar = reverseString.charAt(index);
                    } catch (IndexOutOfBoundsException e) {
                    }
                } else {
                    indexChar = '0';
                }
            }

            if (indexChar == '1') {
                event.getMessage().addReaction("\uD83D\uDC4D").queue();
            } else {
                event.getMessage().addReaction("\uD83D\uDC4E").queue();
                System.out.println(String.format("Role Integer: %d\nBinary String:%s\nChar at Index:%s", modRoleValue, binaryString, indexChar));
            }
        }

    }


    /*

                    PRIVATE MESSAGE RECEIVED LISTENER

     */
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (userMap.containsKey(event.getAuthor().getId())) {
            int function = userMap.get(event.getAuthor().getId());
            if (function == 1) {
                if (event.getMessage().getContentDisplay().equalsIgnoreCase("done")) {
                    userMap.remove(event.getAuthor().getId());
                    channelGuildMap.remove(event.getAuthor().getId());
                    event.getChannel().sendMessage("Transmission ended").queue();
                } else {
                    if (!SherlockBot.guildMap.get(channelGuildMap.get(event.getAuthor().getId())).getBlacklistedWords().contains(event.getMessage().getContentDisplay())) {
                        SherlockBot.guildMap.get(channelGuildMap.get(event.getAuthor().getId())).addBadWord(event.getMessage().getContentDisplay());
                        if (database.insertBadWord(Long.valueOf(channelGuildMap.get(event.getAuthor().getId())), event.getMessage().getContentDisplay()) >= 1) {
                            event.getMessage().addReaction("✅").queue();
                        }
                    }
                }
            }

            if (function == 2) {
                if (event.getMessage().getContentDisplay().equalsIgnoreCase("done")) {
                    userMap.remove(event.getAuthor().getId());
                    channelGuildMap.remove(event.getAuthor().getId());
                    event.getChannel().sendMessage("Transmission ended").queue();
                } else {
                    if (SherlockBot.guildMap.get(channelGuildMap.get(event.getAuthor().getId())).getBlacklistedWords().contains(event.getMessage().getContentDisplay())) {
                        SherlockBot.guildMap.get(channelGuildMap.get(event.getAuthor().getId())).removeBadWord(event.getMessage().getContentDisplay());
                        if (database.removeBadWord(Long.valueOf(channelGuildMap.get(event.getAuthor().getId())), event.getMessage().getContentDisplay()) >= 1) {
                            event.getMessage().addReaction("✅").queue();
                        }
                    }
                }
            }
        }
    }

    private void handleAutoRole(GuildMessageReceivedEvent event, Boolean removal) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        try {
            if (event.getGuild().getRoleById(args[1]) != null) {

                if (database.insertAutoRole(event.getGuild().getIdLong(), Long.valueOf(args[1])) >= 1) {
                    event.getMessage().addReaction("✅").queue();
                } else {
                    event.getMessage().addReaction("❌").queue();
                }

                return;
            }

        } catch (NumberFormatException e) {
        }

        // Role was not found using ID
        try {
            for (Role r : event.getGuild().getRoles()) {
                if (r.getName().equalsIgnoreCase(args[1])) {
                    if (database.insertAutoRole(event.getGuild().getIdLong(), r.getIdLong()) >= 1) {
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        event.getMessage().addReaction("❌").queue();
                    }
                }
            }
        } catch (NullPointerException | PermissionException e) {

        }
    }
}
