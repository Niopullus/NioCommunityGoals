import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ConfigParser {

    private ItemStackParser itemStackParser;
    public static String headerMessage = "&5Goal Info:";
    public static String goalFinishedMessage = "&5Goal *goal name* has been completed.";

    public ConfigParser() {
        super();
        itemStackParser = new ItemStackParser();
    }

    public Map<String, CommunityGoal> extractCommunityGoals(final FileConfiguration config) {
        final Map<String, CommunityGoal> goals;
        final Set<String> goalNames;
        final ConfigurationSection goalsSection;
        goals = new HashMap<>();
        goalsSection = config.getConfigurationSection("goals");
        goalNames = goalsSection.getKeys(false);
        for (final String goalName : goalNames) {
            final CommunityGoal goal;
            final ConfigurationSection section;
            final int target;
            final String goalItemID;
            final String goalItemName;
            final List<String> goalItemLore;
            final ItemStack itemStack;
            final ItemMeta itemMeta;
            final ConfigurationSection tiersSection;
            final List<GoalTier> tiers;
            section = goalsSection.getConfigurationSection(goalName);
            target = section.getInt("target");
            goalItemID = section.getString("goalItemID");
            goalItemName = withColor(section.getString("goalItemName"), false);
            goalItemLore = loreFromLine(withColor(section.getString("goalItemLore"), false));
            itemStack = new ItemStack(materialFromId(goalItemID), 1);
            itemMeta = itemStack.getItemMeta();
            if (goalItemName != null) {
                itemMeta.setDisplayName(goalItemName);
            }
            if (goalItemLore != null) {
                itemMeta.setLore(goalItemLore);
            }
            itemStack.setItemMeta(itemMeta);
            tiersSection = section.getConfigurationSection("tiers");
            tiers = extractTiers(tiersSection);
            goal = new CommunityGoal(goalName, target, itemStack);
            goal.setTiers(tiers);
            goals.put(goalName, goal);
        }
        headerMessage = withColor(config.getString("headerMessage"), false);
        goalFinishedMessage = withColor(config.getString("goalFinishedMessage") ,false);
        return goals;
    }

    private List<String> loreFromLine(final String line) {
        String currentLine;
        final List<String> fullLore;
        if (line == null) {
            return null;
        }
        fullLore = new ArrayList<>();
        currentLine = "";
        for (final char c : line.toCharArray()) {
            if (c == '|') {
                fullLore.add(currentLine);
                currentLine = "";
            } else {
                currentLine += c;
            }
        }
        fullLore.add(currentLine);
        return fullLore;
    }

    private Material materialFromId(final String id) {
        final MinecraftKey minecraftKey;
        final Item item;
        String name;
        minecraftKey = new MinecraftKey(id);
        item = IRegistry.ITEM.get(minecraftKey);
        if (item != null) {
            final Material material;
            name = item.getName();
            name = name.substring(name.lastIndexOf('.') + 1).toUpperCase();
            material = Material.getMaterial(name);
            return material;
        }
        return null;
    }

    private String withColor(final String line, final boolean performReset) {
        if (line == null) {
            return null;
        }
        if (performReset) {
            return "" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', line);
        } else {
            return "" + ChatColor.translateAlternateColorCodes('&', line);
        }
    }

    private List<GoalTier> extractTiers(final ConfigurationSection section) {
        final List<GoalTier> tiers;
        tiers = new ArrayList<>();
        for (final String key : section.getKeys(false)) {
            final ConfigurationSection tierSection;
            final GoalTier tier;
            tierSection = section.getConfigurationSection(key);
            tier = extractTier(tierSection);
            tiers.add(tier);
        }
        return tiers;
    }

    private GoalTier extractTier(final ConfigurationSection section) {
        final GoalTier tier;
        final ConfigurationSection rewardsSection;
        final ConfigurationSection commandsSection;
        final String tierName;
        final int minimumProgress;
        final List<ItemStack> rewards;
        final List<String> commands;
        rewardsSection = section.getConfigurationSection("rewards");
        minimumProgress = section.getInt("minimumProgress");
        tierName = section.getString("name");
        tier = new GoalTier();
        rewards = extractTierRewards(rewardsSection);
        commandsSection = section.getConfigurationSection("commands");
        commands = extractCommands(commandsSection);
        tier.setMinimumProgress(minimumProgress);
        tier.setTierName(tierName);
        tier.setRewards(rewards);
        tier.setCommands(commands);
        return tier;
    }

    private List<ItemStack> extractTierRewards(final ConfigurationSection section) {
        final List<ItemStack> items;
        items = new ArrayList<>();
        for (final String key : section.getKeys(false)) {
            final ConfigurationSection itemSection;
            final ItemStack item;
            itemSection = section.getConfigurationSection(key);
            item = itemStackParser.getItemStack(itemSection);
            items.add(item);
        }
        return items;
    }

    private List<String> extractCommands(final ConfigurationSection section) {
        final List<String> commands;
        commands = new ArrayList<>();
        for (final String key : section.getKeys(false)) {
            final String command;
            command = section.getString(key);
            commands.add(command);
        }
        return commands;
    }

}
