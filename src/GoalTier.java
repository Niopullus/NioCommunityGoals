import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GoalTier {

    private String tierName;
    private int minimumProgress;
    private List<ItemStack> rewards;
    private List<String> commands;

    public GoalTier() {
        super();
    }

    public int getMinimumProgress() {
        return minimumProgress;
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }

    public String getTierName() {
        return tierName;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setMinimumProgress(int minimumProgress) {
        this.minimumProgress = minimumProgress;
    }

    public void setRewards(List<ItemStack> rewards) {
        this.rewards = rewards;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public void setCommands(final List<String> commands) {
        this.commands = commands;
    }
}
