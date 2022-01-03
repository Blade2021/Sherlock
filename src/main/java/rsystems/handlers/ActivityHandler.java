package rsystems.handlers;

import net.dv8tion.jda.api.entities.Activity;
import rsystems.SherlockBot;

import java.sql.SQLException;

public class ActivityHandler {

    public void pushNextActivity(){
        try {
            String newActivity = SherlockBot.database.nextActivity(SherlockBot.activityIndex);
            SherlockBot.jda.getPresence().setActivity(Activity.playing(newActivity));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
