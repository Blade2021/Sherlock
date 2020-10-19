package rsystems.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import static rsystems.SherlockBot.database;

public class WelcomeSettings extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()){
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");

        /*
        GET/SET WELCOME MESSAGE METHOD
         */
        if (SherlockBot.commandMap.get(21).checkCommand(event.getMessage())) {
            // GET CURRENT METHOD
            if(args.length <= 1){
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMethod()).queue();
            }

            // SET WELCOME METHOD
            if(args.length > 1){
                try{
                    int newWelcomeMethod = Integer.parseInt(args[1]);

                    SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeMethod(newWelcomeMethod);
                    if(database.putValue("WelcomeTable","WelcomeMethod","ChildGuildID",event.getGuild().getIdLong(),newWelcomeMethod) >= 1){
                        event.getMessage().addReaction("✅").queue();
                    }

                } catch(NumberFormatException e){
                    //todo Write error
                }
            }
        }

        /*
        GET/SET WELCOME CHANNEL ID
         */
        if (SherlockBot.commandMap.get(22).checkCommand(event.getMessage())) {
            // GET CURRENT METHOD
            if(args.length <= 1){
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeChannelID()).queue();
            }

            // SET WELCOME METHOD
            if(args.length > 1){
                if(event.getMessage().getMentionedChannels().size() > 0){
                    SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeChannelID(event.getMessage().getMentionedChannels().get(0).getIdLong());
                } else {

                    try {
                        SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeChannelID(Long.valueOf(args[1]));
                        if (database.putValue("WelcomeTable", "WelcomeChannelID", "ChildGuildID", event.getGuild().getIdLong(), Long.valueOf(args[1])) >= 1) {
                            event.getMessage().addReaction("✅").queue();
                        }

                    } catch (NumberFormatException e) {
                        //todo Write error
                    }
                }
            }
        }

        /*
        GET/SET WELCOME MESSAGE
         */
        if (SherlockBot.commandMap.get(23).checkCommand(event.getMessage())) {
            // GET CURRENT METHOD
            if(args.length <= 1){
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMessage()).queue();
            }

            // SET WELCOME METHOD
            if(args.length > 1){
                String welcomeMessage = event.getMessage().getContentDisplay().substring(args[0].length() + 1);

                    SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeMessage(welcomeMessage);
                    if(database.putValue("WelcomeTable","WelcomeMessage","ChildGuildID",event.getGuild().getIdLong(),welcomeMessage) >= 1){
                        event.getMessage().addReaction("✅").queue();
                    }

            }
        }

        /*
        GET/SET WELCOME MESSAGE TIMEOUT
         */
        if (SherlockBot.commandMap.get(24).checkCommand(event.getMessage())) {

            // GET CURRENT METHOD
            if(args.length <= 1){
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMessageTimeout()).queue();
            }

            // SET WELCOME METHOD
            if(args.length > 1){
                try {
                    SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeMessageTimeout(Integer.parseInt(args[1]));
                    if(database.putValue("WelcomeTable", "MessageTimeout", "ChildGuildID", event.getGuild().getIdLong(), Integer.parseInt(args[1])) >= 1){
                        event.getMessage().addReaction("✅").queue();
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
