package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;
import java.util.ArrayList;

public class AutoPush extends SlashCommand {

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public CommandData getCommandData() {

        // Filter Group
        ArrayList<SubcommandData> subCommands = new ArrayList<>();
        subCommands.add(new SubcommandData("add", "Add a channel to the automatic push category").addOption(OptionType.CHANNEL, "channel", "The channel to be added", true));
        subCommands.add(new SubcommandData("remove", "Remove a channel from the automatic push category").addOption(OptionType.CHANNEL, "channel", "The channel to be removed", true));
        subCommands.add(new SubcommandData("list", "List all Channels that have auto push enabled"));

        return Commands.slash(this.getName().toLowerCase(), this.getDescription()).addSubcommands(subCommands);
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {

        if (event.getSubcommandName().equalsIgnoreCase("add")) {

            event.deferReply().setEphemeral(this.isEphemeral()).queue();
            final NewsChannel targetChannel = event.getOption("channel").getAsNewsChannel();

            if(targetChannel != null){

                // Add channel to database
                try {
                    SherlockBot.database.insertAutoPushChannel(event.getGuild().getIdLong(),targetChannel.getIdLong());

                    reply(event,"Success",this.isEphemeral());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                reply(event,"Failed to add channel.\nWas the channel provided an announcement channel?",this.isEphemeral());
            }
        }

        else if(event.getSubcommandName().equalsIgnoreCase("remove")){

            // Remove channel from database
            event.deferReply().setEphemeral(this.isEphemeral()).queue();
            final NewsChannel targetChannel = event.getOption("channel").getAsNewsChannel();

            if(targetChannel != null){

                try {
                    SherlockBot.database.removeAutoPushChannel(event.getGuild().getIdLong(),targetChannel.getIdLong());

                    reply(event,"Success",this.isEphemeral());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                reply(event,"Failed to remove channel.\nWas the channel provided an announcement channel?",this.isEphemeral());
            }

        }

        else if(event.getSubcommandName().equalsIgnoreCase("list")){
            event.deferReply().queue();

            reply(event,"This function is not ready yet",this.isEphemeral());
        }
    }

    @Override
    public String getDescription() {
        return "Automatically push messages that are posted in a Announcement channel";
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }
}
