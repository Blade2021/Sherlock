package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import rsystems.events.ButtonClickEvents;
import rsystems.events.GuildMemberEvents;
import rsystems.events.GuildNicknameListener;
import rsystems.events.GuildStateListener;
import rsystems.handlers.Dispatcher;
import rsystems.handlers.Overseer;
import rsystems.handlers.SQLHandler;
import rsystems.handlers.SlashCommandDispatcher;
import rsystems.objects.DBPool;
import rsystems.objects.GuildSettings;
import rsystems.objects.UserRoleReactionObject;
import rsystems.threads.ExpiredTrackersCheck;
import rsystems.threads.OneMinute;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class SherlockBot {

    public static DBPool dbPool = new DBPool(Config.get("DATABASE_HOST"), Config.get("DATABASE_USER"), Config.get("DATABASE_PASS"));
    public static SQLHandler database = new SQLHandler(dbPool.getPool());
    public static Dispatcher dispatcher;
    public static SlashCommandDispatcher slashCommandDispatcher;

    public static Map<Long, GuildSettings> guildMap = new HashMap<>();
    public static Map<Long, Map<Long, ArrayList<UserRoleReactionObject>>> reactionHandleMap = new HashMap<>();
    public static User bot = null;
    public static String version = "2.6.2";
    public static JDA jda = null;
    public static Long botOwnerID = Long.valueOf(Config.get("OWNER_ID"));
    public static Overseer overseer = new Overseer();
    public static int activityIndex = 0;

    private static Map<colorType, String> colorMap = new HashMap<>();

    public static String defaultPrefix = Config.get("defaultPrefix");

    public static void main(String[] args) throws LoginException {
        JDA api = JDABuilder.createDefault(Config.get("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                //.enableCache(CacheFlag.ACTIVITY)
                .setChunkingFilter(ChunkingFilter.NONE)
                .build();

        api.addEventListener(dispatcher = new Dispatcher());
        api.addEventListener(slashCommandDispatcher = new SlashCommandDispatcher());
        api.addEventListener(new GuildStateListener());
        api.addEventListener(new GuildMemberEvents());
        api.addEventListener(new ButtonClickEvents());
        api.addEventListener(new GuildNicknameListener());

        loadColorMap();

        try {
            api.awaitReady();
            api.getPresence().setActivity(Activity.playing("Starting up..."));
            api.awaitStatus(JDA.Status.CONNECTED);

            jda = api;
            bot = api.getSelfUser();

            slashCommandDispatcher.submitCommands(api);


            //Get the data for each guild from the database
            api.getGuilds().forEach(guild -> {

                //SherlockBot.slashCommandDispatcher.submitCommands(guild.getIdLong());

                try {
                    guildMap.put(guild.getIdLong(), SherlockBot.database.getGuildData(guild.getIdLong()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            });

            System.out.println("Commands Loaded: " + SherlockBot.dispatcher.getCommands().size());
            System.out.println("Commands Loaded: " + SherlockBot.slashCommandDispatcher.getCommands().size());


        } catch (InterruptedException e) {

        }

        Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new ThreeMinute(), 60*1000,60*1000);

        //One-Minute Timer
        timer.scheduleAtFixedRate(new OneMinute(), 30 * 1000, 60 * 1000);

        //Three-Minute Timer
        timer.scheduleAtFixedRate(new ExpiredTrackersCheck(), 60 * 1000, 180 * 1000);

    }

    public static GuildSettings getGuildSettings(final Long guildID) {
        return guildMap.get(guildID);
    }

    public static Color getColor(colorType colorType) {
        if (colorMap.containsKey(colorType)) {
            return Color.decode(colorMap.get(colorType));
        } else {
            return Color.decode(colorMap.get(SherlockBot.colorType.GENERIC));
        }
    }

    private static void loadColorMap() {
        colorMap.putIfAbsent(colorType.WARNING, "#F5741A");
        colorMap.putIfAbsent(colorType.QUARANTINE, "#AF1AF5");
        colorMap.putIfAbsent(colorType.GENERIC, "#1ABDF5");
        colorMap.putIfAbsent(colorType.ERROR, "#EC0000");
    }

    public enum colorType {
        WARNING, QUARANTINE, GENERIC, ERROR
    }
}
