package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;
import java.util.ArrayList;

public class Whitelist extends SlashCommand {

    @Override
    public CommandData getCommandData() {

        CommandData commandData = new CommandData(this.getName().toLowerCase(), this.getDescription());

        // Whitelist Guild Group
        ArrayList<SubcommandData> whitelistCommands = new ArrayList<>();
        whitelistCommands.add(new SubcommandData("enable", "Enable/Disable this function").addOption(OptionType.BOOLEAN, "enable", "True = Enable the Filter, False = Disable the filter", true));
        whitelistCommands.add(new SubcommandData("add", "Enable a sever's invites to be posted here").addOption(OptionType.STRING, "serverid", "ID of the server to be whitelisted", true));
        whitelistCommands.add(new SubcommandData("remove", "Disable a server's invites from being posted here").addOption(OptionType.STRING, "serverid", "ID of the server to be removed from the whitelist", true));
        whitelistCommands.add(new SubcommandData("list", "List all whitelisted servers"));

        commandData.addSubcommands(whitelistCommands);

        return commandData;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandEvent event) {


        event.deferReply().setEphemeral(this.isEphemeral()).queue();

        if (event.getSubcommandName().equalsIgnoreCase("add")) {

            Long serverIDLong = null;
            if (event.getOption("serverid") != null) {
                serverIDLong = event.getOption("serverid").getAsLong();
            }

            try {
                if (SherlockBot.database.whiteListServer(event.getGuild().getIdLong(), serverIDLong) > 0) {
                    event.getHook().editOriginal(String.format("I have added `%d` to the whitelist.", serverIDLong)).queue();
                } else {
                    event.getHook().editOriginal("Nothing happened...").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("remove")) {

            Long serverIDLong = null;
            if (event.getOption("serverid") != null) {
                serverIDLong = event.getOption("serverid").getAsLong();
            }

            try {
                if (SherlockBot.database.deWhiteListServer(event.getGuild().getIdLong(), serverIDLong) > 0) {
                    event.getHook().editOriginal(String.format("I have removed `%d` from the whitelist.", serverIDLong)).queue();
                } else {
                    event.getHook().editOriginal("Nothing happened...").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("list")) {
            try {
                ArrayList<String> whiteList = SherlockBot.database.getList(event.getGuild().getIdLong(), "InviteWhitelist", "TargetGuildID");

                if (whiteList.size() > 0) {
                    event.getHook().editOriginal(whiteList.toString()).queue();
                } else {
                    event.getHook().editOriginal("There are no servers that are currently whitelisted.").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (event.getSubcommandName().equalsIgnoreCase("enable")) {

            Boolean enableInput = event.getOption("enable").getAsBoolean();

            if (enableInput != null) {
                if (enableInput) {

                    SherlockBot.getGuildSettings(event.getGuild().getIdLong()).setInviteFilterEnabled(1);
                    SherlockBot.getGuildSettings(event.getGuild().getIdLong()).save();

                    event.getHook().editOriginal("Server invite filter `enabled`").queue();

                } else {
                    SherlockBot.getGuildSettings(event.getGuild().getIdLong()).setInviteFilterEnabled(0);
                    SherlockBot.getGuildSettings(event.getGuild().getIdLong()).save();

                    event.getHook().editOriginal("Server invite filter `disabled`").queue();
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Set which servers may have invites posted in your server.";
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public Integer getPermissionIndex() {
        return 8192;
    }

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }
}
