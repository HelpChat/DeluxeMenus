package com.extendedclip.deluxemenus.utils;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class SkullUtils {

    private static final Gson GSON = new Gson();

    /**
     * Helper method to get the encoded bytes for a full MC Texture
     *
     * @param url the url of the texture
     * @return fully encoded texture url
     */
    @NotNull
    public static String getEncoded(@NotNull final String url) {
        final byte[] encodedData = Base64.getEncoder().encode(String
                .format("{textures:{SKIN:{url:\"%s\"}}}", "https://textures.minecraft.net/texture/" + url)
                .getBytes());
        return new String(encodedData);
    }

    /**
     * Get the skull from a base64 encoded texture url
     *
     * @param base64Url base64 encoded url to use
     * @return skull
     */
    @NotNull
    public static ItemStack getSkullByBase64EncodedTextureUrl(@NotNull final DeluxeMenus plugin, @NotNull final String base64Url) {
        final ItemStack head = plugin.getHead().clone();
        if (base64Url.isEmpty()) {
            return head;
        }

        final SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) {
            return head;
        }

        if (VersionHelper.HAS_PLAYER_PROFILES) {
            final PlayerProfile profile = getPlayerProfile(plugin, base64Url);
            headMeta.setOwnerProfile(profile);
            head.setItemMeta(headMeta);
            return head;
        }

        final GameProfile profile = getGameProfile(base64Url);
        final Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (final NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            plugin.printStacktrace(
                    "Failed to get head item from base64 texture url",
                    exception
            );
        }
        head.setItemMeta(headMeta);
        return head;
    }

    public static String getTextureFromSkull(final DeluxeMenus plugin, ItemStack item) {
        if (!(item.getItemMeta() instanceof SkullMeta)) return null;
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (VersionHelper.HAS_PLAYER_PROFILES) {
            PlayerProfile profile = meta.getOwnerProfile();
            if (profile == null) return null;

            URL url = profile.getTextures().getSkin();
            if (url == null) return null;

            return url.toString().substring("https://textures.minecraft.net/texture/".length() - 1);
        }

        GameProfile profile;
        try {
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profile = (GameProfile) profileField.get(meta);
        } catch (final NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            plugin.printStacktrace(
                    "Failed to get base64 texture url from head item",
                    exception
            );
            return null;
        }

        for (Property property : profile.getProperties().get("textures")) {
            if (property.getName().equals("textures")) {
                return decodeSkinUrl(property.getValue());
            }
        }
        return null;
    }


    /**
     * Get the skull from a player name
     *
     * @param playerName the player name to use
     * @return skull
     */
    @NotNull
    public static ItemStack getSkullByName(@NotNull final DeluxeMenus plugin, @NotNull final String playerName) {
        final ItemStack head = plugin.getHead().clone();
        if (playerName.isEmpty()) {
            return head;
        }

        final SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) {
            return head;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (VersionHelper.HAS_PLAYER_PROFILES && offlinePlayer.getPlayerProfile().getTextures().isEmpty()) {
            // updates the Player Profile and populates textures for offline players - for some reason this doesn't populate when getting the Profile first time
            headMeta.setOwnerProfile(offlinePlayer.getPlayerProfile().update().join());
        } else if (!VersionHelper.IS_SKULL_OWNER_LEGACY) {
            headMeta.setOwningPlayer(offlinePlayer);
        } else {
            headMeta.setOwner(offlinePlayer.getName());
        }

        head.setItemMeta(headMeta);
        return head;
    }

    public static String getSkullOwner(ItemStack skull) {
        if (skull == null || !(skull.getItemMeta() instanceof SkullMeta)) return null;
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (!VersionHelper.IS_SKULL_OWNER_LEGACY) {
            if (meta.getOwningPlayer() == null) return null;
            return meta.getOwningPlayer().getName();
        }

        return meta.getOwner();
    }

    /**
     * Create a game profile object
     *
     * @param base64Url the base64 encoded texture url to use
     * @return game profile
     */
    @NotNull
    private static GameProfile getGameProfile(@NotNull final String base64Url) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64Url));
        return profile;
    }

    /**
     * Create a player profile object
     * Player profile was introduced in 1.18.1+
     *
     * @param base64Url the base64 encoded texture URL to use
     * @return player profile
     */
    @NotNull
    private static PlayerProfile getPlayerProfile(@NotNull final DeluxeMenus plugin, @NotNull final String base64Url) {
        final PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

        final String decodedBase64 = decodeSkinUrl(base64Url);
        if (decodedBase64 == null) {
            return profile;
        }

        final PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL(decodedBase64));
        } catch (final MalformedURLException exception) {
            plugin.printStacktrace("Something went horribly wrong trying to create basehead URL", exception);
        }

        profile.setTextures(textures);
        return profile;
    }

    /**
     * Decode a base64 string and extract the url of the skin. Example:
     * <br>
     * - Base64: {@code eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNlYjE3MDhkNTQwNGVmMzI2MTAzZTdiNjA1NTljOTE3OGYzZGNlNzI5MDA3YWM5YTBiNDk4YmRlYmU0NjEwNyJ9fX0=}
     * <br>
     * - JSON: {@code {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107"}}}}
     * <br>
     * - Result: {@code http://textures.minecraft.net/texture/dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107}
     * <br>
     * Credit: <a href="https://github.com/TriumphTeam/triumph-gui/pull/104/files#diff-ef6f3ffdac8e5f722e2e9121be8003b26d087c2d7871ca43d31b65c7565b0c1fR92">iGabyTM</a>
     *
     * @param base64Texture the texture
     * @return the url of the texture if found, otherwise {@code null}
     */
    @Nullable
    public static String decodeSkinUrl(@NotNull final String base64Texture) {
        final String decoded = new String(Base64.getDecoder().decode(base64Texture));
        final JsonObject object = GSON.fromJson(decoded, JsonObject.class);

        final JsonElement textures = object.get("textures");

        if (textures == null) {
            return null;
        }

        final JsonElement skin = textures.getAsJsonObject().get("SKIN");

        if (skin == null) {
            return null;
        }

        final JsonElement url = skin.getAsJsonObject().get("url");
        return url == null ? null : url.getAsString();
    }
}
