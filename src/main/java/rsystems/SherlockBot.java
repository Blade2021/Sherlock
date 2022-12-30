package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import rsystems.events.*;
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
    public static String version = "2.8.0";
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
        api.addEventListener(new GuildNewsMessageListener());

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

    public static void createQuarantineRole(final Long guildID){
        final Guild guild = SherlockBot.jda.getGuildById(guildID);

        guild.createRole().setColor(Color.decode("#9B1AF5")).setName("SL-Quarantine").queue(role -> {
            role.getManager().revokePermissions(role.getPermissions()).queue();

            ArrayList<Permission> mutePerms = new ArrayList<>();
            mutePerms.add(Permission.MESSAGE_SEND);
            mutePerms.add(Permission.MESSAGE_ADD_REACTION);
            mutePerms.add(Permission.VOICE_STREAM);
            mutePerms.add(Permission.VOICE_SPEAK);
            mutePerms.add(Permission.CREATE_INSTANT_INVITE);
            mutePerms.add(Permission.CREATE_PUBLIC_THREADS);
            mutePerms.add(Permission.MESSAGE_SEND_IN_THREADS);

            for (Category category : guild.getCategories()) {
                try {
                    category.upsertPermissionOverride(role).deny(mutePerms).reason("Initiating bot perms").queue();
                } catch (PermissionException e) {
                    break;
                }
            }

            //Set the mute role permission override for each channel
            for (TextChannel channel : guild.getTextChannels()) {
                try {
                    channel.upsertPermissionOverride(role).deny(mutePerms).queue();
                } catch (PermissionException e) {
                    break;
                }
            }

            for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
                try {
                    voiceChannel.upsertPermissionOverride(role).deny(mutePerms).queue();
                } catch (PermissionException e) {
                    break;
                }
            }


            SherlockBot.guildMap.get(guild.getIdLong()).setQuarantineRoleID(role.getIdLong());
            SherlockBot.guildMap.get(guild.getIdLong()).save();
        });

    }
}
