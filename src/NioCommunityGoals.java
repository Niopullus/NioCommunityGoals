import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NioCommunityGoals extends JavaPlugin {

    private Map<String, CommunityGoal> goals;
    private File progressDataFile;

    public void onEnable() {
        progressDataFile = new File(getDataFolder().getAbsolutePath() + "\\progressData.json");
        parseConfig();
        loadGoalProgress();
    }

    private void parseConfig() {
        final FileConfiguration config;
        final ConfigParser parser;
        parser = new ConfigParser();
        config = getConfig();
        goals = parser.extractCommunityGoals(config);
    }

    public void onDisable() {
        saveGoalProgress();
    }

    private void loadGoalProgress() {
        if (progressDataFile.exists()) {
            final GoalProgressExtractor goalProgressExtractor;
            goalProgressExtractor = new GoalProgressExtractor();
            goalProgressExtractor.extractGoalProgress(goals, progressDataFile);
        }
    }

    private void saveGoalProgress() {
        if (!progressDataFile.exists()) {
            try {
                progressDataFile.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        final GoalProgressArchiver goalProgressArchiver;
        goalProgressArchiver = new GoalProgressArchiver();
        goalProgressArchiver.archiveProgressData(goals, progressDataFile);
    }

    public boolean onCommand(final CommandSender commandSender, final Command command, final String label, String[] args) {
        final String commandName;
        commandName = command.getName();
        if (commandName.equals("goalcontribute")) {
            final String goalName;
            final int amount;
            if (args.length != 2) {
                return false;
            }
            goalName = args[0];
            amount = Integer.parseInt(args[1]);
            if (commandSender instanceof Player) {
                contributeCommand((Player) commandSender, goalName, amount);
            } else {
                messagePlayer(commandSender, "Only plays may run that command.");
            }
        } else if (commandName.equals("goalinfo")) {
            final String goalName;
            if (args.length != 1) {
                return false;
            }
            goalName = args[0];
            goalInfoCommand((Player) commandSender, goalName);
        } else if (commandName.equals("goalrewardclaim")) {
            final String goalName;
            if (args.length != 1) {
                return false;
            }
            goalName = args[0];
            goalRewardClaim((Player) commandSender, goalName);
        } else if (commandName.equals("goalresetprogress")) {
            final String goalName;
            if (args.length != 1) {
                return false;
            }
            goalName = args[0];
            resetGoalProgress((Player) commandSender, goalName);
        }
        return true;
    }

    private void contributeCommand(final Player player, final String goalName, final int amount) {
        final PlayerInventory playerInventory;
        final CommunityGoal goal;
        final ItemStack[] contents;
        goal = goals.get(goalName);
        if (goal == null) {
            messagePlayer(player, "That goal does not exist.");
            return;
        }
        if (goal.isComplete()) {
            messagePlayer(player, "That goal is already complete.");
            return;
        }
        playerInventory = player.getInventory();
        contents = playerInventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            final ItemStack item;
            item = contents[i];
            if (goal.isGoalItem(item)) {
                final int progressableAmount;
                boolean doneLooking;
                doneLooking = false;
                progressableAmount = goal.getProgressableAmount(amount);
                if (item.getAmount() > progressableAmount) {
                    item.setAmount(item.getAmount() - progressableAmount);
                    doneLooking = true;
                    goal.advanceProgress(player.getUniqueId().toString(), progressableAmount);
                } else if (item.getAmount() == progressableAmount) {
                    item.setAmount(0);
                    contents[i] = null;
                    playerInventory.setContents(contents);
                    doneLooking = true;
                    goal.advanceProgress(player.getUniqueId().toString(), progressableAmount);
                }
                player.updateInventory();
                if (doneLooking) {
                    messagePlayer(player, "Contribution successful.");
                    return;
                }
            }
        }
        messagePlayer(player, "Goal item (or amount) not found.");
    }

    private void goalInfoCommand(final Player player, final String goalName) {
        final CommunityGoal goal;
        final double progressFactor;
        String progressString;
        final ItemStack goalItem;
        final ItemMeta goalItemMeta;
        final String goalItemName;
        final int selectedMarks;
        goal = goals.get(goalName);
        if (goal == null) {
            messagePlayer(player, "That goal does not exist.");
            return;
        }
        messagePlayer(player, ConfigParser.headerMessage);
        goalItem = goal.getGoalItem();
        goalItemMeta = goalItem.getItemMeta();
        goalItemName = goalItemMeta.getDisplayName();
        messagePlayer(player, "Goal Item: " + goalItemName);
        messagePlayer(player, "Goal Target: " + goal.getTarget());
        messagePlayer(player, "Your Contribution: " + goal.getPlayerContribution(player));
        progressFactor = ((double) goal.getTotalProgress() / (double) goal.getTarget());
        selectedMarks = (int) Math.floor(progressFactor * 30);
        progressString = "Progress: [" + ChatColor.translateAlternateColorCodes('&', "&a");
        for (int i = 0; i < selectedMarks; i++) {
            progressString += "|";
        }
        progressString += ChatColor.translateAlternateColorCodes('&', "&c");
        for (int i = 0; i < (30 - selectedMarks); i++) {
            progressString += "|";
        }
        progressString += ChatColor.translateAlternateColorCodes('&', "&r");
        progressString += "] " + (progressFactor * 100) + " %";
        messagePlayer(player, progressString);
        messagePlayer(player, "Reward Tiers:");
        for (final GoalTier tier : goal.getTiers()) {
            messagePlayer(player, tier.getMinimumProgress() + ": " + tier.getTierName());
        }
    }

    private void messagePlayer(final CommandSender player, final String message) {
        player.sendMessage(message);
    }

    private void goalRewardClaim(final Player player, final String goalName) {
        final CommunityGoal goal;
        goal = goals.get(goalName);
        if (goal == null) {
            messagePlayer(player, "That goal does not exist.");
            return;
        }
        if (goal.isComplete()) {
            final GoalTier tier;
            tier = goal.getTierForPlayer(player);
            if (tier != null) {
                final PlayerInventory playerInventory;
                final List<ItemStack> tierRewards;
                tierRewards = tier.getRewards();
                playerInventory = player.getInventory();
                for (final ItemStack item : tierRewards) {
                    final ItemStack itemClone;
                    itemClone = item.clone();
                    applySignature(itemClone, player);
                    playerInventory.addItem(itemClone);
                }
                playerInventory.addItem();
                player.updateInventory();
                goal.markPlayerRewardsClaimed(player);
                for (final String command : tier.getCommands()) {
                    final String filteredCommand;
                    filteredCommand = command.replace("*player name*", player.getName());
                    getServer().dispatchCommand(getServer().getConsoleSender(), filteredCommand);
                }
                messagePlayer(player, "Goal rewards successfully claimed.");
            } else {
                messagePlayer(player, "Sorry, no unclaimed rewards found.");
            }
        } else {
            messagePlayer(player, "Sorry, that goal's progress is currently insufficient.");
        }
    }

    private void applySignature(final ItemStack itemStack, final Player player) {
        final ItemMeta itemMeta;
        final String itemName;
        final List<String> currentLore;
        final List<String> lore;
        itemMeta = itemStack.getItemMeta();
        itemName = itemMeta.getDisplayName();
        itemMeta.setDisplayName(itemName.replace("*player name*", player.getName()));
        lore = new ArrayList<>();
        currentLore = itemMeta.getLore();
        for (final String loreLine : currentLore) {
            lore.add(loreLine.replace("*player name*", player.getName()));
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
    }

    private void resetGoalProgress(final Player player, final String goalName) {
        final CommunityGoal goal;
        goal = goals.get(goalName);
        if (goal == null) {
            messagePlayer(player, "That goal does not exist.");
            return;
        }
        goal.reset();
    }

}
