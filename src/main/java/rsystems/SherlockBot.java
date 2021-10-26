package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rsystems.events.GuildMessageEvent;
import rsystems.events.JoinGuild;
import rsystems.handlers.CommandLoader;
import rsystems.handlers.SQLHandler;
import rsystems.objects.Command_OLD;
import rsystems.objects.DBPool;
import rsystems.objects.GuildSettings;
import rsystems.objects.UserRoleReactionObject;
import rsystems.threads.ThreeMinute;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class SherlockBot {

    public static DBPool dbPool = new DBPool(Config.get("DATABASE_HOST"), Config.get("DATABASE_USER"),Config.get("DATABASE_PASS"));
    public static SQLHandler database = new SQLHandler(dbPool.getPool());


    public static Map<Integer, Command_OLD> commandMap = new HashMap<>();
    public static Map<Long,GuildSettings> guildMap = new HashMap<>();
    public static Map<Long, Map<Long, ArrayList<UserRoleReactionObject>>> reactionHandleMap = new HashMap<>();
    //public static SQLHandler database = new SQLHandler(Config.get("Database_Host"),Config.get("Database_User"),Config.get("Database_Pass"));
    public static User bot = null;
    public static String version = "0.6.2";
    public static JDA jda = null;

    public static String defaultPrefix = "!sl";

    public static void main(String[] args) throws LoginException {
        JDA api = JDABuilder.createDefault(Config.get("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();

        api.addEventListener(new GuildMessageEvent());
        api.addEventListener(new JoinGuild());

        try{
            CommandLoader commandLoader = new CommandLoader();

            api.awaitReady();

            jda = api;
            bot = api.getSelfUser();

            //Get the data for each guild from the database
            api.getGuilds().forEach(guild -> {

                guildMap.put(guild.getIdLong(),SherlockBot.database.getGuildData(guild.getIdLong()));

            });

        } catch(InterruptedException e){
            //do nothing
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ThreeMinute(), 60*1000,60*1000);

    }

}
