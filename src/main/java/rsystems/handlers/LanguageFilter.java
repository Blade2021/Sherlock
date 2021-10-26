package rsystems.handlers;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static rsystems.SherlockBot.database;

public class LanguageFilter extends ListenerAdapter {

    String badMessage = " Your message has been flagged due to inappropriate content [Blacklisted Word(s)].  Please edit or delete your message immediately or risk the message being deleted.  This action has been logged.";
    private Map<String, Future<?>> futures = new HashMap<>();

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String triggerWord = getBadWord(event.getGuild().getId(),event.getMessage().getContentDisplay());
        if(triggerWord != null){

            //Bad word was detected!
            try {
                event.getMessage().addReaction("⚠").queue();
                event.getMessage().reply(badMessage).queue(success -> {
                    success.delete().queueAfter(60, TimeUnit.SECONDS);
                });
                futures.put(event.getMessageId(), event.getMessage().delete().submitAfter(60, TimeUnit.SECONDS));
                database.insertInfraction(event.getGuild().getId(),event.getMember().getIdLong(),"Language Violation",event.getJDA().getSelfUser().getIdLong());

                LogChannel logChannel = new LogChannel();
                logChannel.logLanguageFilterAction(event,triggerWord);

            } catch (NullPointerException | PermissionException e) {

            }
        }
    }

    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (languageCheck(event.getGuild().getId(), event.getMessage().getContentDisplay())) {
            boolean futureFound = false;
            for(Map.Entry<String,Future<?>> entry:futures.entrySet()){
                String key = entry.getKey();
                if(key.equalsIgnoreCase(event.getMessageId())){
                    entry.getValue().cancel(true);
                    futureFound = true;
                }
            }
            if(!futureFound){
                futures.put(event.getMessageId(),event.getMessage().delete().submitAfter(30,TimeUnit.SECONDS));
                try {
                    event.getMessage().addReaction("⁉").queue();
                }catch(NullPointerException e){

                }
            }
        } else {
            try{
                String messageid = "";

                // Cancel the future
                for(Map.Entry<String,Future<?>> entry:futures.entrySet()){
                    String key = entry.getKey();
                    if(key.equalsIgnoreCase(event.getMessageId())){
                        entry.getValue().cancel(true);
                        System.out.println("Removing future for message: " + entry.getKey());
                        messageid = entry.getKey();
                    }
                }


                //Remove the entry from the HashMap
                Iterator it = futures.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry pair = (Map.Entry)it.next();
                    if(pair.getKey().toString().equalsIgnoreCase(messageid)){
                        it.remove();
                    }
                }

                event.getMessage().removeReaction("⚠").queue();
                event.getMessage().removeReaction("⁉").queue();

                //System.out.println(futures.size());
            } catch(NullPointerException ignored){}
        }
    }

    public boolean languageCheck(String guildID, String message) {
        for (String word : SherlockBot.guildMap.get(guildID).getBlacklistedWords()) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String getBadWord(String guildID, String message) {
        for (String word : SherlockBot.guildMap.get(guildID).getBlacklistedWords()) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
                return word;
            }
        }
        return null;
    }

}
