package com.extendedclip.deluxemenus.action;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.AdventureUtils;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.ExpUtils;
import com.extendedclip.deluxemenus.utils.StringUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;

public class ClickActionTask extends BukkitRunnable {

  private final DeluxeMenus plugin;
  private final String name;
  private final ActionType actionType;
  private final String exec;

  public ClickActionTask(
      @NotNull final DeluxeMenus plugin,
      @NotNull final String name,
      @NotNull final ActionType actionType,
      @NotNull final String exec
  ) {
    this.plugin = plugin;
    this.name = name;
    this.actionType = actionType;
    this.exec = exec;
  }

  @Override
  public void run() {
    final Player player = Bukkit.getServer().getPlayerExact(name);
    if (player == null) {
      return;
    }

    final String executable = PlaceholderAPI.setPlaceholders((OfflinePlayer) player, exec);
    final Optional<MenuHolder> holder = Menu.getMenuHolder(player);

    switch (actionType) {
      case META:
        if (!VersionHelper.IS_PDC_VERSION || DeluxeMenus.getInstance().getPersistentMetaHandler() == null) {
          DeluxeMenus.debug(DebugLevel.HIGHEST, Level.INFO, "Meta action not supported on this server version.");
          break;
        }
        try {
          final boolean result = DeluxeMenus.getInstance().getPersistentMetaHandler().setMeta(player, executable);
          if (!result) {
            DeluxeMenus.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid meta action! Make sure you have the right syntax.");
            break;
          }
        } catch (final NumberFormatException exception) {
          DeluxeMenus.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid integer value for meta action!");
        }
        break;

      case PLAYER:
        player.chat("/" + executable);
        break;

      case PLAYER_COMMAND_EVENT:
        Bukkit.getPluginManager().callEvent(new PlayerCommandPreprocessEvent(player, "/" + executable));
        break;

      case PLACEHOLDER:
        PlaceholderAPI.setPlaceholders(player, executable);
        break;

      case CHAT:
        player.chat(executable);
        break;

      case CONSOLE:
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executable);
        break;

      case MINI_MESSAGE:
        plugin.adventure().player(player).sendMessage(MiniMessage.miniMessage().deserialize(executable));
        break;

      case MINI_BROADCAST:
        plugin.adventure().all().sendMessage(MiniMessage.miniMessage().deserialize(executable));
        break;

      case MESSAGE:
        player.sendMessage(StringUtils.color(executable));
        break;

      case BROADCAST:
        Bukkit.broadcastMessage(StringUtils.color(executable));
        break;

      case CLOSE:
        Menu.closeMenu(player, true, true);
        break;

      case OPEN_GUI_MENU:
      case OPEN_MENU:
        final Optional<Menu> menuToOpen = Menu.getMenuByName(executable);
        if (menuToOpen.isEmpty()) {
          DeluxeMenus.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find and open menu " + executable);
          break;
        }

        if (holder.isEmpty()) {
          menuToOpen.get().openMenu(player);
          break;
        }

        menuToOpen.get().openMenu(player, holder.get().getTypedArgs(), holder.get().getPlaceholderPlayer());
        break;

      case CONNECT:
        DeluxeMenus.getInstance().connect(player, executable);
        break;

      case JSON_MESSAGE:
        AdventureUtils.sendJson(player, executable);
        break;

      case JSON_BROADCAST:
      case BROADCAST_JSON:
        plugin.adventure().all().sendMessage(AdventureUtils.fromJson(executable));
        break;

      case REFRESH:
        if (holder.isEmpty()) {
          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              player.getName() + " does not have menu open! Nothing to refresh!"
          );
          break;
        }

        holder.get().refreshMenu();
        break;

      case TAKE_MONEY:
        if (DeluxeMenus.getInstance().getVault() == null || !DeluxeMenus.getInstance().getVault().hooked()) {
          DeluxeMenus.debug(DebugLevel.HIGHEST, Level.WARNING, "Vault not hooked! Cannot take money!");
          break;
        }

