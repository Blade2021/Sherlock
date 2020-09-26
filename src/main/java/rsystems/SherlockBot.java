package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rsystems.commands.Infraction;
import rsystems.commands.Leave;
import rsystems.events.JoinGuild;
import rsystems.events.LeaveGuild;
import rsystems.events.LocalChannelManager;
import rsystems.handlers.SQLHandler;
import rsystems.objects.Command;
import rsystems.objects.GuildSettings;
import rsystems.objects.InfractionObject;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SherlockBot {
    public static ArrayList<Command> commands = new ArrayList<>();
    public static Map<String,GuildSettings> guildMap = new HashMap<>();
    public static SQLHandler database = new SQLHandler(Config.get("Database_Host"),Config.get("Database_User"),Config.get("Database_Pass"));
    public static Long botID = null;

    public static void main(String[] args) throws LoginException {
        JDA api = JDABuilder.createDefault(Config.get("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();

        api.addEventListener(new LocalChannelManager());
        api.addEventListener(new JoinGuild());
        api.addEventListener(new Leave());
        api.addEventListener(new LeaveGuild());
        api.addEventListener(new Infraction());

        try{
            api.awaitReady();
            botID = api.getSelfUser().getIdLong();

            //Add each guild's log channel to the Map
            api.getGuilds().forEach(guild -> {

                //todo Replace with guild loader
                guildMap.put(guild.getId(),new GuildSettings("!",guild));
            });

        } catch(InterruptedException e){

        }

        loadCommands();

        ArrayList<InfractionObject> list;
        list = database.infractions("386701951662030858","649071079029080075");
        for(InfractionObject i:list){
            System.out.println(i.toString());
        }

    }

    private static void loadCommands(){
        commands.add(new Command("leave"));
        commands.add(new Command("infract"));
    }

}
