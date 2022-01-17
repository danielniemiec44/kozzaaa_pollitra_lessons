package eu.simplenetwork.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements CommandExecutor, Listener {
    public static Logger console;
    FileConfiguration config = getConfig();
    static Inventory gui;
    static HashMap<String, Integer> kitTime = new HashMap<>();
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    static String guiName;

    @Override
    public void onEnable() {
        super.onEnable();

        //konfiguracja
        config.options().copyDefaults(true);
        saveConfig();
        createDataFile();
        saveCustomConfig();
        guiName = config.getString("gui-name");


        //gui
        int size = config.getInt("size");
        gui = Bukkit.createInventory(null, size, guiName);
        for(int i = 0 ; i < size ; i++) {
            ItemStack itemStack = new ItemStack(Material.valueOf(config.getString("background")));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(config.getString("name"));
            itemStack.setItemMeta(itemMeta);
            gui.setItem(i, itemStack);

        }
        ConfigurationSection cs = config.getConfigurationSection("kits");
        for(String key : cs.getKeys(false)) {
            ItemStack itemStack = new ItemStack(Material.valueOf(cs.getString(key + ".item")));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(cs.getString(key + ".display-name"));
            List<String> lore = cs.getStringList(key + ".lore");
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            gui.setItem(cs.getInt(key + ".slot"), itemStack);
            kitTime.put(cs.getString(key + ".display-name"), cs.getInt(key + ".time"));
        }


        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            console.info("Komenda jest dostepna tylko z poziomu klienta gry.");
            return true;
        }

        if(label.equalsIgnoreCase("kit")) {
            if (args.length > 0) {
                return false;
            }
            Player p = (Player) sender;
            p.openInventory(gui);
        }

        return true;
    }


    public void reloadCustomConfig() throws UnsupportedEncodingException {

        // Look for defaults in the jar
        Reader defConfigStream = new InputStreamReader(this.getResource("data.yml"), "UTF8");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }


    public void createDataFile() {
        if (customConfigFile == null) {
            customConfigFile = new File(getDataFolder(), "data.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }


    public FileConfiguration getDataFile() throws UnsupportedEncodingException {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }


    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getDataFile().save(customConfigFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

    @EventHandler
    public void cancelItemMoveInGui(InventoryClickEvent e) {
        if(e.getView().getTitle().equalsIgnoreCase(guiName)) {
            e.setCancelled(true);
        }
    }

}
