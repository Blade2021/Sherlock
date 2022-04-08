package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import rsystems.objects.SlashCommand;

public class Unban extends SlashCommand {

    @Override
    public Permission getDiscordPermission() {
        return Permission.BAN_MEMBERS;
    }

    @Override
    public Integer getPermissionIndex() {
        return 512;
    }

    @Override
    public CommandData getCommandData() {

        return Commands.slash(this.getName().toLowerCase(), this.getDescription()).addOption(OptionType.NUMBER,"userid","The banned user's ID");
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandInteractionEvent event) {
        event.deferReply(this.isEphemeral()).queue();

        final Long userID = event.getOption("userid").getAsLong();
        event.getGuild().retrieveBanById(userID).queue(ban -> {
            event.getGuild().unban(ban.getUser()).reason("Requested by " + event.getMember().getUser().getAsTag()).queue(success -> {
                reply(event,ban.getUser().getAsTag() + " has been unbanned",this.isEphemeral());
            });
        }, failure -> {
            reply(event,"Could not find user on the ban list",this.isEphemeral());
        });
    }

    @Override
    public String getDescription() {
        return "Unban a banned user by UserID";
    }
}
