package rsystems.commands.guildFunctions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import rsystems.handlers.LogMessage;
import rsystems.objects.Command;

import java.sql.SQLException;

public class GuildSetting extends Command {

    private static final String[] ALIASES = new String[]{"gs", "guild", "setting"};

    @Override
    public Permission getDiscordPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public Integer getPermissionIndex() {
        return 32768;
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, MessageReceivedEvent event) throws SQLException {
        String[] args = content.split("\\s+");

        if (args.length >= 2) {

            final String subCommand = args[0];

            // LOG CHANNEL
            if ((subCommand.equalsIgnoreCase("lc")) || (subCommand.equalsIgnoreCase("logChannel"))) {

                TextChannel logChannel = null;
                if (message.getMentionedChannels().size() > 0) {
                    logChannel = message.getMentionedChannels().get(0);
                    if (logChannel.canTalk()) {
                        if (LogMessage.registerLogChannel(event.getGuild(), logChannel)) {
                            message.addReaction("✅").queue();
                            return;
                        }
                    }
                }
            } else if (args[1].equalsIgnoreCase("here")) {
                //No verification required.  The channel obviously exists and the bot can obviously see it.

                if (event.getTextChannel().canTalk()) {
                    LogMessage.registerLogChannel(event.getGuild(), event.getTextChannel());
                    message.addReaction("✅").queue();
                    return;
                }

            }
        }

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return ALIASES;
    }
}
