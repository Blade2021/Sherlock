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
import rsystems.handlers.LanguageFilter;
import rsystems.handlers.SQLHandler;
import rsystems.objects.Command;
import rsystems.objects.GuildSettings;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SherlockBot {
    public static ArrayList<Command> commands = new ArrayList<>();
    public static Map<String,GuildSettings> guildMap = new HashMap<>();
    public static SQLHandler database = new SQLHandler(Config.get("Database_Host"),Config.get("Database_User"),Config.get("Database_Pass"));
    public static User bot = null;
    public static String version = "0.1.7";

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

        try{
            api.awaitReady();
            bot = api.getSelfUser();

            //Add each guild's log channel to the Map
            api.getGuilds().forEach(guild -> {
                guildMap.put(guild.getId(),new GuildSettings("!"));
                database.loadGuildData(guild.getId());
            });

        } catch(InterruptedException e){

        }

        loadCommands();

    }

    private static void loadCommands(){
        commands.add(new Command("leave")); // 0
        commands.add(new Command("infract")); // 1
        commands.add(new Command("mute")); // 2
        commands.add(new Command("unmute")); // 3
        commands.add(new Command("removeSelfRole")); // 4
        commands.add(new Command("addSelfRole")); // 5
        commands.add(new Command("logChannel")); // 6
        commands.add(new Command("commands")); // 7
        commands.add(new Command("setPrefix")); // 8
        commands.add(new Command("getAroles")); // 9
        commands.add(new Command("lfadd")); // 10
        commands.add(new Command("lfremove")); // 11
        commands.add(new Command("lflist")); // 12
        commands.add(new Command("cooldown")); // 13
        commands.add(new Command("embedFilter")); // 14
        commands.add(new Command("info")); // 15
        commands.add(new Command("ginfo")); // 16
        commands.add(new Command("settings")); // 17
        commands.add(new Command("lookup")); // 18
        commands.add(new Command("check")); // 19
        commands.add(new Command("placeholder")); // 20
        commands.add(new Command("welcomeMethod")); // 21
        commands.add(new Command("welcomeChannelID")); // 22
        commands.add(new Command("welcomeMessage")); // 23
        commands.add(new Command("welcomeTimeout")); // 24
        commands.add(new Command("addModRole")); // 25
        commands.add(new Command("removeModRole")); // 26
        commands.add(new Command("updateModRole")); // 27
        commands.add(new Command("getModRoles")); // 28
        commands.add(new Command("addException")); // 29
        commands.add(new Command("removeException")); // 30
        commands.add(new Command("getExceptions")); // 31
        commands.add(new Command("placeholder")); // 32
        commands.add(new Command("placeholder")); // 33
        commands.add(new Command("placeholder")); // 34
        commands.add(new Command("placeholder")); // 35
        commands.add(new Command("placeholder")); // 36
        commands.add(new Command("placeholder")); // 37


    }

}
