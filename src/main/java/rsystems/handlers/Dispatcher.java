package rsystems.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.commands.botManager.ForceDisconnect;
import rsystems.commands.botManager.Test;
import rsystems.commands.guildFunctions.*;
import rsystems.commands.modCommands.*;
import rsystems.commands.publicCommands.Help;
import rsystems.commands.publicCommands.Info;
import rsystems.commands.subscriberOnly.ColorRole;
import rsystems.commands.subscriberOnly.CopyChannel;
import rsystems.objects.Command;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

public class Dispatcher extends ListenerAdapter {

    private final Set<Command> commands = ConcurrentHashMap.newKeySet();
    private final ExecutorService pool = Executors.newCachedThreadPool(newThreadFactory("command-runner", false));
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(10);

    public Dispatcher() {

        registerCommand(new SelfRole());
        registerCommand(new AutoRole());
        registerCommand(new GiveRole());
        registerCommand(new TakeRole());
        registerCommand(new SoftBan());
        registerCommand(new Infraction());
        registerCommand(new Reason());
        registerCommand(new Test());
        registerCommand(new GuildSetting());
        registerCommand(new CopyChannel());
        registerCommand(new IgnoreChannel());
        registerCommand(new WatchChannel());
        registerCommand(new ColorRole());
        registerCommand(new Help());
        registerCommand(new ForceDisconnect());
        registerCommand(new Leave());
        registerCommand(new Unban());
        registerCommand(new Info());

    }

    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(new HashSet<>(this.commands));
    }

    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        final String defaultPrefix = Config.get("defaultPrefix");
        final String message = event.getMessage().getContentRaw();
        final String guildPrefix = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getPrefix();
        final boolean defaultPrefixFound = message.toLowerCase().startsWith(SherlockBot.defaultPrefix.toLowerCase());

        if ((defaultPrefixFound) || ((guildPrefix != null) && (message.toLowerCase().startsWith(guildPrefix.toLowerCase())))) {
            //PREFIX FOUND

            String prefix;
            if (defaultPrefixFound) {
                prefix = defaultPrefix;
            } else {
                prefix = guildPrefix;
            }

            try {
                if(SherlockBot.database.getLong("IgnoreChannelTable","ChannelID","ChildGuildID",event.getGuild().getIdLong(),"ChannelID",event.getChannel().getIdLong()) == null) {

                    for (final Command c : this.getCommands()) {
                        if (message.toLowerCase().startsWith(prefix.toLowerCase() + c.getName().toLowerCase() + ' ') || message.equalsIgnoreCase(prefix + c.getName())) {
                            this.executeCommand(c, c.getName(), prefix, message, event);
                            return;
                        } else {
                            for (final String alias : c.getAliases()) {
                                if (message.toLowerCase().startsWith(prefix.toLowerCase() + alias.toLowerCase() + ' ') || message.equalsIgnoreCase(prefix + alias)) {
                                    this.executeCommand(c, alias, prefix, message, event);
                                    return;
                                }
                            }
                        }
                    }

                    //SELF ROLES
                    if (SherlockBot.database.getTableCount(event.getGuild().getIdLong(), "SelfRoles") > 0) {

                        Map<String, Long> guildSelfRoleMap = SherlockBot.database.getGuildSelfRoles(event.getGuild().getIdLong());

                        //ITERATE THROUGH GUILD SELF ROLE MAP
                        for (Map.Entry<String, Long> entry : guildSelfRoleMap.entrySet()) {

                            if (entry.getKey().equalsIgnoreCase(message.substring(prefix.length()))) {
                                //ENTRY FOUND
                                Long roleID = entry.getValue();
                                handleSelfRoleEvent(event, roleID);
                                return;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {

            executeCommand(message,event);
        }
    }

    @Override
    public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        final Long authorID = event.getAuthor().getIdLong();

        final String prefix = Config.get("defaultPrefix");
        String message = event.getMessage().getContentRaw();

        final MessageChannel channel = event.getChannel();

        if (message.toLowerCase().startsWith(prefix.toLowerCase())) {
            for (final Command c : this.getCommands()) {
                if (message.toLowerCase().startsWith(prefix.toLowerCase() + c.getName().toLowerCase() + ' ') || message.equalsIgnoreCase(prefix + c.getName())) {
                    this.executeCommand(c, c.getName(), prefix, message, event);
                    return;
                } else {
                    for (final String alias : c.getAliases()) {
                        if (message.toLowerCase().startsWith(prefix.toLowerCase() + alias.toLowerCase() + ' ') || message.equalsIgnoreCase(prefix + alias)) {
                            this.executeCommand(c, alias, prefix, message, event);
                            return;
                        }
                    }
                }
            }
        }
    }


    public boolean registerCommand(final Command command) {
        if (command.getName().contains(" "))
            throw new IllegalArgumentException("Name must not have spaces!");
        if (this.commands.stream().map(Command::getName).anyMatch(c -> command.getName().equalsIgnoreCase(c)))
            return false;
        this.commands.add(command);
        return true;
    }

    private void executeCommand(final Command c, final String alias, final String prefix, final String message,
                                final GuildMessageReceivedEvent event) {
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
                    final String content = this.removePrefix(alias, prefix, message);
                    c.dispatch(event.getAuthor(), event.getChannel(), event.getMessage(), content, event);

                    //database.logCommandUsage(c.getName());
                } catch (final NumberFormatException numberFormatException) {
                    numberFormatException.printStackTrace();
                    event.getMessage().reply("**ERROR:** Bad format received").queue();
                    messageOwner(event, c, numberFormatException);
                } catch (final Exception e) {
                    e.printStackTrace();
                    event.getChannel().sendMessage("**There was an error processing your command!**").queue();
                    messageOwner(event,c,e);
                }
            } else {

                StringBuilder errorString = new StringBuilder();

                if(c.getPermissionIndex() != null){
                    errorString.append("Permission Index: ").append(c.getPermissionIndex()).append("\n");
                }

                if(c.getDiscordPermission() != null){
                    errorString.append("Discord Permission: ").append(c.getDiscordPermission().getName());
                }

                event.getMessage().reply(String.format(event.getAuthor().getAsMention() + " You are not authorized for command: `%s`\n%s", c.getName(), errorString)).queue();
            }
        });
    }

    private void executeCommand(final Command c, final String alias, final String prefix, final String message,
                                final PrivateMessageReceivedEvent event) {
        this.pool.submit(() ->
        {
            boolean authorized = false;

            if(c.getPermissionIndex() == null){
                authorized = true;
            } else {
                authorized = false;
            }

            if (authorized) {

                try {
                    final String content = this.removePrefix(alias, prefix, message);
                    c.dispatch(event.getAuthor(), event.getChannel(), event.getMessage(), content, event);

                    //HiveBot.sqlHandler.logCommandUsage(c.getName());
                } catch (final NumberFormatException numberFormatException) {
                    numberFormatException.printStackTrace();
                    //event.getMessage().reply("**ERROR:** Bad format received").queue();
                    //messageOwner(numberFormatException, c, event);
                } catch (final Exception e) {
                    e.printStackTrace();
                    event.getChannel().sendMessage("**There was an error processing your command!**").queue();
                    //messageOwner(e, c, event);
                }
            } else {
                event.getMessage().reply(String.format(event.getAuthor().getAsMention() + " You are not authorized for command: `%s`\nPermission Index: %d", c.getName(), c.getPermissionIndex())).queue();
            }
        });
    }

    private void executeCommand(final String message,
                                final GuildMessageReceivedEvent event) {
        this.pool.submit(() ->
        {

            //Check for Guild invites
            if (event.getMessage().getInvites().size() > 0) {
                try {
                    List<Long> whiteListedGuilds = SherlockBot.database.getLongMultiple("InviteWhitelist", "TargetGuildID", "ChildGuildID", event.getGuild().getIdLong());

                    for(String code:event.getMessage().getInvites()){
                        Invite.resolve(SherlockBot.jda,code).queue(resolvedInvite -> {
                            Long targetGuildID = resolvedInvite.getGuild().getIdLong();
                            if((event.getGuild().getIdLong() == targetGuildID) || (whiteListedGuilds.contains(targetGuildID))){
                                // ID is ok
                            } else {
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setTitle("Discord Link Detection");
                                builder.setDescription("User posted unauthorized discord link:\n" + resolvedInvite.getUrl());
                                builder.setColor(Color.yellow);
                                if(event.getMessage().getMember().getAsMention() != null){
                                    builder.addField("User:",String.format(event.getMessage().getMember().getAsMention() + "\n%s\n%s",event.getAuthor().getAsTag(),event.getAuthor().getId()),true);
                                } else {
                                    builder.addField("User:", String.format("n%s\n%s", event.getAuthor().getAsTag(), event.getAuthor().getId()), true);
                                }
                                builder.addField("Target Guild:",String.format("%s\n%d",resolvedInvite.getGuild().getName(),resolvedInvite.getGuild().getIdLong()),true);
                                builder.setTimestamp(Instant.now());

                                LogMessage.sendLogMessage(event.getGuild().getIdLong(),builder.build());

                                builder.clear();

                                event.getMessage().delete().reason("User posted discord invite link").queue(DeleteSuccess -> {

                                    EmbedBuilder embedBuilder = new EmbedBuilder();
                                    embedBuilder.setDescription("Sorry, Only authorized Discord servers can have invite links posted here.  Please refrain from posting any other invite links as an automatic punishment will take place.");
                                    embedBuilder.setColor(Color.yellow);
                                    embedBuilder.setFooter("This action has been logged");
                                    event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                                    embedBuilder.clear();
                                });
                            }
                        });
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return;
            }


            if(event.getMessage().getType().equals(MessageType.GUILD_MEMBER_JOIN)){
                return;
            }

            // SPAM MONITORING
            event.getChannel().getHistoryBefore(event.getMessage(), 12).queue(messageHistory -> {

                ArrayList<String> messages = new ArrayList<>();
                for (Message m : messageHistory.getRetrievedHistory()) {

                    if (m.getAuthor().isBot()) {
                        continue;
                    }

                    if (m.getContentRaw().equalsIgnoreCase(event.getMessage().getContentRaw())) {
                        if (m.getAuthor().getIdLong() == event.getAuthor().getIdLong()) {
                            messages.add(m.getId());
                        }
                    }

                    //SPAM DETECTED
                    if (messages.size() >= 3) {
                        messages.add(event.getMessageId());
                        event.getChannel().purgeMessagesById(messages);

                        Role muteRole = event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getIdLong()).getMuteRoleID());
                        if(muteRole != null) {
                            event.getGuild().addRoleToMember(event.getMember(),muteRole).reason("Spam Detected").queue(Success -> {
                                try {
                                    event.getGuild().removeRoleFromMember(event.getMember(), muteRole).reason("Mute Expiration").queueAfter(60, TimeUnit.SECONDS,null,failure -> {
                                        System.out.println("Couldn't unmute user");
                                    },scheduledExecutorService);
                                } catch(ErrorResponseException e){
                                    // do nothing
                                }
                            });
                        }

                        // Log message to log channel
                        if (SherlockBot.guildMap.get(event.getGuild().getIdLong()).getLogChannelID() != null) {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTitle("Spam Detection - " + event.getAuthor().getAsTag())
                                    .setDescription(String.format("`Message:`\n%s\n\n" + TimeFormat.RELATIVE.now(), m.getContentRaw()));
                            builder.addField("User:", event.getAuthor().getAsMention(), false);
                            builder.setFooter(String.format("Tag: %s | ID: %s", event.getAuthor().getAsTag(), event.getAuthor().getId()));
                            builder.setColor(Color.decode("#FFCC37"));

                            LogMessage.sendLogMessage(event.getGuild().getIdLong(), builder.build());

                            builder.clear();
                        }

                        EmbedBuilder notification = new EmbedBuilder();
                        notification.setTimestamp(Instant.now())
                                .setTitle("Spam Detection")
                                .setDescription(String.format("%s has been muted for 1 minute\n\n", event.getMember().getEffectiveName()))
                                .addField("Reason:", "Similar-Messages / Spam", false)
                                .setFooter(String.format("%s | %s", event.getAuthor().getAsTag(), event.getAuthor().getId()));
                        notification.setColor(Color.decode("#9837FF"));

                        event.getChannel().sendMessageEmbeds(notification.build()).queue();
                        notification.clear();
                        break;
                    }
                }
            });

        });

        }

    private String removePrefix(final String commandName, final String prefix, String content) {
        content = content.substring(commandName.length() + prefix.length());
        if (content.startsWith(" "))
            content = content.substring(1);
        return content;
    }

    public static ThreadFactory newThreadFactory(String threadName, boolean isdaemon) {
        return (r) ->
        {
            Thread t = new Thread(r, threadName);
            t.setDaemon(isdaemon);
            return t;
        };
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        Command.removeResponses(event.getChannel(), event.getMessageIdLong());
    }

    public static Boolean isAuthorized(final Command c, final Long guildID, final Member member, final Integer permissionIndex) throws SQLException {
        boolean authorized = false;

        if(c.isOwnerOnly()){
            if(member.getIdLong() == SherlockBot.botOwnerID){

                System.out.println(member.getIdLong());
                System.out.println(SherlockBot.botOwnerID);

                return true;
            } else {
                return false;
            }
        }

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

    private void handleSelfRoleEvent(final GuildMessageReceivedEvent event, final Long roleID) {

        Role role = event.getGuild().getRoleById(roleID);
        if (role != null) {

            try {

                if (event.getMember().getRoles().contains(role)) {
                    event.getGuild().removeRoleFromMember(event.getMember().getIdLong(), role).reason("Requested via SelfRole").queue(success -> {
                        event.getMessage().addReaction("\uD83D\uDC4D ").queue();
                    });
                } else {
                    event.getGuild().addRoleToMember(event.getMember().getIdLong(), role).reason("Requested via SelfRole").queue(success -> {
                        event.getMessage().addReaction("\uD83D\uDC4D ").queue();
                    });
                }

            } catch (PermissionException permissionException) {
                event.getMessage().reply("Missing Permissions: " + permissionException.getPermission().toString()).queue();
                event.getMessage().addReaction("⚠").queue();
            }
        }
    }

}
