package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import rsystems.SherlockBot;
import rsystems.handlers.LogMessage;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;
import java.util.ArrayList;

public class GuildSetting extends SlashCommand {

    @Override
    public CommandData getCommandData() {

        ArrayList<SubcommandGroupData> subCmdGroupData = new ArrayList<>();

        // Prefix Group
        ArrayList<SubcommandData> prefixCommands = new ArrayList<>();
        prefixCommands.add(new SubcommandData("set","Set a custom prefix for the bot on this server").addOption(OptionType.STRING,"prefix","The prefix to be set",true));
        prefixCommands.add(new SubcommandData("clear","Return to the default prefix only"));

        subCmdGroupData.add(new SubcommandGroupData("bot-prefix","Prefix Set/Clear commands").addSubcommands(prefixCommands));

        // Logging Group
        ArrayList<SubcommandData> loggingCommands = new ArrayList<>();
        loggingCommands.add(new SubcommandData("set","Set the log channel to be used by the BoT").addOption(OptionType.CHANNEL,"channel","The log channel",true));
        loggingCommands.add(new SubcommandData("clear","Clear the log channel being used by the BoT"));

        subCmdGroupData.add(new SubcommandGroupData("logging","Set/Clear Logging Channel commands").addSubcommands(loggingCommands));

        // Welcome Message Group
        ArrayList<SubcommandData> welcomeCommands = new ArrayList<>();
        welcomeCommands.add(new SubcommandData("set","Set a welcome message for joining members on your server").addOption(OptionType.STRING,"message","The message to be sent to the user",true));
        welcomeCommands.add(new SubcommandData("clear","Clear the welcome message for this server."));

        subCmdGroupData.add(new SubcommandGroupData("welcome","Welcome Message Commands").addSubcommands(welcomeCommands));

        return Commands.slash(this.getName().toLowerCase(), this.getDescription()).addSubcommandGroups(subCmdGroupData);
    }

    @Override
    public Integer getPermissionIndex() {
        return 32768;
    }

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {

        if(event.getSubcommandGroup().equalsIgnoreCase("bot-prefix")){

            event.deferReply().setEphemeral(false).queue();

            if(event.getSubcommandName().equalsIgnoreCase("set")){

                String prefix = null;
                if(event.getOption("prefix") != null){
                    prefix = event.getOption("prefix").getAsString();
                }

                SherlockBot.guildMap.get(event.getGuild().getIdLong()).setPrefix(prefix);
                try{
                    if(SherlockBot.database.updateGuild(SherlockBot.getGuildSettings(event.getGuild().getIdLong())) > 0) {
                        event.getHook().editOriginal(String.format("Your prefix `%s` has been set.", prefix)).queue();
                    } else {
                        event.getHook().editOriginal("Failed to update the database.  Contact Support").queue();
                    }
                } catch (SQLException e) {
                    event.getHook().editOriginal("Failed to update the database.  Contact Support").queue();
                }


            } else if(event.getSubcommandName().equalsIgnoreCase("clear")){
                SherlockBot.guildMap.get(event.getGuild().getIdLong()).setPrefix(null);
                try{
                    if(SherlockBot.database.updateGuild(SherlockBot.getGuildSettings(event.getGuild().getIdLong())) > 0) {
                        event.getHook().editOriginal("Your prefix has been cleared").queue();
                    } else {
                        event.getHook().editOriginal("Failed to update the database.  Contact Support").queue();
                    }
                } catch (SQLException e) {
                    event.getHook().editOriginal("Failed to update the database.  Contact Support").queue();
                }
            }

        }

        if(event.getSubcommandGroup().equalsIgnoreCase("logging")) {

            event.deferReply().setEphemeral(false).queue();

            if (event.getSubcommandName().equalsIgnoreCase("set")) {

                if (event.getOption("channel").getAsChannel() != null) {
                    Long channelID = event.getOption("channel").getAsChannel().getIdLong();

                    if (event.getGuild().getTextChannelById(channelID) != null) {
                        TextChannel logChannel = event.getGuild().getTextChannelById(channelID);
                        LogMessage.registerLogChannel(event.getGuild(), logChannel);

                        event.getHook().editOriginal(String.format("%s has been registered as the Log Channel going forward", logChannel.getAsMention())).queue();
                        return;
                    }
                } else if (event.getOption("channel").getAsChannel().asCategory() != null) {
                    event.getHook().editOriginal("Categories cannot be registered as a Log Channel").queue();
                    return;
                }

                event.getHook().editOriginal("Channel not found").queue();
            } else if (event.getSubcommandName().equalsIgnoreCase("clear")) {

                LogMessage.clearLogChannel(event.getGuild().getIdLong());

                event.getHook().editOriginal("Log Channel cleared.").queue();
            }
        }

        if(event.getSubcommandGroup().equalsIgnoreCase("welcome")) {

            event.deferReply().setEphemeral(false).queue();

            if (event.getSubcommandName().equalsIgnoreCase("set")) {
                if (!event.getOption("message").getAsString().isEmpty()) {
                    try {
                        SherlockBot.database.addWelcomeRow(event.getGuild().getIdLong(), event.getOption("message").getAsString());
                        SherlockBot.database.putValue("GuildTable", "WelcomeMessageSetting", "GuildID", event.getGuild().getIdLong(), 1);
                        event.getHook().editOriginal("Welcome message was set").queue();
                    } catch(SQLException e){
                        event.getHook().editOriginal("An error occured").queue();
                    }
                }
            } else if (event.getSubcommandName().equalsIgnoreCase("clear")) {

                try {
                    SherlockBot.database.putValueNull("WelcomeTable", "WelcomeMessage", "ChildGuildID", event.getGuild().getIdLong());
                    SherlockBot.database.putValue("GuildTable", "WelcomeMessageSetting", "GuildID", event.getGuild().getIdLong(), 0);

                    event.getHook().editOriginal("Welcome message cleared.").queue();
                } catch(SQLException e){
                    event.getHook().editOriginal("An error occured").queue();
                }
            }
        }

        try {
            SherlockBot.guildMap.put(event.getGuild().getIdLong(),SherlockBot.database.getGuildData(event.getGuild().getIdLong()));
        } catch (SQLException e) {

        }
    }

    @Override
    public String getDescription() {
        return "Guild settings";
    }
}
