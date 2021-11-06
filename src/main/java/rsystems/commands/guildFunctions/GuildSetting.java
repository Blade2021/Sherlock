package rsystems.commands.guildFunctions;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.sql.SQLException;
import java.util.Iterator;

public class GuildSetting extends Command {

    private static final String[] ALIASES = new String[] {"gs","guild","setting"};


    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void dispatch(User sender, MessageChannel channel, Message message, String content, GuildMessageReceivedEvent event) throws SQLException {
        String[] args = content.split("\\s+");

        if(args.length >= 2){

            final String subCommand = args[0];
            boolean pushUpdate = false;

            // LOG CHANNEL
            if((subCommand.equalsIgnoreCase("lc")) || (subCommand.equalsIgnoreCase("logChannel"))){

                Long previousLogChannelID = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getLogChannelID();

                if(message.getMentionedChannels().size() > 0){
                    final TextChannel logChannel = message.getMentionedChannels().get(0);
                    if(logChannel.canTalk()){
                        SherlockBot.guildMap.get(event.getGuild().getIdLong()).setLogChannelID(logChannel.getIdLong());
                        message.addReaction("✅").queue();

                        if(event.getGuild().getTextChannelById(previousLogChannelID) != null){
                            registerLogChannel(event.getGuild().getTextChannelById(previousLogChannelID),logChannel);
                        } else {
                            registerLogChannel(null,logChannel);
                        }
                        pushUpdate = true;
                    }
                } else if(args[1].equalsIgnoreCase("here")){
                    //No verification required.  The channel obviously exists and the bot can obviously see it.

                    if(event.getChannel().canTalk()) {
                        SherlockBot.guildMap.get(event.getGuild().getIdLong()).setLogChannelID(event.getChannel().getIdLong());
                        message.addReaction("✅").queue();
                        if(event.getGuild().getTextChannelById(previousLogChannelID) != null){
                            registerLogChannel(event.getGuild().getTextChannelById(previousLogChannelID),event.getChannel());
                        } else {
                            registerLogChannel(null,event.getChannel());
                        }
                        pushUpdate = true;
                    }

                } else {

                    final Long channelID = Long.valueOf(args[1]);
                    if ((channelID != null) && (event.getGuild().getTextChannelById(channelID) != null)) {

                        final TextChannel logChannel = event.getGuild().getTextChannelById(channelID);
                        if(logChannel.canTalk()) {
                            SherlockBot.guildMap.get(event.getGuild().getIdLong()).setLogChannelID(event.getChannel().getIdLong());
                            message.addReaction("✅").queue();

                            if(event.getGuild().getTextChannelById(previousLogChannelID) != null){
                                registerLogChannel(event.getGuild().getTextChannelById(previousLogChannelID),logChannel);
                            } else {
                                registerLogChannel(null,logChannel);
                            }
                            pushUpdate = true;
                        }

                    }
                }

            }

            if(pushUpdate) {
                if (SherlockBot.database.updateGuild(SherlockBot.guildMap.get(event.getGuild().getIdLong())) < 1) {
                    //error
                } else {
                    message.addReaction("\uD83D\uDCE8").queue();
                }
            }

        }

    }

    private void registerLogChannel(TextChannel previousLogChannel, TextChannel logChannel){

        if(previousLogChannel.getIdLong() != logChannel.getIdLong()) {
            if (previousLogChannel != null) {
                previousLogChannel.retrieveWebhooks().queue(webhooks -> {

                    Iterator it = webhooks.iterator();
                    while (it.hasNext()) {
                        Webhook hook = (Webhook) it.next();
                        if (hook.getSourceChannel().getId().equalsIgnoreCase(Config.get("AnnouncementChannelID"))) {
                            previousLogChannel.deleteWebhookById(hook.getId()).queue();
                            System.out.println("Webhook removed from previous channel");
                            break;
                        }
                    }
                });
            }
        }

        logChannel.retrieveWebhooks().queue(webhooks -> {
            boolean skipRegister = false;

            for(Webhook webhook:webhooks){
                if(webhook.getSourceChannel().getId().equalsIgnoreCase(Config.get("AnnouncementChannelID"))){
                    skipRegister = true;
                    break;
                }
            }

            if(!skipRegister){
                System.out.println("not skipping");
                TextChannel newsChannel = SherlockBot.jda.getGuildById(Config.get("MainGuild")).getTextChannelById(Config.get("AnnouncementChannelID"));
                newsChannel.follow(logChannel).queue();
            }

            //System.out.println(webhooks);
        });
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
