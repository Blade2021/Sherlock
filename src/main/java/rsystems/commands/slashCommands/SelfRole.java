package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class SelfRole extends SlashCommand {

    @Override
    public CommandData getCommandData() {

        ArrayList<SubcommandData> selfRoleCommands = new ArrayList<>();
        selfRoleCommands.add(new SubcommandData("add", "Enable a role to self added/removed").addOption(OptionType.STRING, "trigger", "The trigger to be used to add/remove the role", true).addOption(OptionType.ROLE, "role", "The role to be added/removed", true));
        selfRoleCommands.add(new SubcommandData("remove", "Remove a self-role").addOption(OptionType.STRING, "trigger", "The trigger of the self role to be removed", true));
        selfRoleCommands.add(new SubcommandData("list", "List all Self-Roles"));

        return Commands.slash(this.getName().toLowerCase(), this.getDescription()).addSubcommands(selfRoleCommands);
    }

    @Override
    public Integer getPermissionIndex() {
        return 32;
    }

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {

        if (event.getSubcommandName().equalsIgnoreCase("add")) {

            event.deferReply().setEphemeral(false).queue();

            if (event.getOption("trigger").getAsString() != null) {

                String roleTrigger = event.getOption("trigger").getAsString();
                Role role = event.getOption("role").getAsRole();

                //Query Database for current self role count
                try {
                    if(SherlockBot.database.getTableCount(event.getGuild().getIdLong(),"SelfRoles") < SherlockBot.database.getGuildData(event.getGuild().getIdLong()).getGrantedSelfRoleCount()){
                        Integer responseCode = SherlockBot.database.insertSelfRole(event.getGuild().getIdLong(),roleTrigger,role.getIdLong());

                        if(responseCode == 200){
                            reply(event,String.format("I have added `%s` as a self-role with the trigger `%s`",role.getName(),roleTrigger),false);
                        } else if(responseCode == 201) {
                            reply(event,String.format("`%s` is already a trigger.  Please try with a different role trigger",roleTrigger),false);
                        } else {
                            // DATABASE ERROR
                            reply(event,"A system error occurred.  Please try again later or contact us for support.  ERROR_ID:2301",false);
                        }
                    } else {
                        // MAX NUMBER OF SELF ROLES REACHED
                        reply(event,"Sorry, You've hit your max quota for self roles.  Contact us for information on getting more.",false);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (event.getSubcommandName().equalsIgnoreCase("list")) {

            event.deferReply().setEphemeral(true).queue();

            try {
                if(SherlockBot.database.getGuildSelfRoles(event.getGuild().getIdLong()).size() > 0) {
                    StringBuilder triggerList = new StringBuilder();
                    StringBuilder roleNameList = new StringBuilder();
                    StringBuilder roleIDList = new StringBuilder();

                    int successfulRolesFound = 0;

                    for (Map.Entry<String, Long> roleMap : SherlockBot.database.getGuildSelfRoles(event.getGuild().getIdLong()).entrySet()) {
                        if(event.getGuild().getRoleById(roleMap.getValue()) != null) {
                            successfulRolesFound++;

                            triggerList.append(roleMap.getKey()).append("\n");
                            roleNameList.append(event.getGuild().getRoleById(roleMap.getValue()).getName()).append("\n");
                            roleIDList.append(roleMap.getValue()).append("\n");
                        }
                    }

                    if(successfulRolesFound > 0) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("Self Roles:")
                                .setColor(Color.CYAN)
                                .addField("Trigger:", triggerList.toString(), true)
                                .addField("Role Name:", roleNameList.toString(), true)
                                .addField("Role ID", roleIDList.toString(), true);

                        MessageBuilder mb = new MessageBuilder();
                        mb.setEmbeds(embedBuilder.build());
                        reply(event, mb.build(),true);

                    } else {
                        reply(event,"No self roles found",true);
                    }
                } else {
                    reply(event,"No self roles found",true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Self roles are roles that any user can add/remove from themselves";
    }
}
