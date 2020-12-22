package rsystems.events;

import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;

import javax.annotation.Nonnull;

public class GuildEmoteRemoved extends ListenerAdapter {

    @Override
    public void onEmoteRemoved(@Nonnull EmoteRemovedEvent event) {
        SherlockBot.database.deleteRow("ReactionTable","ReactionID",event.getEmote().getId());
    }
}