        try {
          DeluxeMenus.getInstance().getVault().takeMoney(player, Double.parseDouble(executable));
        } catch (final NumberFormatException exception) {
          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Amount for take money action: " + executable + ", is not a valid number!"
          );
        }
        break;

      case GIVE_MONEY:
        if (DeluxeMenus.getInstance().getVault() == null || !DeluxeMenus.getInstance().getVault().hooked()) {
          DeluxeMenus.debug(DebugLevel.HIGHEST, Level.WARNING, "Vault not hooked! Cannot give money!");
          break;
        }

        try {
          DeluxeMenus.getInstance().getVault().giveMoney(player, Double.parseDouble(executable));
        } catch (final NumberFormatException exception) {
          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Amount for give money action: " + executable + ", is not a valid number!"
          );
        }
        break;

      case TAKE_EXP:
      case GIVE_EXP:
        final String lowerCaseExecutable = executable.toLowerCase();

        try {
          if (Integer.parseInt(lowerCaseExecutable.replaceAll("l", "")) <= 0) break;

          if (actionType == ActionType.TAKE_EXP) {
              ExpUtils.setExp(player, "-" + lowerCaseExecutable);
              break;
          }

          ExpUtils.setExp(player, lowerCaseExecutable);
          break;

        } catch (final NumberFormatException exception) {
          if (actionType == ActionType.TAKE_EXP) {
            DeluxeMenus.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Amount for take exp action: " + executable + ", is not a valid number!"
            );
            break;
          }

          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Amount for give exp action: " + executable + ", is not a valid number!"
          );
          break;
        }

      case GIVE_PERM:
        if (DeluxeMenus.getInstance().getVault() == null || !DeluxeMenus.getInstance().getVault().hooked()) {
          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Vault not hooked! Cannot give permission: " + executable + "!");
          break;
        }

        DeluxeMenus.getInstance().getVault().givePermission(player, executable);
        break;

      case TAKE_PERM:
        if (DeluxeMenus.getInstance().getVault() == null || !DeluxeMenus.getInstance().getVault().hooked()) {
          DeluxeMenus.debug(
              DebugLevel.HIGHEST,
              Level.WARNING,
              "Vault not hooked! Cannot take permission: " + executable + "!");
          break;
        }

        DeluxeMenus.getInstance().getVault().takePermission(player, executable);
        break;

      case BROADCAST_SOUND:
      case BROADCAST_WORLD_SOUND:
      case PLAY_SOUND:
        final Sound sound;
        float volume = 1;
        float pitch = 1;

        if (!executable.contains(" ")) {
          try {
            sound = Sound.valueOf(executable.toUpperCase());
          } catch (final IllegalArgumentException exception) {
            DeluxeMenus.printStacktrace(
                "Sound name given for sound action: " + executable + ", is not a valid sound!",
                exception
            );
            break;
          }
        } else {
          String[] parts = executable.split(" ", 3);

          try {
            sound = Sound.valueOf(parts[0].toUpperCase());
          } catch (final IllegalArgumentException exception) {
            DeluxeMenus.printStacktrace(
                "Sound name given for sound action: " + parts[0] + ", is not a valid sound!",
                exception
            );
            break;
          }

          if (parts.length == 3) {
            try {
              pitch = Float.parseFloat(parts[2]);
            } catch (final NumberFormatException exception) {
              DeluxeMenus.debug(
                  DebugLevel.HIGHEST,
                  Level.WARNING,
                  "Pitch given for sound action: " + parts[2] + ", is not a valid number!"
              );

              DeluxeMenus.printStacktrace(
                  "Pitch given for sound action: " + parts[2] + ", is not a valid number!",
                  exception
              );
            }
          }


          try {
            volume = Float.parseFloat(parts[1]);
          } catch (final NumberFormatException exception) {
            DeluxeMenus.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Volume given for sound action: " + parts[1] + ", is not a valid number!"
            );

            DeluxeMenus.printStacktrace(
                "Volume given for sound action: " + parts[1] + ", is not a valid number!",
                exception
            );
          }
        }

        switch (actionType) {
          case BROADCAST_SOUND:
            for (final Player target : Bukkit.getOnlinePlayers()) {
              target.playSound(target.getLocation(), sound, volume, pitch);
            }
            break;

          case BROADCAST_WORLD_SOUND:
            for (final Player target : player.getWorld().getPlayers()) {
              target.playSound(target.getLocation(), sound, volume, pitch);
            }
            break;

          case PLAY_SOUND:
            player.playSound(player.getLocation(), sound, volume, pitch);
            break;
        }
        break;

      default:
        break;
    }
  }
}