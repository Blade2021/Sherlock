package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rsystems.events.*;
import rsystems.handlers.Dispatcher;
import rsystems.handlers.SQLHandler;
import rsystems.objects.DBPool;
import rsystems.objects.GuildSettings;
import rsystems.objects.UserRoleReactionObject;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class SherlockBot {

    public static DBPool dbPool = new DBPool(Config.get("DATABASE_HOST"), Config.get("DATABASE_USER"),Config.get("DATABASE_PASS"));
    public static SQLHandler database = new SQLHandler(dbPool.getPool());
    public static Dispatcher dispatcher;

    public static Map<Long,GuildSettings> guildMap = new HashMap<>();
    public static Map<Long, Map<Long, ArrayList<UserRoleReactionObject>>> reactionHandleMap = new HashMap<>();
    //public static SQLHandler database = new SQLHandler(Config.get("Database_Host"),Config.get("Database_User"),Config.get("Database_Pass"));
    public static User bot = null;
    public static String version = "0.6.2";
    public static JDA jda = null;
    public static Long botOwnerID = Long.valueOf(Config.get("OWNER_ID"));

    public static String defaultPrefix = "!sl";

    public static void main(String[] args) throws LoginException {
        JDA api = JDABuilder.createDefault(Config.get("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();

        //api.addEventListener(new GuildMessageReceived());
        //api.addEventListener(new JoinGuild());
        api.addEventListener(dispatcher = new Dispatcher());
        //api.addEventListener(new GuildBanEventListener());
        api.addEventListener(new GuildChannelEventListener());
        api.addEventListener(new GuildStateListener());
        api.addEventListener(new GuildNicknameListener());
        api.addEventListener(new GuildMemberEvents());

        try{
            api.awaitReady();
            api.awaitStatus(JDA.Status.CONNECTED);

            jda = api;
            bot = api.getSelfUser();

            //Get the data for each guild from the database
            api.getGuilds().forEach(guild -> {

                try {
                    guildMap.put(guild.getIdLong(),SherlockBot.database.getGuildData(guild.getIdLong()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            });



        } catch(InterruptedException e){
            //do nothing
        }

        Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new ThreeMinute(), 60*1000,60*1000);

    }

    public static GuildSettings getGuildSettings(final Long guildID){
        return guildMap.get(guildID);
    }

}
