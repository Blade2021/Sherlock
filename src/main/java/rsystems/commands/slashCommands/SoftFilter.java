package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;
import java.util.ArrayList;

public class SoftFilter extends SlashCommand {

    @Override
    public CommandData getCommandData() {

        // Filter Group
        ArrayList<SubcommandData> filteringCommands = new ArrayList<>();
        filteringCommands.add(new SubcommandData("add", "Enable filtering for a word").addOption(OptionType.STRING, "word", "Word to be filtered", true));
        filteringCommands.add(new SubcommandData("remove", "Remove filtering for a word").addOption(OptionType.STRING, "word", "Word to be cleared from the filter", true));
        filteringCommands.add(new SubcommandData("list", "List all filtered words"));

        return Commands.slash(this.getName().toLowerCase(), this.getDescription()).addSubcommands(filteringCommands);
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {


        event.deferReply().setEphemeral(this.isEphemeral()).queue();

        String word = null;
        if (event.getOption("word") != null) {
            word = event.getOption("word").getAsString();
        }

        if (event.getSubcommandName().equalsIgnoreCase("add")) {
            try {
                if (SherlockBot.database.addSoftFilterWord(event.getGuild().getIdLong(), word) > 0) {
                    event.getHook().editOriginal(String.format("I have added `%s` to the filter.", word)).queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("remove")) {
            try {
                if (SherlockBot.database.removeSoftFilterWord(event.getGuild().getIdLong(), word) > 0) {
                    event.getHook().editOriginal(String.format("I have removed `%s` from the filter.", word)).queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("list")) {
            try {
                ArrayList<String> filterWordList = SherlockBot.database.getSoftFilteredWords(event.getGuild().getIdLong());

                if (filterWordList.size() > 0) {
                    event.getHook().editOriginal(filterWordList.toString()).queue();
                } else {
                    event.getHook().editOriginal("There are no words that are currently being filtered.").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getDescription() {
        return "Word filter, Trigger an automatic message delete when a word is used";
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public Integer getPermissionIndex() {
        return 4096;
    }

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }
}
