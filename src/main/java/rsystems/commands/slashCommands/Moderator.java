package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class Moderator extends SlashCommand {
    @Override
    public CommandData getCommandData() {

        ArrayList<SubcommandGroupData> subCmdGroupData = new ArrayList<>();
        CommandData commandData = new CommandData(this.getName().toLowerCase(), "Moderator settings");

        ArrayList<SubcommandData> modSubCommands = new ArrayList<>();
        modSubCommands.add(new SubcommandData("add", "Add a moderator role").addOption(OptionType.ROLE, "role", "The role to be configured", true).addOption(OptionType.NUMBER, "permlevel", "Permission level for this moderator group",true));
        modSubCommands.add(new SubcommandData("remove", "Remove a moderator role").addOption(OptionType.ROLE, "role", "The role to be removed from the moderator table", true));
        modSubCommands.add(new SubcommandData("list", "List all moderator roles"));

        commandData.addSubcommands(modSubCommands);

        return commandData;
    }

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandEvent event) {

        event.deferReply().setEphemeral(true).queue();

        if (event.getSubcommandName().equalsIgnoreCase("add")) {

            if (event.getOption("role").getAsRole() != null) {

                try {
                    if(SherlockBot.database.submitModeratorRole(event.getGuild().getIdLong(),event.getOption("role").getAsRole().getIdLong(),Integer.parseInt(event.getOption("permlevel").getAsString())) >= 1){
                        event.getHook().editOriginal(String.format("Added %s to the mod role table with permissions: %s",event.getOption("role").getAsRole().getAsMention(),event.getOption("permlevel").getAsString())).queue();
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                event.getHook().editOriginal(String.format("An error occurred when trying to add the mod role.  Does it exist in the table already?")).queue();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("remove")) {

            if (event.getOption("role").getAsRole() != null) {
                try {
                    if(SherlockBot.database.deleteRow("ModRoleTable","ModRoleID",event.getOption("role").getAsRole().getIdLong()) >= 1){
                        event.getHook().editOriginal(String.format("%s removed successfully",event.getOption("role").getAsRole().getAsMention())).queue();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                event.getHook().editOriginal(String.format("An error occurred when trying to remove the mod role.")).queue();
            }
        } else if (event.getSubcommandName().equalsIgnoreCase("list")) {

            try {
                Map<Long,Integer> modRoleMap = SherlockBot.database.getModRoles(event.getGuild().getIdLong());

                if(modRoleMap.size() > 0){
                    EmbedBuilder builder = new EmbedBuilder();
                    StringBuilder roleNameString = new StringBuilder();
                    StringBuilder roleIDSting = new StringBuilder();
                    StringBuilder rolePermString = new StringBuilder();

                    for(Map.Entry<Long,Integer> entry:modRoleMap.entrySet()){

                        if(event.getGuild().getRoleById(entry.getKey()) != null) {
                            roleNameString.append(event.getGuild().getRoleById(entry.getKey()).getName());
                            roleIDSting.append(entry.getKey()).append("\n");
                            rolePermString.append(entry.getValue()).append("\n");
                        }
                    }

                    builder.setTitle("Moderator Roles");
                    builder.setColor(SherlockBot.getColor(SherlockBot.colorType.GENERIC));
                    builder.addField("Role Name",roleNameString.toString(),true);
                    builder.addField("Role ID",roleIDSting.toString(),true);
                    builder.addField("Role Permissions",rolePermString.toString(),true);

                    event.getHook().editOriginalEmbeds(builder.build()).queue();
                    builder.clear();

                } else {
                    reply(event,"No mod roles found",this.isEphemeral());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getDescription() {
        return "Moderator roles";
    }
}
