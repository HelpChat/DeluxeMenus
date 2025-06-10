package com.extendedclip.deluxemenus.action;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ActionType {
  META("[meta]", "Handle meta for a player",
      "- '[meta] <set/remove/add/subtract/switch> <key> <value type> <value>'"),
  CONSOLE("[console]", "Execute a command from the console",
      "- '[console] <command with no slash>'"),
  PLAYER("[player]", "Execute a command for the menu viewer",
      "- '[player] <command with no slash>'"),
  PLAYER_COMMAND_EVENT("[commandevent]",
      "Fire a PlayerCommandPreprocessEvent for commands that do not use the bukkit command system",
      "- '[commandevent] <command with no slash>'"),
  MINI_MESSAGE("[minimessage]", "Send a message to the menu viewer using the minimessage style",
      "- '[minimessage] <message>'"),
  MINI_BROADCAST("[minibroadcast]", "Broadcast a message to the server using the minimessage style",
      "- '[minibroadcast] <message>'"),
  MESSAGE("[message]", "Send a message to the menu viewer",
      "- [message] <message>"),
  LOG("[log]", "Log a message to the console", "- [log] <level> <message>"),
  BROADCAST("[broadcast]", "Broadcast a message to the server", "- '[broadcast] <message>"),
  CHAT("[chat]", "Send a chat message as the player performing the action", "- '[chat] <message>"),
  OPEN_GUI_MENU("[openguimenu]", "Open a GUI menu", "- '[openguimenu] <menu-name>'"),
  OPEN_MENU("[openmenu]", "Open a GUI menu", "- '[openmenu] <menu-name>'"),
  CONNECT("[connect]", "Connect to the specified bungee server", "- '[connect] <serverName>'"),
  CLOSE("[close]", "Close the viewers open menu", "- '[close]"),
  REFRESH("[refresh]", "Refresh items in the current menu view", "- '[refresh]"),
  BROADCAST_SOUND("[broadcastsound]", "Broadcast a sound to the server", "- '[broadcastsound]"),
  BROADCAST_WORLD_SOUND("[broadcastsoundworld]", "Broadcast a sound to the player's world", "- '[broadcastsoundworld]"),
  PLAY_SOUND("[sound]", "Play a sound for a the specific player", "- '[sound]"),
  TAKE_MONEY("[takemoney]", "Take money from a player (requires Vault)", "- '[takemoney] <amount>"),
  GIVE_MONEY("[givemoney]", "Give money to a player (requires Vault)", "- '[givemoney] <amount>"),
  TAKE_EXP("[takeexp]", "Take exp points/levels from a player", "- '[takeexp] <amount>L'"),
  GIVE_EXP("[giveexp]", "Give exp points/levels to a player", "- '[giveexp] <amount>L'"),
  TAKE_PERM("[takepermission]", "Take a permission from a player (requires Vault)", "- '[takepermission] <permission>'"),
  GIVE_PERM("[givepermission]", "Give a permission to a player (requires Vault)", "- '[givepermission] <permission>'"),
  JSON_MESSAGE("[json]", "Send a json message to the menu viewer",
      "- '[json] {\"text\":\"message\"}'"),
  JSON_BROADCAST("[jsonbroadcast]", "Broadcast a json message to all online players",
      "- '[jsonbroadcast] {\"text\":\"message\"}'"),
  BROADCAST_JSON("[broadcastjson]", "Broadcast a json message to all online players",
      "- '[broadcastjson] {\"text\":\"message\"}'"),
  PLACEHOLDER("[placeholder]", "Parse placeholders for a player without any chat or console output",
      "- '[placeholder] %placeholder%'");

  private static final Map<String, ActionType> BY_NAME = Arrays.stream(values())
      .collect(Collectors.toMap(e -> e.name().toUpperCase(Locale.ROOT), Function.identity()));

  private final String identifier;
  private final String description;
  private final String usage;

  ActionType(@NotNull final String identifier, @NotNull final String description, @NotNull final String usage) {
    this.identifier = identifier;
    this.description = description;
    this.usage = usage;
  }

  /**
   * Get an {@link ActionType} by its name.
   *
   * @param name The name of the action type.
   * @return The {@link ActionType} or null if it does not exist.
   */
  public static @Nullable ActionType getByName(@NotNull final String name) {
    return BY_NAME.get(name.toUpperCase(Locale.ROOT));
  }

  /**
   * Get an {@link ActionType} by its identifier.
   *
   * @param identifier The identifier of the action type.
   * @return The {@link ActionType} or null if it does not exist.
   */
  public static @Nullable ActionType getByIdentifier(@NotNull final String identifier) {
    return BY_NAME.values().stream()
        .filter(e -> e.identifier.equalsIgnoreCase(identifier))
        .findFirst()
        .orElse(null);
  }

  /**
   * Get an {@link ActionType} from the start of the string.
   * <br>
   * This is used for passing the executable directly instead of the action name.
   *
   * @param string The string to get the action from.
   * @return The {@link ActionType} or null if it does not exist.
   */
  public static @Nullable ActionType getByStart(@NotNull final String string) {
    return BY_NAME.values().stream().map(ActionType::getIdentifier).filter(string::startsWith).findFirst()
        .map(ActionType::getByIdentifier).orElse(null);
  }

  /**
   * Get a formatted string listing all action types, their description and their usage.
   *
   * @return The list of actions in a string.
   */
  public static String listAllActionTypes() {
    final StringBuilder builder = new StringBuilder();

    for (final ActionType type : BY_NAME.values()) {
      builder
          .append("\n")
          .append(type.getIdentifier()).append(" - ").append(type.getDescription())
          .append("\n")
          .append("Usage: ").append(type.getUsage())
          .append("\n");
    }

    return builder.toString();
  }

  public @NotNull String getIdentifier() {
    return identifier;
  }

  public @NotNull String getDescription() {
    return description;
  }

  public @NotNull String getUsage() {
    return usage;
  }

}
