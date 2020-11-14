package rsystems.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ChannelTopic extends ListenerAdapter {

    static ArrayList<Long> cooldownMap = new ArrayList<>();

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        /*
        CHANNEL TOPIC COMMAND
         */
        if (SherlockBot.commandMap.get(36).checkCommand(event.getMessage())) {

            if (!cooldownMap.contains(event.getChannel().getIdLong())) {
                if (event.getChannel().getTopic() != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(event.getChannel().getName() + " - Topic")
                            .setDescription(event.getChannel().getTopic())
                            .setColor(Color.CYAN)
                            .setFooter("Called by: " + event.getMember().getUser().getAsTag(), event.getAuthor().getEffectiveAvatarUrl());

                    try {
                        event.getChannel().sendMessage(embedBuilder.build()).queue();
                    } catch (Exception e) {
                        //todo send to error catch
                        System.out.println("Ran into error");
                        e.printStackTrace();
                    } finally {
                        embedBuilder.clear();
                    }

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                System.out.println(String.format("Adding %s | ID: %s to topic cooldown map", event.getChannel().getName(), event.getChannel().getId()));
                                cooldownMap.add(event.getChannel().getIdLong());
                                //Sleep the thread for 10 minutes
                                Thread.sleep(600000);
                            } catch (InterruptedException ie) {
                            }
                            //Remove the entry from the HashMap
                            Iterator it = cooldownMap.iterator();
                            while (it.hasNext()) {
                                Long checkId = (Long) it.next();
                                if (checkId == event.getChannel().getIdLong()) {
                                    System.out.println("Removing " + it.next() + " from channel topic cooldown map");
                                    it.remove();
                                }
                            }
                        }
                    }).start();

                } else {
                    try {
                        event.getMessage().addReaction("⚠").queue();
                    } catch(InsufficientPermissionException e){

                    }
                }
            }
        }
    }


}
