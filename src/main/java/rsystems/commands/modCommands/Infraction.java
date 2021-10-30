package rsystems.commands.modCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;
import rsystems.objects.InfractionObject;

import java.sql.SQLException;

public class Infraction extends Command {

    private static final String[] ALIASES = new String[] {"Warn", "Warning"};

    @Override
    public Integer getPermissionIndex() {
        return 2;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(final User sender, final MessageChannel channel, final Message message, String content, final GuildMessageReceivedEvent event) throws SQLException {
        Member offender = null;
        String[] args = content.split("\\s+");

        if(args.length >= 1) {

            if (message.getMentionedMembers().size() > 0) {
                offender = message.getMentionedMembers().get(0);

                if(message.getMentionedMembers().size() > 1){

                    // Remove other member's names from content.
                    for(Member m:message.getMentionedMembers()){
                        content = content.replaceAll(m.getAsMention(),"");
                    }
                }
            } else {
                offender.getGuild().getMemberById(args[0]);
            }

            // Member was found
            if(offender != null){

                args = content.split("\\s+");

                InfractionObject infractionObject = new InfractionObject(event.getGuild().getIdLong());
                infractionObject.setUserID(offender.getIdLong());
                infractionObject.setEventType(InfractionObject.EventType.WARNING);
                infractionObject.setModeratorID(message.getAuthor().getIdLong());
                infractionObject.setUserTag(offender.getUser().getAsTag());
                infractionObject.setModeratorTag(message.getAuthor().getAsTag());

                if(args.length > 1){
                    infractionObject.setNote(content.substring(args[0].length()));
                }

                // Insert infraction level warning into database for user

                // Log infraction message to log channel

                channelReply(event,infractionObject.createEmbedMessge(),Success -> {
                    try {
                        SherlockBot.database.putValue("CaseTable","LogMessageID","ChildGuildID",event.getGuild().getIdLong(),"CaseID",infractionObject.getCaseNumber(),Success.getIdLong());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                SherlockBot.database.insertCaseEvent(event.getGuild().getIdLong(),infractionObject);

                // Show successful command using emoji
                message.addReaction("✅").queue(Success -> {
                    //message.delete().queueAfter(30, TimeUnit.SECONDS);
                });

            }

        }
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getAliases(){
        return ALIASES;
    }
}
