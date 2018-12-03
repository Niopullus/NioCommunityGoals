import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GoalProgressExtractor {

    public GoalProgressExtractor() {
        super();
    }

    public void extractGoalProgress(final Map<String, CommunityGoal> goals, final File file) {
        final JSONParser jsonParser;
        final FileReader fileReader;
        jsonParser = new JSONParser();
        try {
            final JSONObject jsonObject;
            fileReader = new FileReader(file);
            jsonObject = (JSONObject) jsonParser.parse(fileReader);
            for (final String goalName : (Set<String>) jsonObject.keySet()) {
                final JSONObject goalJSON;
                final JSONObject playerProgressJSON;
                final CommunityGoal goal;
                final int totalProgress;
                final HashMap<String, Integer> playerProgress;
                final HashMap<String, Boolean> playerRewardsClaimed;
                final boolean complete;
                playerProgress = new HashMap<>();
                playerRewardsClaimed = new HashMap<>();
                goal = goals.get(goalName);
                goalJSON = (JSONObject) jsonObject.get(goalName);
                totalProgress = (int) ((long) goalJSON.get("totalProgress"));
                complete = (boolean) goalJSON.get("complete");
                goal.setTotalProgress(totalProgress);
                goal.setComplete(complete);
                playerProgressJSON = (JSONObject) goalJSON.get("playerProgress");
                for (final String playerID : (Set<String>) playerProgressJSON.keySet()) {
                    final int progress;
                    final JSONObject playerData;
                    final boolean rewardsClaimed;
                    playerData = (JSONObject) playerProgressJSON.get(playerID);
                    progress = (int) ((long) playerData.get("progress"));
                    rewardsClaimed = (boolean) playerData.get("claimedRewards");
                    playerProgress.put(playerID, progress);
                    playerRewardsClaimed.put(playerID, rewardsClaimed);
                }
                goal.setPlayerProgress(playerProgress);
                goal.setRewardClaimed(playerRewardsClaimed);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
