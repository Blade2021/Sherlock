package rsystems.events;

import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rsystems.SherlockBot;
import rsystems.handlers.LogChannel;

import java.util.ArrayList;

public class GuildCategoryDeleted extends ListenerAdapter {

    public void onCategoryDelete(CategoryDeleteEvent event) {

        if(event.getIdLong() == SherlockBot.database.getLong("GuildTable","ArchiveCat","GuildID",event.getGuild().getIdLong())){
            SherlockBot.database.putValueNull("GuildTable","ArchiveCat","GuildID",event.getGuild().getIdLong());
            //todo log error
        }

        if (SherlockBot.database.archiveChannelList(event.getIdLong(), event.getGuild().getIdLong()).size() > 0) {
            ArrayList<Long> channelList = new ArrayList();
            channelList.addAll(SherlockBot.database.archiveChannelList(event.getIdLong(), event.getGuild().getIdLong()));

            StringBuilder channelNameString = new StringBuilder();
            StringBuilder channelIDString = new StringBuilder();

            for (Long channelID : channelList) {
                try {
                    channelNameString.append(event.getGuild().getTextChannelById(channelID).getName()).append("\n");
                    channelIDString.append(channelID).append("\n");
                } catch (NullPointerException e) {
                }
                SherlockBot.database.deleteRow("ArchiveTable", "ChannelID", channelID);
            }

            LogChannel logChannel = new LogChannel();
            logChannel.logNotification(event.getGuild(),"Category Deletion","You deleted a category that had linked channels in the archive",2,"Channel Names:",channelNameString.toString(),"Channel IDs",channelIDString.toString());
        }
    }
}
