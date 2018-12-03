import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackParser {

    public ItemStackParser() {
        super();
    }

    public ItemStack getItemStack(final ConfigurationSection section) {
        final ItemStack itemStack;
        final ItemMeta itemMeta;
        final String id;
        String name;
        String lore;
        final List<String> fullLore;
        final int amount;
        final String damage;
        Material material;
        id = section.getString("id");
        if (id.equals("*nothing*")) {
            itemStack = null;
        } else {
            material = materialFromId(id);
            if (material == null) {
                System.out.println("INVALID ITEM ID ENTERED");
            }
            name = section.getString("name");
            lore = section.getString("lore");
            damage = section.getString("damage");
            amount = section.getInt("amount");
            itemStack = new ItemStack(material, amount);
            itemMeta = itemStack.getItemMeta();
            if (name != null) {
                name = "" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            if (lore != null) {
                String currentLine;
                lore = "" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', lore);
                fullLore = new ArrayList<>();
                currentLine = "";
                for (final char c : lore.toCharArray()) {
                    if (c == '|') {
                        fullLore.add(currentLine);
                        currentLine = "";
                    } else {
                        currentLine += c;
                    }
                }
                fullLore.add(currentLine);
                itemMeta.setLore(fullLore);
            }
            itemStack.setItemMeta(itemMeta);
            if (damage != null) {
                itemStack.setDurability((short) Integer.parseInt(damage));
            }
        }
        return itemStack;
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

}
