package rsystems.commands.modCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Reason extends Command {

    @Override
    public Integer getPermissionIndex() {
        return 32;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        String[] args = content.split("\\s+");

        if(args.length >= 2){

            int caseID = Integer.parseInt(args[0]);
            String reason = content.substring(args[0].length()+1);

            if(reason.length() >= 200){
                reply(event,String.format("All reasons must be under 200 characters.\nYour Message has: %d characters",reason.length()));
                return;
            }

            if(SherlockBot.database.putValue("CaseTable","Reason","ChildGuildID",event.getGuild().getIdLong(),"CaseID",caseID,reason) >= 1){
                message.addReaction("✅").queue();

                Long previousMessageID = SherlockBot.database.getLong("CaseTable","LogMessageID","ChildGuildID",event.getGuild().getIdLong(),"CaseID",caseID);
                Long logChannelID = SherlockBot.database.getLong("GuildTable","LogChannelID","GuildID",event.getGuild().getIdLong());

                if((previousMessageID != null) && (logChannelID != null)){
                    // gold star...

                    TextChannel textChannel = event.getGuild().getTextChannelById(logChannelID);
                    if(textChannel != null) {
                        textChannel.retrieveMessageById(previousMessageID).queue(success -> {

                            if(success.getEmbeds().size() == 1) {
                                MessageEmbed embed = success.getEmbeds().get(0);
                                EmbedBuilder builder = new EmbedBuilder(embed);

                                int reasonFieldIndex = -1;

                                for(int x=0;x<embed.getFields().size();x++){
                                    if(embed.getFields().get(x).getName().equalsIgnoreCase("Reason")){
                                        reasonFieldIndex = x;
                                    }
                                }

                                if(reasonFieldIndex > 0) {
                                    builder.getFields().set(reasonFieldIndex, new MessageEmbed.Field("Reason", reason, false));
                                }
                                success.editMessageEmbeds(builder.build()).queue();
                            }
                        });
                    }
                }

            }

        }
    }

    @Override
    public String getHelp() {
        return "`{prefix}{command} (caseID) (Reason)\n\nAdd a note to a case event";
    }
}
