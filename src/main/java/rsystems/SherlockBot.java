package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rsystems.commands.Infraction;
import rsystems.commands.Leave;
import rsystems.commands.Mute;
import rsystems.events.JoinGuild;
import rsystems.events.LeaveGuild;
import rsystems.events.LocalChannelManager;
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
        commands.add(new Command("leave"));
        commands.add(new Command("infract"));
        commands.add(new Command("mute"));
        commands.add(new Command("unmute"));
    }

}
