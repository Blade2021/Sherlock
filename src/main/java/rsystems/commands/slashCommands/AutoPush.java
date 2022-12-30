package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
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
            final NewsChannel targetChannel = event.getOption("channel").getAsChannel().asNewsChannel();

            if (targetChannel != null) {

                // Add channel to database
                try {
                    if (SherlockBot.database.insertAutoPushChannel(event.getGuild().getIdLong(), targetChannel.getIdLong()) > 0) {
                        reply(event, "Success", this.isEphemeral());
                    }
                }
                catch (SQLIntegrityConstraintViolationException ex){
                    reply(event, "Failed to add channel.\n**Channel is already in the system**", this.isEphemeral());
                }
                catch (SQLException e) {
                    reply(event, "An Error Occurred", this.isEphemeral());
                }

            } else {

                reply(event, "Failed to add channel.\nWas the channel provided an announcement channel?", this.isEphemeral());

            }
        } else if (event.getSubcommandName().equalsIgnoreCase("remove")) {

            // Remove channel from database
            event.deferReply().setEphemeral(this.isEphemeral()).queue();
            final NewsChannel targetChannel = event.getOption("channel").getAsChannel().asNewsChannel();

            if (targetChannel != null) {

                try {
                    if (SherlockBot.database.removeAutoPushChannel(event.getGuild().getIdLong(), targetChannel.getIdLong()) > 0) {
                        reply(event, "Success", this.isEphemeral());
                    } else {
                        reply(event, "Failed to remove channel", this.isEphemeral());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                reply(event, "Failed to remove channel.\nWas the channel provided an announcement channel?", this.isEphemeral());
            }

        } else if (event.getSubcommandName().equalsIgnoreCase("list")) {
            event.deferReply(this.isEphemeral()).queue();

            try {
                ArrayList<Long> channelList = SherlockBot.database.getListLong(event.getGuild().getIdLong(),"AutoPush","ChannelID");
                if(channelList != null && !channelList.isEmpty()){

                    EmbedBuilder builder = new EmbedBuilder();
                    StringBuilder sb = new StringBuilder();

                    for(Long entry:channelList){

                        final NewsChannel entryChannel = event.getGuild().getNewsChannelById(entry);

                        if(entryChannel != null){
                            sb.append(entryChannel.getAsMention()).append(", ");
                        }
                    }

                    builder.setTitle("Auto Pushed Channels:");
                    builder.setDescription(sb.toString());
                    builder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));

                    reply(event,builder.build());

                } else {
                    reply(event,"No channels found",this.isEphemeral());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
