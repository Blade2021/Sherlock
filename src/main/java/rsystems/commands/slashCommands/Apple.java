package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import rsystems.objects.SlashCommand;

public class Apple extends SlashCommand {

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandEvent event) {

        event.deferReply(true).queue();

        event.getGuild().retrieveCommands().queue(success -> {
            success.forEach(cmd -> {
                cmd.delete().queue();
            });
        });

    }

    @Override
    public String getDescription() {
        return "just a test";
    }

    @Override
    public boolean isSubscriberCommand() {
        return true;
    }
}
