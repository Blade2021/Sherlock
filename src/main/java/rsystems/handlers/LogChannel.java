package rsystems.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.PermissionException;
import rsystems.SherlockBot;

import java.util.List;

public class LogChannel {

    public void logAction(Guild guild, String action, Member violator, Member submitter){
        String channelLogID = SherlockBot.guildMap.get(guild.getId()).getLogChannelID();
        if(channelLogID != null){
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(action)
                    .addField("Violator:",violator.getUser().getAsTag(),false)
                    .setFooter("Submitted by: " + submitter.getUser().getAsTag());

            try{
                guild.getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
            } catch(NullPointerException | PermissionException e){

            }
        }
    }

    public void logAction(Guild guild, String action, List<Member> violators, Member submitter){
        String channelLogID = SherlockBot.guildMap.get(guild.getId()).getLogChannelID();
        if(channelLogID != null){

            StringBuilder violatorNames = new StringBuilder();
            for(Member m:violators){
                violatorNames.append(m.getUser().getAsTag()).append(",");
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(action)
                    .addField("Violators:",violatorNames.toString(),false)
                    .setFooter("Submitted by: " + submitter.getUser().getAsTag());

            try{
                guild.getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
            } catch(NullPointerException | PermissionException e){

            }
        }
    }

    public void logAction(Guild guild, String action, String note, List<Member> violators, Member submitter){
        String channelLogID = SherlockBot.guildMap.get(guild.getId()).getLogChannelID();
        if(channelLogID != null){

            StringBuilder violatorNames = new StringBuilder();
            for(Member m:violators){
                violatorNames.append(m.getUser().getAsTag()).append(",");
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(action)
                    .setDescription(note)
                    .addField("Violators:",violatorNames.toString(),false)
                    .setFooter("Submitted by: " + submitter.getUser().getAsTag());

            try{
                guild.getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
            } catch(NullPointerException | PermissionException e){

            }
        }
    }

    public void logAction(Guild guild, String action, Member violator, User submitter){
        String channelLogID = SherlockBot.guildMap.get(guild.getId()).getLogChannelID();
        if(channelLogID != null){

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(action)
                    .addField("Violators:",violator.getUser().getAsTag(),false)
                    .setFooter("Submitted by: " + submitter.getAsTag());

            try{
                guild.getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
            } catch(NullPointerException | PermissionException e){

            }
        }
    }

    public void logAction(Guild guild, String action, String note, Member violator, User submitter){
        String channelLogID = SherlockBot.guildMap.get(guild.getId()).getLogChannelID();
        if(channelLogID != null){

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(action)
                    .setDescription(note)
                    .addField("Violators:",violator.getUser().getAsTag(),false)
                    .setFooter("Submitted by: " + submitter.getAsTag());

            try{
                guild.getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
            } catch(NullPointerException | PermissionException e){

            }
        }
    }

    public void logAction(Guild guild, String action, User violator){
        String channelLogID = SherlockBot.guildMap.get(guild.getId()).getLogChannelID();
        if(channelLogID != null){

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(action)
                    .addField("Violators:",violator.getAsTag(),false);

            try{
                guild.getTextChannelById(channelLogID).sendMessage(embedBuilder.build()).queue();
            } catch(NullPointerException | PermissionException e){

            }
        }
    }
}
