package com.extendedclip.deluxemenus.hooks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultHook {
  private final Economy economy;
  private final Permission permission;

  public VaultHook() {
    final RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServer().getServicesManager()
        .getRegistration(Economy.class);
    final RegisteredServiceProvider<Permission> rspPermissions = Bukkit.getServer().getServicesManager()
        .getRegistration(Permission.class);

    economy = rspEconomy == null ? null : rspEconomy.getProvider();
    permission = rspPermissions == null ? null : rspPermissions.getProvider();
  }

  /**
   * Checks if the Economy and Permission hooks are enabled.
   *
   * @return true if both hooks are enabled, false otherwise.
   */
  public boolean hooked() {
    return economy != null && permission != null;
  }

  /**
   * Checks if the player has the amount in their account.
   *
   * @param player the player to check.
   * @param amount the amount to check for.
   * @return true if the economy hook is enabled and player has the amount, false otherwise.
   */
  public boolean hasEnough(@NotNull final Player player, final double amount) {
    return economy != null && economy.has(player, amount);
  }

  /**
   * Takes the amount from the player's account.
   * <br>
   * This will do nothing if the economy hook is disabled. You should check {@link #hooked()} before calling this.
   *
   * @param player the player to take from.
   * @param amount the amount to take.
   */
  public void takeMoney(@NotNull final Player player, final double amount) {
    if (economy == null) return;
    economy.withdrawPlayer(player, amount);
  }

  /**
   * Gives the player the amount.
   * <br>
   * This will do nothing if the economy hook is disabled. You should check {@link #hooked()} before calling this.
   *
   * @param player the player to give to.
   * @param amount the amount to give.
   */
  public void giveMoney(@NotNull final Player player, final double amount) {
    if (economy == null) return;
    economy.depositPlayer(player, amount);
  }

  /**
   * Checks if the player has the permission.
   *
   * @param player the player to check.
   * @param permissionNode the permission to check for.
   * @return true if the permission hook is enabled and player has the permission, false otherwise.
   */
  public boolean hasPermission(@NotNull final Player player, @NotNull final String permissionNode) {
    return this.permission != null && this.permission.has(player, permissionNode);
  }

  /**
   * Take the permission from the player.
   * <br>
   * This will do nothing if the permission hook is disabled. You should check {@link #hooked()} before calling this.
   *
   * @param player the player to take from.
   * @param permissionNode the permission to take.
   */
  public void takePermission(@NotNull final Player player, @NotNull final String permissionNode) {
    if (permission == null) return;
    permission.playerRemove(null, player, permissionNode);
  }

  /**
   * Give the player the permission.
   * <br>
   * This will do nothing if the permission hook is disabled. You should check {@link #hooked()} before calling this.
   *
   * @param player the player to give to.
   * @param permissionNode the permission to give.
   */
  public void givePermission(@NotNull final Player player, @NotNull final String permissionNode) {
    if (permission == null) return;
    permission.playerAdd(null, player, permissionNode);
  }
}