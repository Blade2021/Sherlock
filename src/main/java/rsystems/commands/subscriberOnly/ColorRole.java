package rsystems.commands.subscriberOnly;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.objects.Command;

import java.awt.*;
import java.sql.SQLException;

public class ColorRole extends Command {

    @Override
    public Permission getDiscordPermission() {
        return Permission.MANAGE_ROLES;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        String[] args = content.split("\\s+");

        if(args.length >= 2){

            Role role = null;

            if(message.getMentionedRoles().size() >= 1){
                role = message.getMentionedRoles().get(0);
            } else if(event.getGuild().getRoleById(args[0]) != null){
                role = event.getGuild().getRoleById(args[0]);
            }

            if(role != null){
                String colorCode = content.substring(args[0].length()+1);
                colorCode = colorCode.replace("# ","#");
                if(!colorCode.startsWith("#")){
                    colorCode = "#"+colorCode;
                }

                role.getManager().setColor(Color.decode(colorCode)).queue();
            }

        }
        // Role ID or Name
        // Color


    }

    @Override
    public String getHelp() {
        //todo help docs
        return null;
    }
}
