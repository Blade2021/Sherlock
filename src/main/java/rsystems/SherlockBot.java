package rsystems;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rsystems.commands.Leave;
import rsystems.events.ChannelManager;
import rsystems.events.JoinGuild;
import rsystems.objects.Command;
import rsystems.objects.GuildSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SherlockBot {
    public static ArrayList<Command> commands = new ArrayList<>();
    public static Map<String,GuildSettings> guildMap = new HashMap<>();


    public static void main(String[] args) throws LoginException {
        JDA api = JDABuilder.createDefault(Config.get("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();

        api.addEventListener(new ChannelManager());
        api.addEventListener(new JoinGuild());
        api.addEventListener(new Leave());

        try{
            api.awaitReady();

            //Add each guild's log channel to the arraylist
            api.getGuilds().forEach(guild -> {

                //todo Replace with guild loader
                guildMap.put(guild.getId(),new GuildSettings("!",guild));
            });

        } catch(InterruptedException e){

        }

        loadCommands();

    }


    private static void loadCommands(){
        commands.add(new Command("leave"));
    }

}
