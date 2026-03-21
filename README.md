[logo]: https://github.com/HelpChat/DeluxeMenus/assets/52609756/f24ac57d-98db-4d57-a723-791a2654e73f

[issues]: https://github.com/HelpChat/DeluxeMenus/issues
[licenseImg]: https://img.shields.io/github/license/helpchat/deluxemenus?&logo=github
[license]: https://github.com/HelpChat/DeluxeMenus/blob/master/LICENSE

[bstatsImg]: https://img.shields.io/bstats/servers/445
[bstats]: https://bstats.org/plugin/bukkit/DeluxeMenus/445

[discordImg]: https://img.shields.io/discord/164280494874165248?color=5562e9&logo=discord&logoColor=white
[discord]: https://helpch.at/discord
[spigot]: https://www.spigotmc.org/resources/11734/

[ci]: http://ci.extendedclip.com/job/DeluxeMenus/
[ciImg]: http://ci.extendedclip.com/buildStatus/icon?job=DeluxeMenus

[contributing]: https://github.com/HelpChat/DeluxeMenus/blob/main/CONTRIBUTING.md

[![logo]][spigot]

[![ciImg]][ci] [![bstatsImg]][bstats] [![discordImg]][discord] [![licenseImg]][license] [![GitBook](https://img.shields.io/static/v1?message=Documented%20on%20GitBook&logo=gitbook&logoColor=ffffff&label=%20&labelColor=5c5c5c&color=3F89A1)](https://wiki.helpch.at/helpchat-plugins/deluxemenus)


# Information
[DeluxeMenus][spigot] is the all in one inventory GUI menu plugin!
You can create GUI menus that open with custom commands that will show stats or perform actions specific to the player who opened it. Your menus are fully configurable. You can create menus that show specific items to different players, or perform different actions depending on what javascript requirement they have for the specific slot in a certain GUI.

DeluxeMenus depends on [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

## Fork Changes

### New Feature: `open_gui` — Direct Menu Switching (Category Navigation)

This fork adds a new `open_gui` item property that allows menu items to directly open another menu when clicked — no `click_commands` needed. This makes it simple to build **category-style navigation** where players can switch between menus like tabs.

#### Usage

Add `open_gui` to any menu item, pointing to the target menu name:

```yaml
items:
  'pvp_tab':
    material: DIAMOND_SWORD
    slot: 2
    display_name: '&c&lPvP'
    open_gui: 'pvp_menu'
  'kits_tab':
    material: CHEST
    slot: 4
    display_name: '&6&lKits'
    open_gui: 'kits_menu'
```

#### How it works

- When clicked, the item instantly opens the target menu
- **Supports PlaceholderAPI** placeholders and arguments in the menu name
- **Passes current arguments** from the source menu to the target menu automatically
- Takes **priority over `click_commands`** — if `open_gui` is set and the target menu exists, it opens directly
- Logs a warning to console if the target menu is not found

#### Files changed

| File | Change |
|------|--------|
| `MenuItemOptions.java` | Added `openGui` field, getter, and builder method |
| `DeluxeMenusConfig.java` | Parses `open_gui` from YAML item config |
| `PlayerListener.java` | Handles `open_gui` on inventory click with placeholder/argument support |

#### Example menus

See the included example files demonstrating a full category system:
- `category_menu_example.yml` — Main menu with 3 category tabs
- `pvp_menu.yml` — PvP category (2 items)
- `kits_menu.yml` — Kits category (1 item)
- `settings_menu.yml` — Settings category (2 items)

---

## Contribute
If you would like to contribute towards DeluxeMenus should you take a look at our [Contributing file][contributing] for the ins and outs on how you can do that and what you need to keep in mind.

## Support
- [Issue Tracker][issues]
- [Discord Support][discord]

## Quick Links
- [Wiki](https://wiki.helpch.at/clips-plugins/deluxemenus/)
- [CI Server][ci]
- [Spigot Page][spigot]
- [Plugin Statistics][bstats]

