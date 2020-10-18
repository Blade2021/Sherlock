package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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
        if (SherlockBot.commands.get(8).checkCommandMod(event.getMessage())) {
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
        if (SherlockBot.commands.get(6).checkCommandMod(event.getMessage())) {
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
                                return;
                            }
                        } catch (NumberFormatException e) {
                            event.getMessage().addReaction("⚠").queue();
                            return;
                        }
                    }

                    database.putValue("GuildTable", "LogChannelID", "GuildID", event.getGuild().getIdLong(), Long.valueOf(newID));
                    SherlockBot.guildMap.get(event.getGuild().getId()).setLogChannelID(newID);
                    event.getMessage().addReaction("✅").queue();
                } else {
                    String logChannelID = SherlockBot.guildMap.get(event.getGuild().getId()).getLogChannelID();
                    System.out.println(logChannelID);
                    if ((logChannelID != null) && !(logChannelID.equalsIgnoreCase("0")) && (event.getGuild().getTextChannelById(logChannelID).canTalk())) {
                        event.getChannel().sendMessage(event.getGuild().getTextChannelById(logChannelID).getAsMention() + "\nLogChannelID: " + logChannelID).queue();
                    } else {
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " No log channel has been set  \uD83E\uDDD0").queue();
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

        //CHANGE Embed filter level
        if (SherlockBot.commands.get(14).checkCommandMod(event.getMessage())) {
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
        if (SherlockBot.commands.get(5).checkCommandMod(event.getMessage())) {
            try {
                if ((args.length == 3) && (event.getGuild().getRoleById(args[2]) != null)) {
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
                                event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ERROR 201: You have hit the maximum number of self roles.  Please try to remove some before continuing.").queue();
                                break;
                            case 400:
                                event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ERROR 400: Database had an unknown error.  This event has been logged.").queue();
                        }
                        // Unsuccessful (0 rows updated)
                        database.logError(event.getGuild().getId(), "Failed to add self role", databaseStatusCode);
                    }
                } else {
                    // Request failed internal checks
                    if (args.length < 3) {
                        event.getChannel().sendMessage("Not enough arguments supplied.").queue();
                    } else if (event.getGuild().getRoleById(args[2]) == null) {
                        // Cannot find role associated with ID provided
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " I could not find a role associated with that ID.").queue();
                    }
                    event.getMessage().addReaction("⚠").queue();
                }
            } catch (NullPointerException e) {
                event.getMessage().addReaction("⚠").queue();
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("What in tarnation is that>?").queue();
                event.getMessage().addReaction("⚠").queue();
            }
        }

        /*
        REMOVE ROLE FROM SELF ROLES OF GUILDMAP
         */
        if (SherlockBot.commands.get(4).checkCommandMod(event.getMessage())) {
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
        if (SherlockBot.commands.get(10).checkCommandMod(event.getMessage())) {

            try {
                event.getAuthor().openPrivateChannel().queue((channel) -> {
                    try {
                        event.getMessage().addReaction("\uD83D\uDCE8 ").queue();
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Please check your private messages for further information").queue();
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
        if (SherlockBot.commands.get(11).checkCommandMod(event.getMessage())) {
            try {
                event.getAuthor().openPrivateChannel().queue((channel) -> {
                    try {
                        event.getMessage().addReaction("\uD83D\uDCE8 ").queue();
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Please check your private messages for further information").queue();
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
        if (SherlockBot.commands.get(12).checkCommandMod(event.getMessage())) {
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
        if (SherlockBot.commands.get(25).checkCommandMod(event.getMessage())) {
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
        if (SherlockBot.commands.get(26).checkCommandMod(event.getMessage())) {
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
        if (SherlockBot.commands.get(27).checkCommandMod(event.getMessage())) {
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
        if (SherlockBot.commands.get(28).checkCommandMod(event.getMessage())) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            StringBuilder roleNameString = new StringBuilder();
            StringBuilder roleIDString = new StringBuilder();
            StringBuilder rolePermissions = new StringBuilder();

            for (Map.Entry<String, Integer> entry : SherlockBot.guildMap.get(event.getGuild().getId()).modRoleMap.entrySet()) {
                try {
                    String roleName = event.getGuild().getRoleById(entry.getKey()).getName();
                    roleNameString.append(roleName).append("\n");

                    roleIDString.append(entry.getKey()).append("\n");
                    rolePermissions.append(entry.getValue()).append("\n");
                } catch (NullPointerException e) {
                    continue;
                }
            }

            embedBuilder.setTitle("Mod Role Table")
                    .addField("Role Name:", roleNameString.toString(), true)
                    .addField("Role ID:", roleIDString.toString(), true)
                    .addField("Role Permissions", rolePermissions.toString(), true)
                    .setFooter("Called by: " + event.getMember().getEffectiveName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setColor(Color.CYAN);

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            embedBuilder.clear();
        }

        /*
        ADD EXCEPTION
         */

        if (SherlockBot.commands.get(29).checkCommandMod(event.getMessage())) {
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
        REMOVE MOD ROLE
         */
        if (SherlockBot.commands.get(30).checkCommandMod(event.getMessage())) {
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
        GET MOD ROLES
         */
        if (SherlockBot.commands.get(31).checkCommandMod(event.getMessage())) {

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
        CHECK AUTHORIZED
         */
        if (SherlockBot.commands.get(19).checkCommand(event.getMessage().getContentDisplay(), event.getGuild().getId())) {
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
}
