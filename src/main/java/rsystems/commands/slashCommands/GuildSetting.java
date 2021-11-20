package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import rsystems.handlers.LogMessage;
import rsystems.objects.SlashCommand;

import java.util.ArrayList;

public class GuildSetting extends SlashCommand {

    @Override
    public CommandData getCommandData() {

        ArrayList<SubcommandData> subcommandData = new ArrayList<>();
        subcommandData.add(new SubcommandData("logchan","The Log channel for the BoT").addOption(OptionType.CHANNEL,"channel","The log channel",true));

        return super.getCommandData().addSubcommands(subcommandData);

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
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandEvent event) {
        if (event.getSubcommandName().equalsIgnoreCase("logchan")) {

            if (event.getOption("channel").getAsMessageChannel() != null) {
                Long channelID = event.getOption("channel").getAsMessageChannel().getIdLong();

                if (event.getGuild().getTextChannelById(channelID) != null) {
                    TextChannel logChannel = event.getGuild().getTextChannelById(channelID);
                    LogMessage.registerLogChannel(event.getGuild(),logChannel);
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
