package rsystems.threads;

import rsystems.SherlockBot;

import java.sql.SQLException;
import java.util.TimerTask;

public class ExpiredTrackersCheck extends TimerTask {
    @Override
    public void run() {
        try {
            SherlockBot.database.checkForExpiredTrackers();
            System.out.println("--- Expired trackers check completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
