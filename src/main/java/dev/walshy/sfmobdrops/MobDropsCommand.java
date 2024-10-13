package dev.walshy.sfmobdrops;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MobDropsCommand implements TabExecutor {

    private final List<String> arg0 = Arrays.asList("reload", "list", "new", "delete");

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("sfmobdrops.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用该指令");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
        } else {
            if (args[0].equalsIgnoreCase("reload")) {
                SfMobDrops.getInstance().reloadConfig();
                SfMobDrops.getInstance().loadDrops();
                sender.sendMessage(ChatColor.DARK_GREEN + "已重载配置文件!");
            } else if (args[0].equalsIgnoreCase("list")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "只有玩家才能执行该指令!");
                    return true;
                }
                Guis.openMobDropList((Player) sender);
            } else if (args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("delete")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "只有玩家才能执行该指令!");
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "该指令暂无任何效果");
            } else {
                sendUsage(sender);
            }
        }

        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], arg0, new ArrayList<>());
        } else {
            return Collections.emptyList();
        }
    }

    private void sendUsage(@Nonnull CommandSender sender) {
        sender.sendMessage(
            ChatColor.GRAY + "----------" + ChatColor.GOLD + "SFMobDrops" + ChatColor.GRAY + "----------"
                + '\n' + ChatColor.GOLD + "/mobdrops reload" + ChatColor.GRAY + " - 重载配置文件"
                + '\n' + ChatColor.GOLD + "/mobdrops list" + ChatColor.GRAY + " - 获取掉落列表"
                + '\n' + ChatColor.GOLD + "/mobdrops new" + ChatColor.GRAY + " - 创建掉落配置"
                + '\n' + ChatColor.GOLD + "/mobdrops delete" + ChatColor.GRAY + " - 删除掉落配置"
        );
    }
}
