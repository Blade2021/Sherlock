package rsystems.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import rsystems.SherlockBot;

public class SlashCommandEvents extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getName().equals("hello")) {
            event.reply("Click the button to say hello")
                    .addActionRow(
                            Button.primary("hello", "Click Me"), // Button with only a label
                            Button.primary("noclick", "Don't click me")) // Button with only an emoji
                    .queue();
        } else if (event.getName().equals("reason")) {

            int caseEvent = Integer.parseInt(event.getOption("id").getAsString());

            String reason = event.getOption("reason").getAsString();
            System.out.println(reason);

            event.reply(String.format("Attempting to set reason for Case# %d", caseEvent)).setEphemeral(true).queue();
        } else if (event.getName().equals("gs")) {
            event.deferReply(true).queue();

            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                if (event.getSubcommandName().equals("logchannel")) {
                    if ((event.getOptions().size() > 0) && (event.getOption("channel").getAsChannel() != null)) {
                        MessageChannel channel = event.getOption("channel").getAsChannel().asTextChannel();

                        event.getHook().sendMessage(String.format("Setting Log Channel for Sherlock to: %s | %s", channel.getName(), channel.getId())).setEphemeral(true).queue();
                        System.out.println(event.getOption("channel").getAsChannel().asTextChannel().getName());
                    } else {

                        if ((SherlockBot.getGuildSettings(event.getGuild().getIdLong()).getLogChannelID() != null) && (SherlockBot.getGuildSettings(event.getGuild().getIdLong()).getLogChannelID() != 0)) {
                            System.out.println(SherlockBot.guildMap.get(event.getGuild().getIdLong()).getLogChannelID());
                            event.getHook().sendMessage("The current Log Channel is: " + event.getGuild().getTextChannelById(SherlockBot.getGuildSettings(event.getGuild().getIdLong()).getLogChannelID())).setEphemeral(true).queue();
                        } else {
                            event.getHook().sendMessage("No current log channel is configured.").setEphemeral(true).queue();
                        }
                    }
                } else {
                    event.getHook().sendMessage("You wander into an endless void").setEphemeral(true).queue();
                }
            } else {
                event.getHook().sendMessage("You don't have proper access for that.").setEphemeral(true).queue();
            }
        }

    }
}
