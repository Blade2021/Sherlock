package rsystems;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import rsystems.events.*;
import rsystems.handlers.Dispatcher;
import rsystems.handlers.SQLHandler;
import rsystems.handlers.SlashCommandDispatcher;
import rsystems.objects.DBPool;
import rsystems.objects.GuildSettings;
import rsystems.objects.SlashCommand;
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
    public static SlashCommandDispatcher slashCommandDispatcher;

    public static Map<Long,GuildSettings> guildMap = new HashMap<>();
    public static Map<Long, Map<Long, ArrayList<UserRoleReactionObject>>> reactionHandleMap = new HashMap<>();
    public static User bot = null;
    public static String version = "0.6.2";
    public static JDA jda = null;
    public static Long botOwnerID = Long.valueOf(Config.get("OWNER_ID"));

    public static String defaultPrefix = "!sl";

    public static void main(String[] args) throws LoginException {
        JDA api = JDABuilder.createDefault(Config.get("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                //.setMemberCachePolicy(MemberCachePolicy.ALL)
                //.enableCache(CacheFlag.ACTIVITY)
                .setChunkingFilter(ChunkingFilter.NONE)
                .build();

        api.addEventListener(dispatcher = new Dispatcher());
        api.addEventListener(slashCommandDispatcher = new SlashCommandDispatcher());
        api.addEventListener(new GuildStateListener());
        api.addEventListener(new GuildNicknameListener());
        api.addEventListener(new GuildMemberEvents());
        //api.addEventListener(new SlashCommandEvents());
        api.addEventListener(new ButtonClickEvents());

        try{
            api.awaitReady();
            api.awaitStatus(JDA.Status.CONNECTED);

            jda = api;
            bot = api.getSelfUser();

            //Get the data for each guild from the database
            api.getGuilds().forEach(guild -> {

                if(guild.getId().equals("386701951662030858")){

                    guild.retrieveCommands().queue(list -> {

                        for(SlashCommand slashCommand: slashCommandDispatcher.getCommands()){
                            /*
                            for(Command c: list){
                                if(c.getName().equalsIgnoreCase(slashCommand.getName())){
                                    continue;
                                }
                            }

                             */
                            guild.upsertCommand(slashCommand.getCommandData()).queue();
                        }
                    });

                    /*guild.upsertCommand("hello","Say Hello World").queue();

                    CommandData guildSetting = new CommandData("gs", "Control guild settings for the bot");
                    SubcommandData logChannel = new SubcommandData("logchannel","Set a log channel for the bot");
                    logChannel.addOption(OptionType.CHANNEL,"channel","The channel to be set as the log channel of the bot",false);

                    guildSetting.addSubcommands(logChannel);

                    guild.upsertCommand(guildSetting).queue();

                    CommandData reasonCommandData = new CommandData("activity","Set a reason for a moderation action");
                    reasonCommandData.addOption(OptionType.INTEGER,"id","The ID # of the case event");
                    reasonCommandData.addOption(OptionType.STRING,"reason","The reason to set");

                    guild.upsertCommand(reasonCommandData).queue();


                     */
                    guild.retrieveCommands().queue(list -> {
                        for(Command command:list){
                            System.out.println(String.format("CMD: %s  | ID: %s",command.getName(),command.getId()));
                        }
                    });
                }

                try {
                    guildMap.put(guild.getIdLong(),SherlockBot.database.getGuildData(guild.getIdLong()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            });

            System.out.println("Commands Loaded: " + SherlockBot.dispatcher.getCommands().size());
            System.out.println("Commands Loaded: " + SherlockBot.slashCommandDispatcher.getCommands().size());


        } catch(InterruptedException e){

        }

        Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new ThreeMinute(), 60*1000,60*1000);

    }

    public static GuildSettings getGuildSettings(final Long guildID){
        return guildMap.get(guildID);
    }

}
