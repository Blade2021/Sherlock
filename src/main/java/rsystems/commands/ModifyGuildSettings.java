package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
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
                    if(event.getMessage().getMentionedChannels().size() > 0){
                            if(event.getMessage().getMentionedChannels().get(0) != null){
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
                        }catch(NumberFormatException e){
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
                    if((logChannelID != null) && !(logChannelID.equalsIgnoreCase("0")) && (event.getGuild().getTextChannelById(logChannelID).canTalk())){
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
                    event.getChannel().sendMessage(String.format("%s The current embed filter setting: %d\nPlease use `%sEmbedFilter [int]` to set a different value.",
                            event.getAuthor().getAsMention(),
                            SherlockBot.guildMap.get(event.getGuild().getId()).getEmbedFilter(),
                            SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix()))
                            .queue();
                } else {
                    try {
                        SherlockBot.guildMap.get(event.getGuild().getId()).setEmbedFilter(Integer.parseInt(args[1]));
                        database.putValue("GuildTable","EmbedFilter","GuildID",event.getGuild().getIdLong(),Integer.parseInt(args[1]));
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
                        ASSIGNABLE ROLES CONFIGURATION
         */

        /*
        ADD ROLE TO ASSIGNABLE ROLES OF GUILDMAP
         */
        if (SherlockBot.commands.get(5).checkCommandMod(event.getMessage())) {
            try {
                if ((args.length == 3) && (event.getGuild().getRoleById(args[2]) != null)) {
                    // Add role to assignable roles of guildmap
                    SherlockBot.guildMap.get(event.getGuild().getId()).addAssignableRole(args[1], Long.valueOf(args[2]));
                    int databaseStatusCode = database.insertAssignableRole(event.getGuild().getIdLong(), args[1], Long.valueOf(args[2]));

                    if (databaseStatusCode == 200) {
                        // Success (1+ updated)
                        event.getMessage().addReaction("✅").queue();
                        return;
                    } else {
                        switch(databaseStatusCode){
                            case 201:
                                event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ERROR 201: You have hit the maximum number of assignable roles.  Please try to remove some before continuing.").queue();
                                break;
                            case 400:
                                event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ERROR 400: Database had an unknown error.  This event has been logged.").queue();
                        }
                        // Unsuccessful (0 rows updated)
                        database.logError(event.getGuild().getId(), "Failed to add assignable role",databaseStatusCode);
                    }
                } else {
                    // Request failed internal checks
                    if (args.length < 3) {
                        event.getChannel().sendMessage("Not enough arguments supplied.").queue();
                    } else if(event.getGuild().getRoleById(args[2]) == null){
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
        REMOVE ROLE FROM ASSIGNABLE ROLES OF GUILDMAP
         */
        if (SherlockBot.commands.get(4).checkCommandMod(event.getMessage())) {
            if (args.length >= 1) {
                if (SherlockBot.guildMap.get(event.getGuild().getId()).assignableRoleMap.containsKey(args[1])) {
                    SherlockBot.guildMap.get(event.getGuild().getId()).removeAssignableRole(args[1]);

                    int rowUpdateCount = database.removeAssignableRole(event.getGuild().getIdLong(), args[1]);

                    // Success (1+ updated)
                    if (rowUpdateCount > 0) {
                        System.out.println(String.format("Guild: %s | Role CMD: %s | Count: %d", event.getGuild().getId(), args[1], rowUpdateCount));
                        event.getMessage().addReaction("✅").queue();
                    } else {
                        // Unsuccessful (0 rows updated)
                        event.getMessage().addReaction("❌").queue();
                        database.logError(event.getGuild().getId(), "Failed to remove assignable role", 400);
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
