package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

public class ModifyGuildSettings extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            //Ignore message.  BOT LAW #2 - DO NOT LISTEN TO OTHER BOTS
        }
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        //CHANGE PREFIX
        if (SherlockBot.commands.get(4).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            try {
                SherlockBot.guildMap.get(event.getGuild().getId()).setPrefix(args[1]);
                event.getMessage().addReaction("✅").queue();
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                try {
                    event.getMessage().addReaction("⚠").queue();
                } catch (Exception global_exception) {
                    global_exception.printStackTrace();
                }
            }
        }

        //CHANGE LOG CHANNEL ID
        if (SherlockBot.commands.get(5).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            try {
                SherlockBot.guildMap.get(event.getGuild().getId()).logChannel.setLogChannel(args[1]);
                event.getMessage().addReaction("✅").queue();
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                try {
                    event.getMessage().addReaction("⚠").queue();
                } catch (Exception global_exception) {
                    global_exception.printStackTrace();
                }
            }
        }

        //GET LOG CHANNEL ID
        if (SherlockBot.commands.get(6).checkCommand(event.getMessage().getContentRaw(), SherlockBot.guildMap.get(event.getGuild().getId()).getPrefix())) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            try {
                TextChannel localLogChannel = SherlockBot.guildMap.get(event.getGuild().getId()).logChannel.getLogChannel();

                embedBuilder.setTitle("Log Channel Info");
                embedBuilder.setDescription(localLogChannel.getName());
                embedBuilder.addField("Channel ID:", localLogChannel.getId(), false);
                embedBuilder.setFooter("Called by: " + event.getMember().getEffectiveName(), event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessage(embedBuilder.build()).queue();
            } catch (IndexOutOfBoundsException | NullPointerException | PermissionException e) {
                try {
                    event.getMessage().addReaction("⚠").queue();
                } catch (Exception global_exception) {
                    global_exception.printStackTrace();
                }
            } finally {
                embedBuilder.clear();
            }
        }


    }
}
