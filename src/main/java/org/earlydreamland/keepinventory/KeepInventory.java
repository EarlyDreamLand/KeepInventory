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
    private static final String PERMISSION_ADMIN = "keepinventory.admin";
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

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReloadCommand(sender);
            case "version":
                return handleVersionCommand(sender);
            case "add":
                return handleAddCommand(sender, args);
            case "del":
                return handleDelCommand(sender, args);
            default:
                sender.sendMessage("§6" + PLUGIN_NAME + " §7› §c未知命令！");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("kip")) return null;
        if (args.length == 1) {
            return getFirstLevelCompletions(args[0]);
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();

            if ("add".equals(subCmd)) {
                List<String> worldNames = getServer().getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> !worlds.contains(name))
                        .collect(Collectors.toList());

                return filterCompletions(worldNames, args[1]);
            } else if ("del".equals(subCmd)) {
                return filterCompletions(worlds, args[1]);
            }
        }

        return Collections.emptyList();
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("§6===== " + PLUGIN_NAME + " 插件帮助 =====");
        sender.sendMessage("§a/kip reload §7- 重载插件配置");
        sender.sendMessage("§a/kip version §7- 显示插件版本");
        sender.sendMessage("§a/kip add <world> §7- 为此世界启用死亡不掉落");
        sender.sendMessage("§a/kip del <world> §7- 为此世界禁用死亡不掉落");
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RELOAD)) {
            sender.sendMessage("§c你没有权限执行此命令！");
            return true;
        }

        boolean oldMetrics = this.metrics;
        reloadPluginConfig();

        if (!oldMetrics && this.metrics) {
            setupMetrics();
        }
        sender.sendMessage("§6" + PLUGIN_NAME + " §7› §a配置已成功重载！");

        return true;
    }

    private void setupMetrics() {
        if (!metrics) return;
        new Metrics(this, 26836);
        getLogger().info("已启用 BStats 统计功能。");
    }

    private boolean handleVersionCommand(CommandSender sender) {
        PluginDescriptionFile description = getDescription();
        String authors = String.join(", ", description.getAuthors());
        sender.sendMessage("§6插件版本：§a" + description.getVersion());
        sender.sendMessage("§6开发作者：§a" + authors);
        sender.sendMessage("§6项目地址：§a" + GITHUB_URL);
        return true;
    }

    private boolean handleAddCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_ADMIN)) {
            sender.sendMessage("§c你没有权限执行此命令！");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /kip add <世界名称>");
            return true;
        }

        String worldName = args[1];
        if (worlds.contains(worldName)) {
            sender.sendMessage("§6" + PLUGIN_NAME + " §7› §c世界 '" + worldName + "' 已在列表中！");
            return true;
        }

        World world = getServer().getWorld(worldName);
        if (world == null) {
            world = tryLoadWorld(worldName);
            if (world == null) {
                sender.sendMessage("§6" + PLUGIN_NAME + " §7› §c世界 '" + worldName + "' 不存在且无法加载！");
                return true;
            }
        }

        worlds.add(worldName);
        updateConfigWorlds();
        applyKeepInventoryToWorld(world);
        sender.sendMessage("§6" + PLUGIN_NAME + " §7› §a已为世界 '" + worldName + "' 启用死亡不掉落！");
        return true;
    }

    private World tryLoadWorld(String worldName) {
        World world = getServer().getWorld(worldName);

        if (world == null) {
            getLogger().info("尝试加载世界: " + worldName);
        }

        return world;
    }

    private boolean handleDelCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_ADMIN)) {
            sender.sendMessage("§c你没有权限执行此命令！");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /kip del <世界名称>");
            return true;
        }

        String worldName = args[1];
        if (!worlds.contains(worldName)) {
            sender.sendMessage("§6" + PLUGIN_NAME + " §7› §c世界 '" + worldName + "' 不在列表中！");
            return true;
        }

        worlds.remove(worldName);
        updateConfigWorlds();

        World world = getServer().getWorld(worldName);
        if (world != null) {
            try {
                world.setGameRule(GameRule.KEEP_INVENTORY, false);
                sender.sendMessage("§6" + PLUGIN_NAME + " §7› §a已为世界 '" + worldName + "' 禁用死亡不掉落！");
            } catch (Exception e) {
                sender.sendMessage("§6" + PLUGIN_NAME + " §7› §c修改世界规则失败: " + e.getMessage());
            }
        } else {
            sender.sendMessage("§6" + PLUGIN_NAME + " §7› §e世界 '" + worldName + "' 不存在，但已从配置移除！");
        }

        return true;
    }

    private void updateConfigWorlds() {
        getConfig().set("worlds", worlds);
        saveConfig();
    }

    private List<String> getFirstLevelCompletions(String input) {
        return filterCompletions(Arrays.asList("reload", "version", "add", "del"), input);
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
        List<String> oldWorlds = worlds == null ? new ArrayList<>() : new ArrayList<>(worlds);

        reloadConfig();
        enabled = getConfig().getBoolean("enable", true);
        metrics = getConfig().getBoolean("metrics", true);
        worlds = getConfig().getStringList("worlds");
        if (worlds == null) {
            worlds = new ArrayList<>();
            getLogger().warning("配置中的 'worlds' 列表为空或格式错误，已初始化为空列表！");
        }
        getLogger().info(PLUGIN_NAME + " 插件" + (enabled ? "已启用" : "已禁用") + "！");
    }

    private void handleWorldChanges(List<String> oldWorlds) {
        List<String> removedWorlds = oldWorlds.stream()
                .filter(w -> !worlds.contains(w))
                .collect(Collectors.toList());

        List<String> addedWorlds = worlds.stream()
                .filter(w -> !oldWorlds.contains(w))
                .collect(Collectors.toList());

        removedWorlds.forEach(worldName -> {
            World world = getServer().getWorld(worldName);
            if (world != null) {
                try {
                    world.setGameRule(GameRule.KEEP_INVENTORY, false);
                    getLogger().info("已为 '" + worldName + "' 世界禁用死亡不掉落！");
                } catch (Exception e) {
                    getLogger().warning("为 '" + worldName + "' 世界设置死亡不掉落失败: " + e.getMessage());
                }
            }
        });

        addedWorlds.forEach(worldName -> {
            World world = getServer().getWorld(worldName);
            if (world != null) {
                applyKeepInventoryToWorld(world);
            } else {
                getLogger().warning("新增世界 '" + worldName + "' 不存在！");
            }
        });
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.STARTUP && enabled) {
            applyKeepInventory();
        }
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
        if (enabled) {
            if (worlds.isEmpty()) {
                getServer().getWorlds().forEach(world -> {
                    try {
                        world.setGameRule(GameRule.KEEP_INVENTORY, false);
                        getLogger().info("已恢复世界 '" + world.getName() + "' 的死亡不掉落规则");
                    } catch (Exception e) {
                        getLogger().warning("恢复世界 '" + world.getName() + "' 规则失败: " + e.getMessage());
                    }
                });
            } else {
                worlds.forEach(worldName -> {
                    World world = getServer().getWorld(worldName);
                    if (world != null) {
                        try {
                            world.setGameRule(GameRule.KEEP_INVENTORY, false);
                            getLogger().info("已恢复世界 '" + worldName + "' 的死亡不掉落规则");
                        } catch (Exception e) {
                            getLogger().warning("恢复世界 '" + worldName + "' 规则失败: " + e.getMessage());
                        }
                    }
                });
            }
        }
        getLogger().info(PLUGIN_NAME + " 插件已禁用！");
    }
}