package org.earlydreamland.keepinventory;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.earlydreamland.keepinventory.metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KeepInventory extends JavaPlugin implements Listener {
    private boolean enabled;
    private boolean metrics;
    private List<String> worlds;
    String PluginName = "KeepInventory";

    @Override
    public void onEnable() {
        // 初始化配置
        initPlugin();

        // 初始化Metrics
        if(metrics){
            int pluginId = 26836;
            Metrics metrics = new Metrics(this, pluginId);
            getLogger().info("已启用 BStats 统计功能。");
        } else {
            getLogger().info("已禁用 BStats 统计功能。");
        }

        getCommand("kip").setExecutor(this);
        getCommand("kip").setTabCompleter(this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    private void initPlugin() {
        // 保存默认配置（如果不存在）
        saveDefaultConfig();
        // 重新加载配置
        reloadPluginConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "reload" -> handleReloadCommand(sender);
            case "version" -> handleVersionCommand(sender);
            default -> {
                sender.sendMessage("§6" + PluginName + " §7› §c未知命令！");
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("kip")) {
            return null;
        }

        if (args.length == 1) {
            return getFirstLevelCompletions(args[0]);
        }

        return Collections.emptyList();
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("§6===== " + PluginName + " 插件帮助 =====");
        sender.sendMessage("§a/kip reload §7- 重载插件配置");
        sender.sendMessage("§a/kip version §7- 显示插件版本");
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("keepinventory.reload")) {
            sender.sendMessage("§c你没有权限执行此命令！");
            return true;
        }

        reloadPluginConfig();
        sender.sendMessage("§6" + PluginName + " §7› §a配置已成功重载！");

        if (enabled) {
            applyKeepInventory();
        }

        return true;
    }

    private boolean handleVersionCommand(CommandSender sender) {
        PluginDescriptionFile description = getDescription();
        String pluginVersion = description.getVersion();
        List<String> pluginAuthors = description.getAuthors();

        sender.sendMessage("§6插件版本：§a" + pluginVersion);
        sender.sendMessage("§6开发作者：§a" + pluginAuthors);
        sender.sendMessage("§6项目地址：§ahttps://github.com/EarlyDreamLand/KeepInventory");
        return true;
    }

    private List<String> getFirstLevelCompletions(String input) {
        List<String> commands = Arrays.asList("reload", "version");
        return filterCompletions(commands, input);
    }

    private List<String> filterCompletions(List<String> options, String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>(options);
        }

        String lowerInput = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerInput))
                .sorted()
                .collect(Collectors.toList());
    }

    private void reloadPluginConfig() {
        reloadConfig();

        enabled = getConfig().getBoolean("enable", true);
        metrics = getConfig().getBoolean("metrics", true);
        worlds = getConfig().getStringList("worlds");

        getLogger().info(PluginName + " 插件" + (enabled ? "已启用" : "已禁用") + "！");
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (!enabled) return;
        applyKeepInventory();
    }

    private void applyKeepInventory() {
        if (worlds.isEmpty()) {
            for (World world : getServer().getWorlds()) {
                applyKeepInventoryToWorld(world);
            }
        } else {
            for (String worldName : worlds) {
                World world = getServer().getWorld(worldName);

                if (world != null) {
                    applyKeepInventoryToWorld(world);
                } else {
                    getLogger().warning("配置中的 '" + worldName + "' 世界不存在！");
                }
            }
        }
    }

    private boolean applyKeepInventoryToWorld(World world) {
        try {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            getLogger().info("已为 '" + world.getName() + "' 世界启用死亡不掉落！");
            return true;
        } catch (Exception e) {
            getLogger().warning("为 '" + world.getName() + "' 世界设置死亡不掉落失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(PluginName + " 插件已禁用！");
    }
}