package rsystems.commands.modCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;

public class Setup extends Command {
    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(SherlockBot.getColor("generic"));
        builder.setTitle("Sherlock Setup");
        builder.setDescription("Here are the standard settings/setup steps:");
        builder.addField("Step 1","Move the `Sherlock Role` to the **highest role allowed**, any roles above this will **NOT** be able to be moderated by Sherlock\n\nNext move `SL-Quarantine` right below the Sherlock Role in the list",false);
        builder.addField("Step 2","Set your own prefix!\n`/guildsetting bot-prefix (prefix)`",false);
        builder.addField("Step 3", "Set a Log channel\nThis will be used for Sherlock announcements or Log messages\n`/guildsetting logging set (channel)`",false);
        builder.addField("Step 4","Add any words you want filtered.  % can be used in place of a \"space\"\n`/guildsetting filter add (word)`",false);
        builder.addField("Step 5","Setup your moderators.  [Using this matrix](https://github.com/Blade2021/Sherlock/wiki/Commands#moderator-role-assignment)\n`/moderator add (Role) (Permission Level)`",false);
        builder.addField("You're Finished!","Profit.",false);
        reply(event,builder.build());

        builder.clear();
    }

    @Override
    public String getHelp() {
        return "Get a list of steps for setting up Sherlock for the first time.";
    }
}
