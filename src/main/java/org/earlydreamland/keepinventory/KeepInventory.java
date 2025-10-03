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
    private static final String PLUGIN_NAME = "KeepInventory";
    private static final String PERMISSION_RELOAD = "keepinventory.reload";
    private static final String GITHUB_URL = "https://github.com/EarlyDreamLand/KeepInventory";

    private boolean enabled;
    private boolean metrics;
    private List<String> worlds;

    @Override
    public void onEnable() {
        initPlugin();
        setupMetrics();
        registerCommands();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void initPlugin() {
        saveDefaultConfig();
        reloadPluginConfig();
    }

    private void registerCommands() {
        getCommand("kip").setExecutor(this);
        getCommand("kip").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return showHelp(sender);

        return switch (args[0].toLowerCase()) {
            case "reload" -> handleReloadCommand(sender);
            case "version" -> handleVersionCommand(sender);
            default -> {
                sender.sendMessage("§6" + PLUGIN_NAME + " §7› §c未知命令！");
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("kip")) return null;
        if (args.length == 1) return getFirstLevelCompletions(args[0]);
        return Collections.emptyList();
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("§6===== " + PLUGIN_NAME + " 插件帮助 =====");
        sender.sendMessage("§a/kip reload §7- 重载插件配置");
        sender.sendMessage("§a/kip version §7- 显示插件版本");
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RELOAD)) {
            sender.sendMessage("§c你没有权限执行此命令！");
            return true;
        }

        boolean oldMetrics = this.metrics;
        reloadPluginConfig();

        if (!oldMetrics && this.metrics) setupMetrics();
        sender.sendMessage("§6" + PLUGIN_NAME + " §7› §a配置已成功重载！");

        if (enabled) applyKeepInventory();
        return true;
    }

    private void setupMetrics() {
        if (!metrics) return;
        new Metrics(this, 26836);
        getLogger().info("已启用 BStats 统计功能。");
    }

    private boolean handleVersionCommand(CommandSender sender) {
        PluginDescriptionFile description = getDescription();
        sender.sendMessage("§6插件版本：§a" + description.getVersion());
        sender.sendMessage("§6开发作者：§a" + description.getAuthors());
        sender.sendMessage("§6项目地址：§a" + GITHUB_URL);
        return true;
    }

    private List<String> getFirstLevelCompletions(String input) {
        return filterCompletions(Arrays.asList("reload", "version"), input);
    }

    private List<String> filterCompletions(List<String> options, String input) {
        if (input == null || input.isEmpty()) return new ArrayList<>(options);

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
        getLogger().info(PLUGIN_NAME + " 插件" + (enabled ? "已启用" : "已禁用") + "！");
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (enabled) applyKeepInventory();
    }

    private void applyKeepInventory() {
        if (worlds.isEmpty()) {
            getServer().getWorlds().forEach(this::applyKeepInventoryToWorld);
        } else {
            worlds.forEach(worldName -> {
                World world = getServer().getWorld(worldName);
                if (world != null) applyKeepInventoryToWorld(world);
                else getLogger().warning("配置中的 '" + worldName + "' 世界不存在！");
            });
        }
    }

    private void applyKeepInventoryToWorld(World world) {
        try {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            getLogger().info("已为 '" + world.getName() + "' 世界启用死亡不掉落！");
        } catch (Exception e) {
            getLogger().warning("为 '" + world.getName() + "' 世界设置死亡不掉落失败: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(PLUGIN_NAME + " 插件已禁用！");
    }
}