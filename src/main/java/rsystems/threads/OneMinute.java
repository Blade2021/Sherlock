package rsystems.threads;

import net.dv8tion.jda.api.entities.Activity;
import rsystems.SherlockBot;

import java.sql.SQLException;
import java.util.TimerTask;

public class OneMinute extends TimerTask {
    @Override
    public void run() {
        pushNextActivity();
    }


    public void pushNextActivity(){
        try {
            final String newActivity = SherlockBot.database.nextActivity(SherlockBot.activityIndex);

            if(!newActivity.equalsIgnoreCase(SherlockBot.jda.getPresence().getActivity().getName())) {
                SherlockBot.jda.getPresence().setActivity(Activity.playing(newActivity));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
