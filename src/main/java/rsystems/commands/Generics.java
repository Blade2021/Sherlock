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
        if (SherlockBot.commandMap.get(7).checkCommand(event.getMessage())){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("BoT Information")
                    .setColor(Color.ORANGE)
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setDescription("You can find detailed information and commands here: [Sherlock Github Page](https://github.com/Blade2021/Sherlock/wiki/Commands)")
                    .setFooter("Called by: " + event.getMember().getEffectiveName(),event.getMember().getUser().getEffectiveAvatarUrl());

            if(event.getChannel().canTalk()) {
                event.getChannel().sendMessage(embedBuilder.build()).queue(success -> {
                    SherlockBot.database.insertAutoTriggerDelete(event.getGuild().getIdLong(),event.getMessageIdLong(),success.getIdLong());
                });
            }

            embedBuilder.clear();
            return;
        }

        /*
        INFO COMMAND
         */
        if (SherlockBot.commandMap.get(15).checkCommand(event.getMessage())){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("BoT Information")
                    .setColor(Color.ORANGE)
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setDescription("Greetings, I am Sherlock.  \n\nI am built upon dynamics that allow me to be a great partner for any and all guilds, helping with administration and simple guild functions.\n\nI am currently serving: **" + event.getJDA().getGuilds().size() + " servers**")
                    .addField("Ping:",event.getGuild().getJDA().getGatewayPing() + "ms",true)
                    .addField("Version:",SherlockBot.version,true)
                    .addField("Source:","[Github](https://github.com/Blade2021/Sherlock)",true)
                    .setFooter("Called by: " + event.getMember().getEffectiveName(),event.getMember().getUser().getEffectiveAvatarUrl());

            if(event.getChannel().canTalk()) {
                event.getChannel().sendMessage(embedBuilder.build()).queue(success -> {
                    SherlockBot.database.insertAutoTriggerDelete(event.getGuild().getIdLong(),event.getMessageIdLong(),success.getIdLong());
                });
            }

            embedBuilder.clear();

        }

        /*
        GUILD INFO COMMAND
         */
        if (SherlockBot.commandMap.get(16).checkCommand(event.getMessage())){
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
        if (SherlockBot.commandMap.get(17).checkCommand(event.getMessage())){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("BoT Information")
                    .setColor(Color.cyan)
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setDescription("Hello!  I'm Sherlock!  Please follow the setup and configuration instructions [here](https://github.com/Blade2021/Sherlock/wiki/Setup-and-Configuration).")
                    .setFooter("Called by: " + event.getMember().getEffectiveName(),event.getMember().getUser().getEffectiveAvatarUrl());

            if(event.getChannel().canTalk()) {
                event.getChannel().sendMessage(embedBuilder.build()).queue(success -> {
                    SherlockBot.database.insertAutoTriggerDelete(event.getGuild().getIdLong(),event.getMessageIdLong(),success.getIdLong());
                });
            }

            embedBuilder.clear();
        }
    }
}
