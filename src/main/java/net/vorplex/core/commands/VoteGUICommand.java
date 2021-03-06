package net.vorplex.core.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import net.vorplex.core.Main;
import net.vorplex.core.util.BookUtils;
import net.vorplex.core.util.NMSUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

public class VoteGUICommand implements CommandExecutor {
    private static final Main plugin = Main.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (plugin.getConfig().getString("VoteBookGUI.votelinks.1") == null) {
            commandSender.sendMessage("vote link #1 is null!!");
            return false;
        }
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("sorry you must be a player to use this command!");
            return false;
        }
        if (s.equalsIgnoreCase("vote")) {
            Player player = (Player) commandSender;
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            ComponentBuilder text = new ComponentBuilder("        Vote").strikethrough(false).bold(true).color(ChatColor.DARK_GREEN)
                    .append("\n------------------").strikethrough(true).bold(false).color(ChatColor.BLACK)
                    .append("\nVote for the server to gain rewards!\n").strikethrough(false).color(ChatColor.BLACK);
            for (String id : plugin.getConfig().getConfigurationSection("VoteBookGUI.votelinks").getKeys(false)) {
                text.append("\n>> ").strikethrough(false).bold(true).color(ChatColor.BLACK)
                        .append("Link " + id).strikethrough(false).bold(false).color(ChatColor.DARK_GREEN)
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getConfig().getString("VoteBookGUI.votelinks." + id)))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Go to vote link " + id + "!")));
            }
            if (plugin.old) {
                List<Object> pages;
                try {
                    pages = (List<Object>) NMSUtils.getBukkitClass("inventory.CraftMetaBook").getDeclaredField("pages").get(bookMeta);
                } catch (ReflectiveOperationException ex) {
                    ex.printStackTrace();
                    fallbackChatMethod(player);
                    return false;
                }
                for (Class<?> clazz : NMSUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()) {
                    try {
                        Method method = clazz.getDeclaredMethod("a", String.class);
                        if (method != null) {
                            pages.add(method.invoke(clazz, ComponentSerializer.toString(text.create())));
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fallbackChatMethod(player);
                        return false;
                    }
                }
            } else
                try {
                    bookMeta.spigot().setPages(text.create());
                } catch (Exception e) {
                    e.printStackTrace();
                    fallbackChatMethod(player);
                    return true;
                }
            bookMeta.setTitle("/vote GUI");
            bookMeta.setAuthor("Vorplex");
            book.setItemMeta(bookMeta);

            BookUtils.openBook(book, player);
            return true;
        }
        return false;
    }

    private void fallbackChatMethod(Player player) {
        player.sendMessage(ChatColor.RED + "An error occurred in creating the gui, using fallback chat method.");
        player.spigot().sendMessage(new ComponentBuilder("        Vote").strikethrough(false).bold(true).color(ChatColor.DARK_GREEN)
                .append("\n------------------").strikethrough(true).bold(false).color(ChatColor.WHITE)
                .append("\nVote for the server to gain rewards!").strikethrough(false).color(ChatColor.WHITE).create());
        for (String id : plugin.getConfig().getConfigurationSection("VoteBookGUI.votelinks").getKeys(false)) {
            player.spigot().sendMessage(new ComponentBuilder("\n>> ").strikethrough(false).bold(true).color(ChatColor.WHITE)
                    .append("Link " + id).strikethrough(false).bold(false).color(ChatColor.DARK_GREEN)
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getConfig().getString("VoteBookGUI.votelinks." + id)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Go to vote link " + id + "!"))).create());
        }
    }
}

