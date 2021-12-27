package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;

public class Ban extends SlashCommand {

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public Permission getDiscordPermission() {
        return Permission.BAN_MEMBERS;
    }

    @Override
    public CommandData getCommandData() {

        CommandData commandData = super.getCommandData();
        commandData.addOption(OptionType.USER, "user", "User to be banned", true)
                .addOption(OptionType.STRING,"reason","The reason the user is getting banned", true);

        return commandData;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandEvent event) {

        event.deferReply(this.isEphemeral()).queue();

        if(event.getOption("user").getAsMember() != null){

            String reason = null;
            if(event.getOptions().contains("reason")){
                reason = event.getOption("reason").getAsString();
            }
            handleBanEvent(event.getOption("user").getAsMember(),event.getMember(),event,reason);
        }

    }

    public void handleBanEvent(final Member member, final Member moderator, final SlashCommandEvent event, final String reason){
        event.getGuild().ban(member,0,reason).queue(success -> {

            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setContent(String.format("%s has been banned",member.getUser().getAsTag()));
            messageBuilder.setActionRows(ActionRow.of(Button.danger("unban:"+member.getIdLong(),"Unban")));

            reply(event,messageBuilder.build(),this.isEphemeral(),messageReply -> {
                try {
                    SherlockBot.database.insertBanEvent(event.getGuild().getIdLong(),member.getIdLong(),moderator.getIdLong(),messageReply.getInteraction().getIdLong());

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }, failure -> {
            reply(event,"Failed to ban user",this.isEphemeral());
        });
    }

    @Override
    public String getDescription() {
        return "Ban user(s)";
    }
}
