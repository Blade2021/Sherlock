package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
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
import java.util.Map;

public class Whitelist extends SlashCommand {

    @Override
    public CommandData getCommandData() {

        // Whitelist Guild Group
        ArrayList<SubcommandData> whitelistCommands = new ArrayList<>();
        whitelistCommands.add(new SubcommandData("enable", "Enable/Disable this function").addOption(OptionType.BOOLEAN, "enable", "True = Enable the Filter, False = Disable the filter", true));
        whitelistCommands.add(new SubcommandData("add", "Enable a sever's invites to be posted here").addOption(OptionType.STRING, "server_id", "ID of the server to be whitelisted", true).addOption(OptionType.STRING,"server_note","Description of the server for reference",false));
        whitelistCommands.add(new SubcommandData("change","Change a ID's Description").addOption(OptionType.STRING,"server_id","The ID of the server to be changed",true).addOption(OptionType.STRING,"server_note","The new description for the server ID",true));
        whitelistCommands.add(new SubcommandData("remove", "Disable a server's invites from being posted here").addOption(OptionType.STRING, "server_id", "ID of the server to be removed from the whitelist", true));
        whitelistCommands.add(new SubcommandData("list", "List all whitelisted servers"));

        return Commands.slash(this.getName().toLowerCase(), this.getDescription()).addSubcommands(whitelistCommands);
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {


        event.deferReply().setEphemeral(this.isEphemeral()).queue();

        if (event.getSubcommandName().equalsIgnoreCase("add")) {

            Long serverIDLong = null;
            if (event.getOption("server_id") != null) {
                serverIDLong = event.getOption("server_id").getAsLong();
            }

            String note = null;
            if(event.getOption("server_note") != null){
                note = event.getOption("server_note").getAsString();
            }

            try {
                if (SherlockBot.database.whiteListServer(event.getGuild().getIdLong(), serverIDLong, note) > 0) {
                    event.getHook().editOriginal(String.format("I have added `%d` to the whitelist.", serverIDLong)).queue();
                } else {
                    event.getHook().editOriginal("Nothing happened...").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("remove")) {

            Long serverIDLong = null;
            if (event.getOption("server_id") != null) {
                serverIDLong = event.getOption("server_id").getAsLong();
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
        }if (event.getSubcommandName().equalsIgnoreCase("change")) {

            Long serverIDLong = null;
            if (event.getOption("server_id") != null) {
                serverIDLong = event.getOption("server_id").getAsLong();
            }

            String note = null;
            if(event.getOption("server_note") != null){
                note = event.getOption("server_note").getAsString();
            }

            try {
                if (SherlockBot.database.putValue("InviteWhitelist","Note","TargetGuildID",serverIDLong,note) > 0){
                    event.getHook().editOriginal(String.format("I have added `%d` to the whitelist.", serverIDLong)).queue();
                } else {
                    event.getHook().editOriginal("Nothing happened...").queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("list")) {
            try {

                Map<Long,String> inviteMap = SherlockBot.database.getMap(event.getGuild().getIdLong(),"InviteWhitelist","TargetGuildID","Note");
                //ArrayList<String> whiteList = SherlockBot.database.getList(event.getGuild().getIdLong(), "InviteWhitelist", "TargetGuildID");

                if (inviteMap.size() > 0) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Server Invite Whitelist");
                    builder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));
                    builder.setDescription("All servers listed here have been whitelisted to allow invites to be posted on this server.");

                    StringBuilder guildIDString = new StringBuilder();
                    StringBuilder noteString = new StringBuilder();

                    for(Map.Entry<Long,String> entry:inviteMap.entrySet()){
                        guildIDString.append(entry.getKey()).append("\n");

                        if((entry.getValue() == null)){
                            noteString.append("null").append("\n");
                        } else {
                            noteString.append(entry.getValue()).append("\n");
                        }
                    }

                    builder.addField("Server ID:",guildIDString.toString(),true);
                    builder.addField("Note:",noteString.toString(),true);

                    event.getHook().editOriginalEmbeds(builder.build()).queue();
                    builder.clear();

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
