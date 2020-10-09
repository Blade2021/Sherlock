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
        api.addEventListener(new AssignableRoles());
        api.addEventListener(new ModifyGuildSettings());
        api.addEventListener(new PrivateMessageReceived());
        api.addEventListener(new GuildRoleDeleted());
        api.addEventListener(new LanguageFilter());
        api.addEventListener(new EmbedMessageListener());
        api.addEventListener(new ChannelCooldown());

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
        commands.add(new Command("resign")); // 4
        commands.add(new Command("assign")); // 5
        commands.add(new Command("logChannel")); // 6
        commands.add(new Command("SPARE")); // 7
        commands.add(new Command("setPrefix")); // 8
        commands.add(new Command("getAroles")); // 9
        commands.add(new Command("lfadd")); // 10
        commands.add(new Command("lfremove")); // 11
        commands.add(new Command("lflist")); // 12
        commands.add(new Command("cooldown")); // 13
        commands.add(new Command("embedFilter")); // 14
        commands.add(new Command("placeholder")); // 15
        commands.add(new Command("placeholder")); // 16
        commands.add(new Command("placeholder")); // 17
        commands.add(new Command("placeholder")); // 18
        commands.add(new Command("placeholder")); // 19
        commands.add(new Command("placeholder")); // 20
    }

}
