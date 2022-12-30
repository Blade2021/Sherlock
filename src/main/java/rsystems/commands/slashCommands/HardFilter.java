package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.EmbedBuilder;
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
import java.util.Map;

public class HardFilter extends SlashCommand {

    @Override
    public CommandData getCommandData() {

        // Filter Group
        ArrayList<SubcommandData> filteringCommands = new ArrayList<>();
        filteringCommands.add(new SubcommandData("add", "Enable hard filtering for a word").addOption(OptionType.STRING, "word", "Word to be filtered", true).addOption(OptionType.STRING,"action","What action to take?",true));
        filteringCommands.add(new SubcommandData("remove", "Remove filtering for a word").addOption(OptionType.STRING, "word", "Word to be cleared from the filter", true));
        filteringCommands.add(new SubcommandData("list", "List all hard filtered words"));

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

            Integer action = null;
            if (event.getOption("action") != null) {
                action = Integer.parseInt(event.getOption("action").getAsString());
            }

            try {
                if (SherlockBot.database.addHardFilterWord(event.getGuild().getIdLong(), word,action) > 0) {
                    event.getHook().editOriginal(String.format("I have added `%s` to the filter.", word)).queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("remove")) {
            try {
                if (SherlockBot.database.removeHardFilterWord(event.getGuild().getIdLong(), word) > 0) {
                    event.getHook().editOriginal(String.format("I have removed `%s` from the filter.", word)).queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("list")) {
            try {

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Hard Filtered Words");
                embedBuilder.setDescription("**All hard filtered words are deleted immediately.**\n" +
                        "```Action types:\n" +
                        "0 = Add a low warning to the user\n" +
                        "1 = Quarantine the user\n" +
                        "2 = Kick the user from the server\n" +
                        "3 = Ban the user from the server```");

                Map<String,Integer> hardFilterList = SherlockBot.database.getHardFilteredWords(event.getGuild().getIdLong());

                if (hardFilterList.size() > 0) {

                    StringBuilder wordString = new StringBuilder();
                    StringBuilder actionString = new StringBuilder();

                    for(Map.Entry<String,Integer> entry: hardFilterList.entrySet()){
                        wordString.append(entry.getKey()).append("\n");
                        actionString.append(entry.getValue()).append("\n");
                    }

                    embedBuilder.addField("Word:",wordString.toString(),true);
                    embedBuilder.addField("Action Type:",actionString.toString(),true);
                    embedBuilder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));

                    event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();

                } else {
                    embedBuilder.appendDescription("\n\n" + "There are no words that are currently being filtered.");
                    event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                }

                embedBuilder.clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getDescription() {
        return "Word filter, Trigger an automatic message delete & action when a word is used";
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
