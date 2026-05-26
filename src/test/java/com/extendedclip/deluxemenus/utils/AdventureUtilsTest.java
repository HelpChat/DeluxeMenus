package com.extendedclip.deluxemenus.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdventureUtilsTest {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    @Test
    void parsesLegacyAndMiniMessageFormattingTogether() {
        assertEquals("§aHello §lworld", LEGACY_SERIALIZER.serialize(AdventureUtils.fromFormattedText("&aHello <bold>world</bold>")));
    }

    @Test
    void resetsLegacyFormattingWhenAColorChanges() {
        final Component expected = text()
                .append(text("Bold ").decorate(TextDecoration.BOLD))
                .append(text("Green", NamedTextColor.GREEN))
                .build();

        assertEquals(expected, AdventureUtils.fromFormattedText("&lBold &aGreen"));
    }

    @Test
    void keepsUnknownTagsAsPlainText() {
        assertEquals(text("Use <menu> here"), AdventureUtils.fromFormattedText("Use <menu> here"));
    }

    @Test
    void parsesLegacyHexColors() {
        assertEquals(text("Hex", TextColor.color(0x12AB34)), AdventureUtils.fromFormattedText("&#12AB34Hex"));
    }

    @Test
    void fullMiniMessageParserKeepsInteractiveTags() {
        final Component expected = text("Click", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/test"));

        assertEquals(expected, AdventureUtils.fromMiniMessage("&a<click:run_command:'/test'>Click</click>"));
    }
}