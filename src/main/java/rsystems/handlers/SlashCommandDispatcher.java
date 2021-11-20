package rsystems.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.commands.slashCommands.Ban;
import rsystems.commands.slashCommands.Commands;
import rsystems.objects.Command;
import rsystems.objects.SlashCommand;
import rsystems.commands.slashCommands.Apple;
import rsystems.commands.slashCommands.CopyChannel;

import java.awt.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class SlashCommandDispatcher extends ListenerAdapter {

    private final Set<SlashCommand> slashCommands = ConcurrentHashMap.newKeySet();
    private final ExecutorService pool = Executors.newCachedThreadPool(newThreadFactory("slashCommand-runner", false));
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(10);

    public SlashCommandDispatcher() {

        registerCommand(new Apple());
        registerCommand(new CopyChannel());
        registerCommand(new Commands());
        registerCommand(new Ban());
    }

    public Set<SlashCommand> getCommands() {
        return Collections.unmodifiableSet(new HashSet<>(this.slashCommands));
    }

    public void onSlashCommand(final SlashCommandEvent event) {

        for (final SlashCommand c : this.getCommands()) {
            if (event.getName().equalsIgnoreCase(c.getName())) {
                this.executeCommand(c, event.getCommandString(), event);
                return;
            }
        }
    }


    public boolean registerCommand(final SlashCommand command) {
        if (command.getName().contains(" "))
            throw new IllegalArgumentException("Name must not have spaces!");
        if (this.slashCommands.stream().map(SlashCommand::getName).anyMatch(c -> command.getName().equalsIgnoreCase(c)))
            return false;
        this.slashCommands.add(command);
        return true;
    }

    private void executeCommand(final SlashCommand c, final String message,
                                final SlashCommandEvent event) {
        this.pool.submit(() ->
        {

            boolean authorized = false;
            if ((c.getPermissionIndex() == null) && (c.getDiscordPermission() == null)) {
                authorized = true;
            } else {
                try {
                    authorized = isAuthorized(c, event.getGuild().getIdLong(), event.getMember(), c.getPermissionIndex());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (authorized) {
                try {
                    final String content = message;
                    c.dispatch(event.getUser(), event.getChannel(), content, event);

                    //database.logCommandUsage(c.getName());
                } catch (final NumberFormatException numberFormatException) {
                    numberFormatException.printStackTrace();
                    //event.getMessage().reply("**ERROR:** Bad format received").queue();
                    //messageOwner(event, c, numberFormatException);
                } catch (final Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("**There was an error processing your command!**").queue();
                    //event.getChannel().sendMessage("**There was an error processing your command!**").queue();
                    //messageOwner(event, c, e);
                }
            } else {

                StringBuilder errorString = new StringBuilder();

                if (c.getPermissionIndex() != null) {
                    errorString.append("Permission Index: ").append(c.getPermissionIndex()).append("\n");
                }

                if (c.getDiscordPermission() != null) {
                    errorString.append("Discord Permission: ").append(c.getDiscordPermission().getName());
                }

                event.reply(String.format(" You are not authorized for command: `%s`\n%s", c.getName(), errorString)).setEphemeral(c.isEphemeral()).queue();
            }
        });
    }


    public static ThreadFactory newThreadFactory(String threadName, boolean isdaemon) {
        return (r) ->
        {
            Thread t = new Thread(r, threadName);
            t.setDaemon(isdaemon);
            return t;
        };
    }


    public static Boolean isAuthorized(final SlashCommand c, final Long guildID, final Member member, final Integer permissionIndex) throws SQLException {
        boolean authorized = false;

        if(member.hasPermission(Permission.ADMINISTRATOR)){
            return true;
        }

        if(c.getDiscordPermission() != null){
            if(member.getPermissions().contains(c.getDiscordPermission())){
                return true;
            }
        }

        if ((c.getDiscordPermission() == null) && (c.getPermissionIndex() == null)) {
            return true;
        }


        Map<Long, Integer> authmap = SherlockBot.database.getModRoles(guildID);
        for (Role role : member.getRoles()) {

            Long roleID = role.getIdLong();

            if (authmap.containsKey(roleID)) {
                int modRoleValue = authmap.get(roleID);

                /*
                Form a binary string based on the permission level integer found.
                Example: 24 = 11000
                 */
                String binaryString = Integer.toBinaryString(modRoleValue);

                //Reverse the string for processing
                //Example 24 = 11000 -> 00011
                String reverseString = new StringBuilder(binaryString).reverse().toString();

                //Turn the command rank into a binary string
                //Example 8 = 1000
                String binaryIndexString = Integer.toBinaryString(permissionIndex);

                //Reverse the string for lookup
                //Example 8 = 1000 -> 0001
                String reverseLookupString = new StringBuilder(binaryIndexString).reverse().toString();

                int realIndex = reverseLookupString.indexOf('1');

                char indexChar = '0';
                try {

                    indexChar = reverseString.charAt(realIndex);

                } catch (IndexOutOfBoundsException e) {

                } finally {
                    if (indexChar == '1') {
                        authorized = true;
                    }
                }

                if (authorized)
                    break;
            }
        }

        return authorized;
    }

    private void messageOwner(final GuildMessageReceivedEvent event, final Command c, final Exception exception){

        SherlockBot.jda.getUserById(SherlockBot.botOwnerID).openPrivateChannel().queue((channel) -> {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("System Exception Encountered")
                    .setColor(Color.RED)
                    .addField("Command:",c.getName(),true)
                    .addField("Calling User:",event.getMessage().getAuthor().getAsTag(),true)
                    .addBlankField(true)
                    .addField("Exception:",exception.toString(),false)
                    .setDescription(exception.getCause().getMessage().substring(0,exception.getCause().getMessage().indexOf(":")));

            channel.sendMessageEmbeds(embedBuilder.build()).queue();

            embedBuilder.clear();
        });
    }

}
