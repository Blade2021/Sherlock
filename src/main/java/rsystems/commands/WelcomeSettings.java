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
        if (SherlockBot.commands.get(20).checkCommandMod(event.getMessage())) {
            // GET CURRENT METHOD
            if(args.length <= 1){
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMethod()).queue();
            }

            // SET WELCOME METHOD
            if(args.length > 1){
                try{
                    int newWelcomeMethod = Integer.parseInt(args[1]);

                    SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeMethod(newWelcomeMethod);
                    database.putValue("WelcomeTable","WelcomeMethod","ChildGuildID",event.getGuild().getIdLong(),newWelcomeMethod);

                } catch(NumberFormatException e){
                    //todo Write error
                }
            }
        }

        /*
        GET/SET WELCOME CHANNEL ID
         */
        if (SherlockBot.commands.get(20).checkCommandMod(event.getMessage())) {
            // GET CURRENT METHOD
            if(args.length <= 1){
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeChannelID()).queue();
            }

            // SET WELCOME METHOD
            if(args.length > 1){
                try{
                    SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeChannelID(Long.valueOf(args[1]));
                    database.putValue("WelcomeTable","WelcomeChannelID","ChildGuildID",event.getGuild().getIdLong(),Long.valueOf(args[1]));

                } catch(NumberFormatException e){
                    //todo Write error
                }
            }
        }

        /*
        GET/SET WELCOME MESSAGE
         */
        if (SherlockBot.commands.get(20).checkCommandMod(event.getMessage())) {
            // GET CURRENT METHOD
            if(args.length <= 1){
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + SherlockBot.guildMap.get(event.getGuild().getId()).getWelcomeMessage()).queue();
            }

            // SET WELCOME METHOD
            if(args.length > 1){
                String welcomeMessage = event.getMessage().getContentDisplay().substring(args[0].length() + 1);


                    SherlockBot.guildMap.get(event.getGuild().getId()).setWelcomeMessage(welcomeMessage);
                    database.putValue("WelcomeTable","WelcomeChannelID","ChildGuildID",event.getGuild().getIdLong(),welcomeMessage);


            }
        }
    }
}
