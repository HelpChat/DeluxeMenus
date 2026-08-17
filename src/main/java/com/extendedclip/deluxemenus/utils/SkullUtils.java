package com.extendedclip.deluxemenus.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.extendedclip.deluxemenus.DeluxeMenus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.UUID;

public class SkullUtils {

    private static final String TEXTURES_PROPERTY = "textures";
    private static final String TEXTURE_URL_PREFIX = "https://textures.minecraft.net/texture/";

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
                .format("{%s:{SKIN:{url:\"%s\"}}}", TEXTURES_PROPERTY, TEXTURE_URL_PREFIX + url)
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

        final PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty(TEXTURES_PROPERTY, base64Url));
        headMeta.setPlayerProfile(profile);

        head.setItemMeta(headMeta);
        return head;
    }

    /**
     * Get the texture id of a skull, i.e. the trailing path segment of its skin url.
     *
     * @return the texture id, or {@code null} if the item is not a skull or carries no texture
     */
    public static @Nullable String getTextureFromSkull(@NotNull final ItemStack item) {
        if (!(item.getItemMeta() instanceof SkullMeta meta)) return null;

        final PlayerProfile profile = meta.getPlayerProfile();
        if (profile == null) return null;

        for (final ProfileProperty property : profile.getProperties()) {
            if (TEXTURES_PROPERTY.equals(property.getName())) {
                return getTextureIdFromBase64(property.getValue());
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
        final PlayerProfile profile = offlinePlayer.getPlayerProfile();

        if (!profile.hasTextures()) {
            // updates the Player Profile and populates textures for offline players - for some reason this doesn't populate when getting the Profile first time
            headMeta.setPlayerProfile(profile.update().join());
        } else {
            headMeta.setOwningPlayer(offlinePlayer);
        }

        head.setItemMeta(headMeta);
        return head;
    }

    public static String getSkullOwner(ItemStack skull) {
        if (skull == null || !(skull.getItemMeta() instanceof SkullMeta)) return null;
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta.getOwningPlayer() == null) return null;
        return meta.getOwningPlayer().getName();
    }

    /**
     * Extract the texture id from a base64 encoded texture blob.
     *
     * @return the texture id, or {@code null} if the blob carries no skin url
     */
    public static @Nullable String getTextureIdFromBase64(@NotNull final String base64Texture) {
        final String url = decodeSkinUrl(base64Texture);
        if (url == null) {
            return null;
        }

        return url.substring(url.lastIndexOf('/') + 1);
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
