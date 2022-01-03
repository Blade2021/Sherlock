package rsystems.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.awt.*;

public class ErrorReportHandler {

    public static void sendErrorReport(Command c, User callingUser, Exception exception){
        SherlockBot.jda.getUserById(SherlockBot.botOwnerID).openPrivateChannel().queue((channel) -> {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("System Exception Encountered")
                    .setColor(Color.RED)
                    .addField("Command:",c.getName(),true)
                    .addField("Calling User:",callingUser.getAsTag(),true)
                    .addBlankField(true)
                    .addField("Exception:",exception.toString(),false)
                    .setDescription(exception.getCause().getMessage().substring(0,exception.getCause().getMessage().indexOf(":")));

            channel.sendMessageEmbeds(embedBuilder.build()).queue();

            embedBuilder.clear();
        });
    }

    public static void sendErrorReport(String report, Long guildID){
        SherlockBot.jda.getUserById(SherlockBot.botOwnerID).openPrivateChannel().queue((channel) -> {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("System Error Encountered")
                    .setColor(Color.RED)
                    .addField("Guild ID",guildID.toString(),true)
                    .setDescription(report);

            channel.sendMessageEmbeds(embedBuilder.build()).queue();

            embedBuilder.clear();
        });
    }
}
