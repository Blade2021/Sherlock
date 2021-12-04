package rsystems.commands.slashCommands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import rsystems.SherlockBot;
import rsystems.objects.SlashCommand;

public class CopyChannel extends SlashCommand {

    @Override
    public Permission getDiscordPermission() {
        return Permission.MANAGE_CHANNEL;
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData().addOption(OptionType.CHANNEL,"channel","Channel to be copied",true);
    }

    @Override
    public void dispatch(User sender, MessageChannel channel, String content, SlashCommandEvent event) {

        event.deferReply().queue();

        if(SherlockBot.database.getInt("SubscriberTable","SubLevel","ChildGuildID",event.getGuild().getIdLong()) >= 1){

            MessageChannel targetChannel = event.getOption("channel").getAsMessageChannel();

            TextChannel textChannel = null;
            if(event.getGuild().getTextChannelById(targetChannel.getIdLong()) != null){
                textChannel = event.getGuild().getTextChannelById(targetChannel.getIdLong());
            }

            if(textChannel != null) {
                String channelName = textChannel.getName() + "-copy";
                TextChannel finalTextChannel = textChannel;
                textChannel.createCopy().setName(channelName).queue(success -> {
                        event.getHook().sendMessage("Your channel has been created here: " + finalTextChannel.getAsMention()).queue();

                    finalTextChannel.retrieveWebhooks().queue((webhooks -> {
                            for (Webhook hook : webhooks) {
                                if (hook.getType().getKey() == 2) {
                                    TextChannel hookChannel = hook.getGuild().getTextChannelById(hook.getSourceChannel().getIdLong());
                                    //hookChannel.follow(success).queue();
                                }
                            }
                        }));

                    });

                return;
            }
        }

        event.getHook().sendMessage("Your command could not be completed").queue();

    }

    @Override
    public String getDescription() {
        return "Copy a channel";
    }
}
