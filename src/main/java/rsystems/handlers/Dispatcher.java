package rsystems.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;
import rsystems.Config;
import rsystems.SherlockBot;
import rsystems.commands.botManager.ForceDisconnect;
import rsystems.commands.botManager.Shutdown;
import rsystems.commands.botManager.Test;
import rsystems.commands.guildFunctions.*;
import rsystems.commands.modCommands.*;
import rsystems.commands.publicCommands.Help;
import rsystems.commands.publicCommands.Info;
import rsystems.commands.subscriberOnly.ColorRole;
import rsystems.commands.subscriberOnly.CopyChannel;
import rsystems.objects.Command;
import rsystems.objects.InfractionObject;

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

    private Map<String, Future<?>> futures = new HashMap<>();

    public Dispatcher() {

        registerCommand(new Shutdown());
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

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        //Ignore all bots
        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.isFromGuild()) {
            final String defaultPrefix = Config.get("defaultPrefix");
            final String message = event.getMessage().getContentRaw();
            final String guildPrefix = SherlockBot.guildMap.get(event.getGuild().getIdLong()).getPrefix();
            final boolean defaultPrefixFound = message.toLowerCase().startsWith(SherlockBot.defaultPrefix.toLowerCase());

            //Is channel set to be ignored ( not monitored )?
            if (!isChannelIgnored(event.getGuild().getIdLong(), event.getChannel().getIdLong())) {

                // EVERYONE MONITORING
                if ((event.getMessage().getContentRaw().contains("@everyone")) || (event.getMessage().getContentRaw().contains("@here"))) {
                    if (event.getMessage().getMember() != null) {
                        if (!event.getMessage().getMember().hasPermission(Permission.MESSAGE_MENTION_EVERYONE)) {

                            InfractionObject infractionObject = new InfractionObject(event.getGuild().getIdLong());
                            infractionObject.setEventType(InfractionObject.EventType.WARNING);
                            infractionObject.setUserID(event.getAuthor().getIdLong());
                            infractionObject.setUserTag(event.getAuthor().getAsTag());
                            infractionObject.setModeratorID(SherlockBot.botOwnerID);
                            infractionObject.setModeratorTag(SherlockBot.jda.getSelfUser().getAsTag());
                            infractionObject.setNote("*Original Message:*\n" +
                                    "```" +
                                    event.getMessage().getContentRaw() +
                                    "```"
                            );

                            muteUser(event, "User mentioned @everyone or @here without authorization", 5);

                            LogMessage.sendLogMessage(event.getGuild().getIdLong(), infractionObject);
                            event.getMessage().delete().reason("Message contained broad mention without authorization").queue();

                            return;
                        }
                    }
                }


                //Look for a prefix at the BEGINNING of the message
                if ((defaultPrefixFound) || ((guildPrefix != null) && (message.toLowerCase().startsWith(guildPrefix.toLowerCase())))) {
                    //PREFIX FOUND

                    String prefix;
                    if (defaultPrefixFound) {
                        prefix = defaultPrefix;
                    } else {
                        prefix = guildPrefix;
                    }
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
                    try {
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
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                } else {
                    /*
                    No command was found.
                    No Self role triggers were found.
                    Pass the event to the next series.
                     */
                    handleEvent(message, event);
                }
            }
        } else {
            // Private Message
            event.getMessage().reply("Sorry I haven't been taught how to reply to direct messages yet.  Try again later").queue();

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
                                final MessageReceivedEvent event) {
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
                    messageOwner(event, c, e);
                }
            } else {

                StringBuilder errorString = new StringBuilder();

                if (c.getPermissionIndex() != null) {
                    errorString.append("Permission Index: ").append(c.getPermissionIndex()).append("\n");
                }

                if (c.getDiscordPermission() != null) {
                    errorString.append("Discord Permission: ").append(c.getDiscordPermission().getName());
                }

                event.getMessage().reply(String.format(event.getAuthor().getAsMention() + " You are not authorized for command: `%s`\n%s", c.getName(), errorString)).queue();
            }
        });
    }

    private void handleEvent(final String message,
                             final MessageReceivedEvent event) {
        this.pool.submit(() ->
        {

            // Filter out join messages
            if (event.getMessage().getType().equals(MessageType.GUILD_MEMBER_JOIN)) {
                return;
            }

            //Check for Guild invites
            if (event.getMessage().getInvites().size() > 0) {
                try {
                    List<Long> whiteListedGuilds = SherlockBot.database.getLongMultiple("InviteWhitelist", "TargetGuildID", "ChildGuildID", event.getGuild().getIdLong());

                    for (String code : event.getMessage().getInvites()) {
                        Invite.resolve(SherlockBot.jda, code).queue(resolvedInvite -> {
                            Long targetGuildID = resolvedInvite.getGuild().getIdLong();
                            if ((event.getGuild().getIdLong() == targetGuildID) || (whiteListedGuilds.contains(targetGuildID))) {
                                // ID is ok
                            } else {
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setTitle("Discord Link Detection");
                                builder.setDescription("User posted unauthorized discord link:\n" + resolvedInvite.getUrl());
                                builder.setColor(Color.yellow);
                                if (event.getMessage().getMember().getAsMention() != null) {
                                    builder.addField("User:", String.format(event.getMessage().getMember().getAsMention() + "\n%s\n%s", event.getAuthor().getAsTag(), event.getAuthor().getId()), true);
                                } else {
                                    builder.addField("User:", String.format("n%s\n%s", event.getAuthor().getAsTag(), event.getAuthor().getId()), true);
                                }
                                builder.addField("Target Guild:", String.format("%s\n%d", resolvedInvite.getGuild().getName(), resolvedInvite.getGuild().getIdLong()), true);
                                builder.setTimestamp(Instant.now());

                                LogMessage.sendLogMessage(event.getGuild().getIdLong(), builder.build());

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
                    if (messages.size() >= Integer.parseInt(Config.get("spamLimit"))) {
                        messages.add(event.getMessageId());

                        muteUser(event, "Similar-Message Spam", 1);

                        event.getChannel().purgeMessagesById(messages);

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
                        break;
                    }
                }
            });

            // Language Filter
            String filterWord = filterWordFound(event.getMessage(), event.getGuild().getIdLong());
            if (filterWord != null) {

                // TRIGGER DELETION AND NOTIFICATION
                event.getMessage().addReaction("⚠").queue();
                event.getMessage().reply(String.format("This server does not allow the word || %s || to be used here.\nPlease edit your message accordingly or it will be **deleted** automatically", filterWord)).queue(success -> {
                    success.delete().queueAfter(60, TimeUnit.SECONDS);
                });

                futures.put(event.getMessageId(), event.getMessage().delete().submitAfter(60, TimeUnit.SECONDS));

                if (LogMessage.hasLogChannel(event.getGuild().getIdLong())) {
                    InfractionObject infractionObject = new InfractionObject(event.getGuild().getIdLong());
                    infractionObject.setModeratorTag(SherlockBot.jda.getSelfUser().getAsTag());
                    infractionObject.setModeratorID(SherlockBot.jda.getSelfUser().getIdLong());
                    infractionObject.setEventType(InfractionObject.EventType.WARNING);
                    infractionObject.setUserTag(event.getAuthor().getAsTag());
                    infractionObject.setUserID(event.getAuthor().getIdLong());
                    infractionObject.setNote(String.format("Filter word detected! \n||%s||", filterWord));

                    LogMessage.sendLogMessage(event.getGuild().getIdLong(), infractionObject);
                }
            }
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
    public void onMessageDelete(MessageDeleteEvent event) {
        Command.removeResponses(event.getChannel(), event.getMessageIdLong());
    }

    private boolean isChannelIgnored(Long guildID, Long channelID) {
        try {
            if (SherlockBot.database.getLong("IgnoreChannelTable", "ChannelID", "ChildGuildID", guildID, "ChannelID", channelID) == null) {
                return false;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return true;
        }
        return true;
    }

    public static Boolean isAuthorized(final Command c, final Long guildID, final Member member, final Integer permissionIndex) throws SQLException {
        boolean authorized = false;

        if (c.isOwnerOnly()) {
            if (member.getIdLong() == SherlockBot.botOwnerID) {

                System.out.println(member.getIdLong());
                System.out.println(SherlockBot.botOwnerID);

                return true;
            } else {
                return false;
            }
        }

        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        if (c.getDiscordPermission() != null) {
            if (member.getPermissions().contains(c.getDiscordPermission())) {
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

    private void messageOwner(final MessageReceivedEvent event, final Command c, final Exception exception) {

        SherlockBot.jda.getUserById(SherlockBot.botOwnerID).openPrivateChannel().queue((channel) -> {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("System Exception Encountered")
                    .setColor(Color.RED)
                    .addField("Command:", c.getName(), true)
                    .addField("Calling User:", event.getMessage().getAuthor().getAsTag(), true)
                    .addBlankField(true)
                    .addField("Exception:", exception.toString(), false)
                    .setDescription(exception.getCause().getMessage().substring(0, exception.getCause().getMessage().indexOf(":")));

            channel.sendMessageEmbeds(embedBuilder.build()).queue();

            embedBuilder.clear();
        });
    }

    private void handleSelfRoleEvent(final MessageReceivedEvent event, final Long roleID) {

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

    private void muteUser(final MessageReceivedEvent event, final String reason, final int minutes) {
        Role muteRole = event.getGuild().getRoleById(SherlockBot.guildMap.get(event.getGuild().getIdLong()).getMuteRoleID());
        if (muteRole != null) {
            event.getGuild().addRoleToMember(event.getMember(), muteRole).reason("Spam Detected").queue(Success -> {
                try {
                    event.getGuild().removeRoleFromMember(event.getMember(), muteRole).reason("Mute Expiration").queueAfter(minutes, TimeUnit.MINUTES, null, failure -> {
                        System.out.println("Couldn't unmute user");
                    }, scheduledExecutorService);
                } catch (ErrorResponseException e) {
                    System.out.println(String.format("An error occured for guild: %d",event.getGuild().getIdLong()));
                }
            });

            EmbedBuilder notification = new EmbedBuilder();
            notification.setTimestamp(Instant.now())
                    .setTitle("Spam Detection")
                    .setDescription(String.format("%s has been muted for %d minute(s)\n\n", event.getMember().getEffectiveName(), minutes))
                    .addField("Reason:", reason, false)
                    .setFooter(String.format("%s | %s", event.getAuthor().getAsTag(), event.getAuthor().getId()));
            notification.setColor(Color.decode("#9837FF"));

            event.getChannel().sendMessageEmbeds(notification.build()).queue();
            notification.clear();
        }
    }

    private String filterWordFound(final Message message, final Long guildID) {
        // Language Filter
        ArrayList<String> filterWordList = null;
        try {
            filterWordList = SherlockBot.database.getFilterWords(guildID);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (String s : filterWordList) {
            if (message.getContentRaw().toLowerCase().contains(s.toLowerCase())) {
                // bad word detected

                String[] args = message.getContentRaw().split("\\s+");
                for (String arg : args) {
                    if (arg.toLowerCase().contains(s.toLowerCase())) {
                        if (arg.startsWith("https://tenor.com")) {
                            return null;
                        } else {
                            return s;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.isFromGuild()) {

            final boolean futureFound = futures.containsKey(event.getMessageId());

            String filterWordFound = filterWordFound(event.getMessage(), event.getGuild().getIdLong());

            if (filterWordFound != null) {
                // Filtered word found on message

                if(!futureFound){
                    futures.put(event.getMessageId(), event.getMessage().delete().submitAfter(30, TimeUnit.SECONDS));
                    try {
                        event.getMessage().addReaction("⁉").queue();
                    } catch (NullPointerException e) {

                    }
                }

            } else if(futureFound){
                try {
                    String messageid = event.getMessageId();

                    // Cancel the future
                    for (Map.Entry<String, Future<?>> entry : futures.entrySet()) {
                        String key = entry.getKey();
                        if (key.equalsIgnoreCase(event.getMessageId())) {
                            entry.getValue().cancel(true);
                            System.out.println("Removing future for message: " + entry.getKey());
                            messageid = entry.getKey();
                        }
                    }


                    //Remove the entry from the HashMap
                    Iterator it = futures.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (pair.getKey().toString().equalsIgnoreCase(messageid)) {
                            it.remove();
                        }
                    }

                    event.getMessage().removeReaction("⚠").queue();
                    event.getMessage().removeReaction("⁉").queue();
                } catch (NullPointerException ignored) {

                }
            }
        }
    }

}
