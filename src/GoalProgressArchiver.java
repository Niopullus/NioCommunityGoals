import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class GoalProgressArchiver {

    public GoalProgressArchiver() {
        super();
    }

    public void archiveProgressData(final Map<String, CommunityGoal> goals, final File file) {
        final JSONObject json;
        final Set<String> goalKeys;
        final FileWriter fileWriter;
        json = new JSONObject();
        goalKeys = goals.keySet();
        for (final String goalName : goalKeys) {
            final CommunityGoal goal;
            final JSONObject goalJSON;
            final JSONObject playerProgressJSON;
            final Map<String, Integer> playerProgress;
            final Map<String, Boolean> rewardsClaimed;
            goal = goals.get(goalName);
            playerProgress = goal.getPlayerProgress();
            rewardsClaimed = goal.getRewardClaimed();
            goalJSON = new JSONObject();
            goalJSON.put("totalProgress", goal.getTotalProgress());
            goalJSON.put("complete", goal.isComplete());
            playerProgressJSON = new JSONObject();
            for (final String playerID : playerProgress.keySet()) {
                final JSONObject playerData;
                playerData = new JSONObject();
                playerData.put("progress", playerProgress.get(playerID));
                if (rewardsClaimed.containsKey(playerID)) {
                    playerData.put("claimedRewards", rewardsClaimed.get(playerID));
                } else {
                    playerData.put("claimedRewards", false);
                }
                playerProgressJSON.put(playerID, playerData);
            }
            goalJSON.put("playerProgress", playerProgressJSON);
            json.put(goalName, goalJSON);
        }
        try {
            final String jsonString;
            fileWriter = new FileWriter(file);
            jsonString = json.toJSONString();
            fileWriter.write(jsonString);
            fileWriter.flush();
            fileWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
