import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityGoal {

    private String name;
    private int target;
    private int totalProgress;
    private ItemStack goalItem;
    private Map<String, Integer> playerProgress;
    private Map<String, Boolean> rewardClaimed;
    private List<GoalTier> tiers;
    private boolean complete;

    public CommunityGoal(final String name, final int target, final ItemStack goalItem) {
        super();
        this.name = name;
        this.target = target;
        this.goalItem = goalItem;
        totalProgress = 0;
        playerProgress = new HashMap<>();
        tiers = new ArrayList<>();
        complete = false;
        rewardClaimed = new HashMap<>();
    }

    public Map<String, Integer> getPlayerProgress() {
        return playerProgress;
    }

    public Map<String, Boolean> getRewardClaimed() {
        return rewardClaimed;
    }

    public int getTotalProgress() {
        return totalProgress;
    }

    public int getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }

    public ItemStack getGoalItem() {
        return goalItem;
    }

    public List<GoalTier> getTiers() {
        return tiers;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setPlayerProgress(final HashMap<String, Integer> playerProgress) {
        this.playerProgress = playerProgress;
    }

    public void setRewardClaimed(final Map<String, Boolean> rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }

    public void setTotalProgress(final int totalProgress) {
        this.totalProgress = totalProgress;
    }

    public void setTiers(List<GoalTier> tiers) {
        this.tiers = tiers;
    }

    public void addTier(final GoalTier goalTier) {
        tiers.add(goalTier);
    }

    public void setComplete(final boolean complete) {
        this.complete = complete;
    }

    public boolean isGoalItem(final ItemStack itemStack) {
        final Material material1;
        final Material material2;
        final ItemMeta itemMeta1;
        final ItemMeta itemMeta2;
        final String name1;
        final String name2;
        final List<String> lore1;
        final List<String> lore2;
        boolean condition1;
        boolean condition2;
        boolean condition3;
        if (itemStack == null) {
            return false;
        }
        condition2 = true;
        condition3 = true;
        material1 = goalItem.getType();
        material2 = itemStack.getType();
        condition1 = material1.equals(material2);
        itemMeta1 = goalItem.getItemMeta();
        itemMeta2 = itemStack.getItemMeta();
        if (itemMeta1 == null) {
            if (itemMeta2 != null) {
                return false;
            }
        } else {
            if (itemMeta2 == null) {
                return false;
            }
            name1 = itemMeta1.getDisplayName();
            name2 = itemMeta2.getDisplayName();
            lore1 = itemMeta1.getLore();
            lore2 = itemMeta2.getLore();
            condition2 = name1.equals(name2);
            condition3 = loresEqual(lore1, lore2);
        }
        return condition1 && condition2 && condition3;
    }

    private boolean loresEqual(final List<String> lore1, final List<String> lore2) {
        if (lore1 == null) {
            return lore2 == null;
        }
        if (lore2 == null) {
            return false;
        }
        if (lore1.size() != lore2.size()) {
            return false;
        }
        for (int i = 0; i < lore1.size(); i++) {
            final String loreItem;
            loreItem = lore1.get(i);
            if (!loreItem.equals(lore2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public void advanceProgress(final String playerID, final int amount) {
        final int progressableAmount;
        progressableAmount = getProgressableAmount(amount);
        totalProgress += progressableAmount;
        if (playerProgress.containsKey(playerID)) {
            playerProgress.put(playerID, playerProgress.get(playerID) + progressableAmount);
        } else {
            playerProgress.put(playerID, amount);
        }
        if (totalProgress >= target) {
            challengeComplete();
        }
    }

    public int getProgressableAmount(final int amount) {
        if (target - totalProgress - amount < 0) {
            return target - totalProgress;
        } else {
            return amount;
        }
    }

    public int getPlayerContribution(final Player player) {
        final String playerID;
        playerID = player.getUniqueId().toString();
        if (playerProgress.containsKey(playerID)) {
            return playerProgress.get(playerID);
        } else {
            return 0;
        }
    }

    private void challengeComplete() {
        complete = true;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ConfigParser.goalFinishedMessage.replace("*goal name*", name));
        }
    }

    public GoalTier getTierForPlayer(final Player player) {
        GoalTier selectedTier;
        final int progress;
        final String playerID;
        selectedTier = null;
        playerID = player.getUniqueId().toString();
        if (playerProgress.containsKey(playerID)) {
            progress = playerProgress.get(playerID);
            for (final GoalTier tier : tiers) {
                if (progress >= tier.getMinimumProgress()) {
                    if (rewardClaimed.containsKey(playerID)) {
                        if (selectedTier == null) {
                            if (!rewardClaimed.get(playerID)) {
                                selectedTier = tier;
                            }
                        } else if (selectedTier.getMinimumProgress() < tier.getMinimumProgress()) {
                            if (!rewardClaimed.get(playerID)) {
                                selectedTier = tier;
                            }
                        }
                    } else {
                        if (selectedTier == null) {
                            selectedTier = tier;
                        } else if (selectedTier.getMinimumProgress() < tier.getMinimumProgress()) {
                            selectedTier = tier;
                        }
                    }
                }
            }
            return selectedTier;
        }
        return null;
    }

    public void markPlayerRewardsClaimed(final Player player) {
        rewardClaimed.put(player.getUniqueId().toString(), true);
    }

    public void reset() {
        totalProgress = 0;
        playerProgress = new HashMap<>();
        complete = false;
        rewardClaimed = new HashMap<>();
    }

}
