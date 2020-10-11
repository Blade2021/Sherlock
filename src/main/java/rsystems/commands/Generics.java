package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.awt.*;
import java.time.format.DateTimeFormatter;

public class Generics extends ListenerAdapter {
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        /*
        COMMANDS COMMAND
         */
        if (SherlockBot.commands.get(7).checkCommand(event.getMessage().getContentDisplay(),event.getGuild().getId())){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("BoT Information")
                    .setColor(Color.ORANGE)
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setDescription("You can find detailed information and commands here: [Sherlock Github Page](https://github.com/Blade2021/Sherlock/wiki/Commands)")
                    .setFooter("Called by: " + event.getMember().getEffectiveName(),event.getMember().getUser().getEffectiveAvatarUrl());

            if(event.getChannel().canTalk()) {
                event.getChannel().sendMessage(embedBuilder.build()).queue();
            }

            embedBuilder.clear();
            return;
        }

        /*
        INFO COMMAND
         */
        if (SherlockBot.commands.get(15).checkCommand(event.getMessage().getContentDisplay(),event.getGuild().getId())){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("BoT Information")
                    .setColor(Color.ORANGE)
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setDescription("Greetings, I am Sherlock.  \n\nI am built upon dynamics that allow me to be a great partner for any and all guilds, helping with administration and simple guild functions.")
                    .addField("Ping:",event.getGuild().getJDA().getGatewayPing() + "ms",true)
                    .addField("Version:",SherlockBot.version,true)
                    .addField("Source:","[Github](https://github.com/Blade2021/Sherlock)",true)
                    .setFooter("Called by: " + event.getMember().getEffectiveName(),event.getMember().getUser().getEffectiveAvatarUrl());

            if(event.getChannel().canTalk()) {
                event.getChannel().sendMessage(embedBuilder.build()).queue();
            }

            embedBuilder.clear();
        }

        /*
        GUILD INFO COMMAND
         */
        if (SherlockBot.commands.get(16).checkCommand(event.getMessage().getContentDisplay(),event.getGuild().getId())){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("BoT Information")
                    .setColor(Color.cyan)
                    .setThumbnail(event.getGuild().getIconUrl())
                    .addField("Guild ID:",event.getGuild().getId(),true)
                    .addField("Total Members:",String.valueOf(event.getGuild().getMemberCount()),true)
                    .addField("Creation Date:",event.getGuild().getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE),true)
                    .setFooter("Called by: " + event.getMember().getEffectiveName(),event.getMember().getUser().getEffectiveAvatarUrl());

            if(event.getChannel().canTalk()) {
                event.getChannel().sendMessage(embedBuilder.build()).queue();
            }

            embedBuilder.clear();
        }

        /*
        GUILD INFO COMMAND
         */
        if (SherlockBot.commands.get(17).checkCommand(event.getMessage().getContentDisplay(),event.getGuild().getId())){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("BoT Information")
                    .setColor(Color.cyan)
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setDescription("Hello!  I'm Sherlock!  Please follow the setup and configuration instructions [here](https://github.com/Blade2021/Sherlock/wiki/Setup-and-Configuration).")
                    .setFooter("Called by: " + event.getMember().getEffectiveName(),event.getMember().getUser().getEffectiveAvatarUrl());

            if(event.getChannel().canTalk()) {
                event.getChannel().sendMessage(embedBuilder.build()).queue();
            }

            embedBuilder.clear();
        }
    }
}
