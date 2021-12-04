package rsystems.events;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildChannelEvents extends ListenerAdapter {

    /*@Override
    public void onChannelDelete(ChannelDeleteEvent event) {

        if(event.isFromType(ChannelType.TEXT)){
            if(event.getChannel().getType().isGuild()){

            }
        }

        final Long guildID = event.getGuild().getIdLong();

        if((SherlockBot.guildMap.get(guildID).getLogChannelID() != null) && (SherlockBot.guildMap.get(guildID).getLogChannelID().equals(event.getChannel().getIdLong()))){
            SherlockBot.guildMap.get(guildID).setLogChannelID(null);
            try {
                SherlockBot.database.updateGuild(SherlockBot.guildMap.get(guildID));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        final Long guildID = event.getGuild().getIdLong();

        if((SherlockBot.guildMap.get(guildID).getLogChannelID() != null) && (SherlockBot.guildMap.get(guildID).getLogChannelID().equals(event.getChannel().getIdLong()))){
            SherlockBot.guildMap.get(guildID).setLogChannelID(null);
            try {
                SherlockBot.database.updateGuild(SherlockBot.guildMap.get(guildID));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

     */
}
