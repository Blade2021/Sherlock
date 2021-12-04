package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import rsystems.SherlockBot;
import rsystems.handlers.Dispatcher;
import rsystems.objects.Command;
import rsystems.objects.SlashCommand;

import java.sql.SQLException;

public class Apple extends SlashCommand {

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandEvent event) {

        event.deferReply(true).queue();

        StringBuilder commandsString = new StringBuilder();

        for(Command c: SherlockBot.dispatcher.getCommands()){
            try {
                if(Dispatcher.isAuthorized(c,event.getGuild().getIdLong(),event.getMember(),c.getPermissionIndex())){
                    if(commandsString.toString().isEmpty()){
                        commandsString.append(c.getName());
                    } else {
                        commandsString.append(", ").append(c.getName());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        /*
        User has no commands accessible to them
         */
        if(commandsString.toString().isEmpty()){
            commandsString.append("You have no commands available");
        }

        reply(event,commandsString.toString(),true);

        /*
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setActionRows(ActionRow.of(Button.primary("test","test"))).setContent("This is just a test");

        reply(event,messageBuilder.build(),true,test -> {
            //test.editOriginal("just kidding").queueAfter(3, TimeUnit.SECONDS);
        });

         */
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
