package rsystems.threads;

import net.dv8tion.jda.api.JDA;
import rsystems.objects.TimedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TimerTask;

import static rsystems.SherlockBot.bot;
import static rsystems.SherlockBot.database;

public class ThreeMinute extends TimerTask {

    @Override
    public void run(){
        ArrayList<TimedEvent> events = new ArrayList<>(database.timedEventList());
        for(TimedEvent timedEvent:events){
            if(timedEvent.eventExpiration.isBefore(LocalDateTime.now())){
                System.out.println(String.format("Expiring timed event: EventID:%d | GuildID:%d | EventType:%d",timedEvent.eventUserID,timedEvent.eventGuildID,timedEvent.eventType));
                switch(timedEvent.eventType){
                    case 1:
                        unmuteUser(timedEvent.eventGuildID,timedEvent.eventUserID);
                        break;
                    case 2:
                        //unmute channel
                        break;
                    case 3:
                        //unban user
                        break;
                }
            }
        }
    }

    private void unmuteUser(Long guildID, Long userID){
        Long muteRoleID = database.getLong("GuildTable","MuteRoleID","GuildID",guildID);

        try{
            JDA jda = bot.getJDA();
            jda.getGuildById(guildID).removeRoleFromMember(userID,jda.getGuildById(guildID).getRoleById(muteRoleID)).queue();
            database.expireTimedEvent(guildID,userID);
        } catch(NullPointerException e){
            System.out.println("Could not remove mute role from user for Guild:" + guildID);
        }
    }
}
