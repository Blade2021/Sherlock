package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rsystems.commands.*;
import rsystems.events.*;
import rsystems.handlers.CommandLoader;
import rsystems.handlers.LanguageFilter;
import rsystems.handlers.SQLHandler;
import rsystems.objects.Command;
import rsystems.objects.GuildSettings;
import rsystems.threads.ThreeMinute;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class SherlockBot {
    public static Map<Integer, Command> commandMap = new HashMap<>();
    public static Map<String,GuildSettings> guildMap = new HashMap<>();
    public static SQLHandler database = new SQLHandler(Config.get("Database_Host"),Config.get("Database_User"),Config.get("Database_Pass"));
    public static User bot = null;
    public static String version = "0.4.6";

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
        api.addEventListener(new Mute());
        api.addEventListener(new SelfRoles());
        api.addEventListener(new ModifyGuildSettings());
        api.addEventListener(new PrivateMessageReceived());
        api.addEventListener(new GuildRoleDeleted());
        api.addEventListener(new LanguageFilter());
        api.addEventListener(new EmbedMessageListener());
        api.addEventListener(new ChannelCooldown());
        api.addEventListener(new Generics());
        api.addEventListener(new WelcomeSettings());
        api.addEventListener(new GuildMemberJoin());
        api.addEventListener(new ModCommands());
        api.addEventListener(new GuildMessageDeleted());
        api.addEventListener(new ArchiveChannel());
        api.addEventListener(new GuildChannelMoveEvent());
        api.addEventListener(new GuildCategoryDeleted());
        api.addEventListener(new GuildTextChannelDeleted());
        api.addEventListener(new ChannelTopic());

        try{
            api.awaitReady();
            bot = api.getSelfUser();

            //Get the data for each guild from the database
            api.getGuilds().forEach(guild -> {
                guildMap.put(guild.getId(),new GuildSettings("!"));
                database.loadGuildData(guild.getId());
            });

        } catch(InterruptedException e){
            //do nothing
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ThreeMinute(), 60*1000,60*1000);

        CommandLoader commandLoader = new CommandLoader();

    }

}
