package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AutoRole extends SlashCommand {

    @Override
    public Integer getPermissionIndex() {
        return 64;
    }

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public CommandData getCommandData() {

        //CommandData commandData = Commands.slash(this.getName().toLowerCase(),this.getDescription());

        // Filter Group
        ArrayList<SubcommandData> subCommands = new ArrayList<>();
        subCommands.add(new SubcommandData("add", "Set a role to be added to a user on join").addOption(OptionType.ROLE, "role", "The role to be added", true));
        subCommands.add(new SubcommandData("remove", "Remove an AutoRole").addOption(OptionType.ROLE, "role", "The role to be removed", true));
        subCommands.add(new SubcommandData("list", "List all AutoRoles"));

        CommandData commandData = Commands.slash(this.getName().toLowerCase(),this.getDescription()).addSubcommands(subCommands);

        return commandData;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {

        if ((event.getSubcommandName().equalsIgnoreCase("add") || (event.getSubcommandName().equalsIgnoreCase("remove")))) {

            event.deferReply().setEphemeral(false).queue();
            final Role role = event.getOption("role").getAsRole();

            Role authorHighestRole = null;
            if(event.getMember().getRoles().size() > 0) {
                authorHighestRole = event.getMember().getRoles().get(0);
            }

            if((role != null) && (((authorHighestRole != null) && (authorHighestRole.canInteract(role))) || event.getMember().isOwner())) {

                if (event.getSubcommandName().equalsIgnoreCase("add")) {
                    //Query Database for current auto role count
                    try {
                        if (SherlockBot.database.getTableCount(event.getGuild().getIdLong(), "AutoRoles") < SherlockBot.database.getGuildData(event.getGuild().getIdLong()).getGrantedAutoRoleCount()) {
                            if (SherlockBot.database.insertAutoRole(event.getGuild().getIdLong(), role.getIdLong()) == 200) {

                                MessageBuilder mb = new MessageBuilder();
                                mb.append(String.format("`%s` has been added as an AutoRole", role.getName()));
                                mb.setActionRows(ActionRow.of(Button.primary("listar", "List Roles")));

                                reply(event, mb.build(), false, MessageHook -> {

                                });

                            } else if (SherlockBot.database.insertAutoRole(event.getGuild().getIdLong(), role.getIdLong()) == 400) {

                                MessageBuilder mb = new MessageBuilder();
                                mb.append(String.format("`%s` is already listed as an AutoRole", role.getName()));
                                mb.setActionRows(ActionRow.of(Button.primary("listar", "List Roles")));

                                reply(event, mb.build(), false, MessageHook -> {

                                });

                            } else {


                                event.getHook().deleteOriginal().queue();
                                event.getHook().sendMessage("A system error occurred.  Please try again later or contact us for support.  ERROR_ID:2302").setEphemeral(true).queue();

                                // DATABASE ERROR
                                //reply(event,"A system error occurred.  Please try again later or contact us for support.  ERROR_ID:2302",false);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if(SherlockBot.database.getAutoRoles(event.getGuild().getIdLong()).contains(role.getIdLong())){
                            Integer result = SherlockBot.database.deleteRow("AutoRoles","ChildGuildID",event.getGuild().getIdLong(),"RoleID",role.getIdLong());
                            if((result != null) && (result > 0)){
                                reply(event,"AutoRole Removed",isEphemeral());
                            } else {
                                reply(event,"An error occurred",isEphemeral());
                            }
                        }
                    } catch (SQLException e) {
                        reply(event,"An error occurred",isEphemeral());
                    }
                }
            }
        }

        else if(event.getSubcommandName().equalsIgnoreCase("list")){
            event.deferReply().queue();

            event.getHook().editOriginalEmbeds(listAutoRoles(event.getGuild().getIdLong())).queue();
        }
    }

    @Override
    public String getDescription() {
        return "AutoRoles are roles that are added to a user when they join the server";
    }

    public static MessageEmbed listAutoRoles(Long guildID){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.cyan);

        ArrayList<Long> roleIDList = new ArrayList<>();

        try {
            roleIDList = SherlockBot.database.getAutoRoles(guildID);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        StringBuilder roleNameString = new StringBuilder();
        StringBuilder roleIDString = new StringBuilder();
        Map<Long, Role> roleMap = new LinkedHashMap<>();

        if(roleIDList.size() > 0){

            List<Role> roleList = SherlockBot.jda.getGuildById(guildID).getRoles();

            for(Long id:roleIDList){
                for(Role role:roleList){
                    if(role.getIdLong() == id){
                        roleMap.putIfAbsent(id,role);
                        roleNameString.append(role.getName()).append("\n");
                        roleIDString.append(id).append("\n");
                    }
                }
            }
        }

        if(roleIDString.length() > 0){
            eb.addField("Role Name:", roleNameString.toString(),true);
            eb.addField("Role ID",roleIDString.toString(),true);
        } else {
            eb.setDescription("No AutoRoles found");
        }

        return eb.build();
    }
}
