package com.extendedclip.deluxemenus.updatechecker;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.listener.Listener;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import com.extendedclip.deluxemenus.utils.Messages;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class UpdateChecker extends Listener {

  private static final TextReplacementConfig.Builder LATEST_VERSION_REPLACER_BUILDER
      = TextReplacementConfig.builder().matchLiteral("<latest-version>");
  private static final TextReplacementConfig.Builder CURRENT_VERSION_REPLACER_BUILDER
      = TextReplacementConfig.builder().matchLiteral("<current-version>");

  final int resourceId = 11734;
  private String latestVersion = null;
  private boolean updateAvailable = false;

  public UpdateChecker(final @NotNull DeluxeMenus instance) {
    super(instance);

    new BukkitRunnable() {
      @Override
      public void run() {
        if (check()) {
          new BukkitRunnable() {

            @Override
            public void run() {
              register();
            }
          }.runTask(plugin);
        }
      }

    }.runTaskAsynchronously(plugin);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(final @NotNull PlayerJoinEvent event) {
    Player player = event.getPlayer();

    if (!player.isOp()) {
      return;
    }

    if (!updateAvailable) {
      return;
    }

    plugin.sms(
        player,
        Messages.UPDATE_AVAILABLE.message().replaceText(
            CURRENT_VERSION_REPLACER_BUILDER.replacement(plugin.getDescription().getVersion()).build()
        ).replaceText(
            LATEST_VERSION_REPLACER_BUILDER.replacement(getLatestVersion()).build()
        )
    );
  }

  private String getSpigotVersion() {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(
          "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("GET");
      return new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
    } catch (Exception ex) {
      plugin.debug(
          DebugLevel.HIGH,
          Level.INFO,
          "Failed to check for update on spigot!"
      );
    }
    return null;
  }

  public boolean check() {
    String version = getSpigotVersion();
    if (version == null) {
      return false;
    }

    if (checkHigher(plugin.getDescription().getVersion(), version)) {
      latestVersion = version;
      updateAvailable = true;
      return true;
    }

    latestVersion = plugin.getDescription().getVersion();
    updateAvailable = false;
    return false;
  }

  public boolean updateAvailable() {
    return updateAvailable;
  }

  public String getLatestVersion() {
    return latestVersion;
  }

  private boolean checkHigher(final @NotNull String currentVersion, final @NotNull String newVersion) {
    return toReadable(currentVersion).compareTo(toReadable(newVersion)) < 0;
  }

  private String toReadable(final @NotNull String version) {
    String[] split = Pattern.compile(".", Pattern.LITERAL).split(version.replace("v", ""));
    StringBuilder versionBuilder = new StringBuilder();
    for (String s : split) {
      versionBuilder.append(String.format("%4s", s));
    }

    return versionBuilder.toString();
  }

}
