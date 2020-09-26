package rsystems.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LogChannel {
    private TextChannel logChannel;
    private final Guild guild;

    public LogChannel(Guild guild){
        this.guild = guild;
    }

    public LogChannel(Guild guild, TextChannel logChannel){
        this.guild = guild;
        this.logChannel = logChannel;
    }

    public TextChannel getLogChannel() {
        return logChannel;
    }

    public void setLogChannel(TextChannel logChannel) {
        this.logChannel = logChannel;
    }

    public void setLogChannel(String id){
        try{
            this.logChannel = this.guild.getTextChannelById(id);
        } catch(NullPointerException e){
            System.out.println("Cannot set logChannel. NULL found. GuildID:" + this.guild.getId());
        }
    }

    public boolean writeMessage(String message){
        try {
            this.logChannel.sendMessage(message).queue();
            return true;
        } catch(NullPointerException e){
            System.out.println("Cannot find channel");
        } catch(PermissionException e){
            System.out.println("Missing permissions when writing to log channel");
        }
        return false;
    }

    public void logAction(@NotNull String action, Member member, @NotNull String violationMessage, String admin){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(action);
        embedBuilder.setDescription(violationMessage);
        embedBuilder.setFooter("Submitted by: " + admin);

        embedBuilder.addField("User",member.getUser().getAsTag(),false);

        try{
            logChannel.sendMessage(embedBuilder.build()).queue();
        } catch(NullPointerException e){
            System.out.println("Could not write to channel");
        } finally {
            embedBuilder.clear();
        }
    }

    public void logAction(@NotNull String action, Member member, @NotNull String violationMessage, Member admin){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(action);
        embedBuilder.setDescription(violationMessage);
        embedBuilder.setFooter("Submitted by: " + admin.getUser().getAsTag());

        embedBuilder.addField("User",member.getUser().getAsTag(),false);

        try{
            logChannel.sendMessage(embedBuilder.build()).queue();
        } catch(NullPointerException e){
            System.out.println("Could not write to channel");
        } finally {
            embedBuilder.clear();
        }
    }

    public void logAction(String action, ArrayList<Member> memberArrayList, String violationMessage, Member admin){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(action);
        embedBuilder.setDescription(violationMessage);
        embedBuilder.setFooter("Submitted by: " + admin.getUser().getAsTag());

        StringBuilder userString = new StringBuilder();
        for (Member m: memberArrayList) {
            userString.append(m.getUser().getAsTag()).append(",");
        }
        embedBuilder.addField("Users",userString.toString(),false);

        try{
            logChannel.sendMessage(embedBuilder.build()).queue();
        } catch(NullPointerException e){
            System.out.println("Could not write to channel");
        } finally {
            embedBuilder.clear();
        }
    }

    public void logAction(@NotNull String action, List<Member> memberArrayList, @NotNull String violationMessage, Member admin){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(action);
        embedBuilder.setDescription(violationMessage);
        embedBuilder.setFooter("Submitted by: " + admin.getUser().getAsTag());

        StringBuilder userString = new StringBuilder();
        for (Member m: memberArrayList) {
            userString.append(m.getUser().getAsTag()).append(",");
        }
        embedBuilder.addField("Users",userString.toString(),false);

        try{
            logChannel.sendMessage(embedBuilder.build()).queue();
        } catch(NullPointerException e){
            System.out.println("Could not write to channel");
        } finally {
            embedBuilder.clear();
        }
    }

    public String getGuildID(){
        return guild.getId();
    }
}
