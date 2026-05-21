package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WebEditorServer {

    private final DeluxeMenus plugin;
    private final MenuConfigEditor configEditor;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private HttpServer server;

    public WebEditorServer(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
        this.configEditor = new MenuConfigEditor(plugin);
    }

    public @NotNull String createSession(
            final @NotNull Menu menu,
            final int requestedPort,
            final @Nullable String requestedHost
    ) throws IOException {
        ensureStarted(requestedPort);

        final String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(token, menu.options().name(), Instant.now().plus(Duration.ofMinutes(60))));

        return "http://" + publicHost(requestedHost) + ":" + server.getAddress().getPort() + "/dm-web/" + token;
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        sessions.clear();
    }

    private void ensureStarted(final int requestedPort) throws IOException {
        if (server != null) {
            if (server.getAddress().getPort() == requestedPort) {
                return;
            }

            stop();
        }

        server = HttpServer.create(new InetSocketAddress(requestedPort), 0);
        server.createContext("/dm-web", this::handle);
        server.setExecutor(null);
        server.start();
    }

    private void handle(final @NotNull HttpExchange exchange) throws IOException {
        final String path = exchange.getRequestURI().getPath();
        final String[] parts = path.split("/");
        if (parts.length < 3) {
            send(exchange, 404, "Not found", "text/plain");
            return;
        }

        final Optional<Session> optionalSession = getSession(parts[2]);
        if (optionalSession.isEmpty()) {
            send(exchange, 403, "Invalid or expired editor session.", "text/plain");
            return;
        }

        final Session session = optionalSession.get();
        final String action = parts.length >= 4 ? parts[3] : "";
        final String method = exchange.getRequestMethod();

        if ("save-menu".equals(action) && "POST".equalsIgnoreCase(method)) {
            saveMenu(exchange, session);
            return;
        }

        if ("save-item".equals(action) && "POST".equalsIgnoreCase(method)) {
            saveItem(exchange, session);
            return;
        }

        if ("delete-item".equals(action) && "POST".equalsIgnoreCase(method)) {
            deleteItem(exchange, session);
            return;
        }

        if ("save-raw".equals(action) && "POST".equalsIgnoreCase(method)) {
            saveRaw(exchange, session);
            return;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            send(exchange, 405, "Method not allowed", "text/plain");
            return;
        }

        render(exchange, session);
    }

    private void render(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = findMenu(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final Menu menu = optionalMenu.get();
        final Map<String, String> query = parseForm(exchange.getRequestURI().getRawQuery());
        final int selectedSlot = clamp(parseInt(query.get("slot")), 0, Math.max(0, menu.options().size() - 1));
        final String saved = query.getOrDefault("saved", "");
        final String html = "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>DeluxeMenus Editor</title>"
                + style()
                + "</head><body>"
                + "<header><div><strong>DeluxeMenus Editor</strong><span>" + escape(menu.options().name()) + "</span></div>"
                + "<a href=\"/dm-web/" + escape(session.token) + "?slot=" + selectedSlot + "\">Refresh</a></header>"
                + "<main>"
                + "<section class=\"preview\"><div class=\"section-title\"><h2>" + escape(menu.options().name()) + "</h2><span>"
                + menu.options().size() + " slots</span></div>"
                + "<div class=\"grid\">" + renderGrid(menu, session, selectedSlot) + "</div>"
                + status(saved)
                + "</section>"
                + "<section class=\"editor\">"
                + renderMenuForm(menu, session, selectedSlot)
                + renderItemForm(menu, session, selectedSlot)
                + renderRawForm(menu, session, selectedSlot)
                + "</section>"
                + "</main>"
                + script()
                + "</body></html>";
        send(exchange, 200, html, "text/html; charset=utf-8");
    }

    private void saveMenu(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = findMenu(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final Map<String, String> form = parseForm(readBody(exchange));
        final Menu menu = optionalMenu.get();
        configEditor.setMenuValue(menu, "menu_title", form.getOrDefault("menu_title", menu.options().title()));
        configEditor.setMenuValue(menu, "size", form.getOrDefault("size", String.valueOf(menu.options().size())));
        reload(menu);
        redirect(exchange, session, parseInt(form.get("slot")), "menu");
    }

    private void saveRaw(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = findMenu(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final Map<String, String> form = parseForm(readBody(exchange));
        final Menu menu = optionalMenu.get();
        configEditor.saveRaw(menu, form.getOrDefault("content", ""));
        reload(menu);
        redirect(exchange, session, parseInt(form.get("slot")), "raw");
    }

    private void saveItem(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = findMenu(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final Map<String, String> form = parseForm(readBody(exchange));
        final int slot = parseInt(form.get("slot"));
        final Menu menu = optionalMenu.get();
        final String material = form.getOrDefault("material", "").isBlank() ? "STONE" : form.get("material");
        configEditor.setItemValue(menu, slot, "material", material);
        configEditor.setItemValue(menu, slot, "amount", form.getOrDefault("amount", "-1"));
        configEditor.setItemValue(menu, slot, "priority", form.getOrDefault("priority", "1"));
        configEditor.setItemValue(menu, slot, "display_name", form.getOrDefault("display_name", ""));
        configEditor.setItemValue(menu, slot, "lore", form.getOrDefault("lore", ""));
        configEditor.setItemValue(menu, slot, "model_data", form.getOrDefault("model_data", ""));
        configEditor.setItemValue(menu, slot, "item_flags", form.getOrDefault("item_flags", ""));
        configEditor.setItemValue(menu, slot, "update", form.containsKey("update") ? "true" : "false");
        configEditor.setItemValue(menu, slot, "click_commands", form.getOrDefault("click_commands", ""));
        configEditor.setItemValue(menu, slot, "left_click_commands", form.getOrDefault("left_click_commands", ""));
        configEditor.setItemValue(menu, slot, "right_click_commands", form.getOrDefault("right_click_commands", ""));
        configEditor.setItemValue(menu, slot, "shift_left_click_commands", form.getOrDefault("shift_left_click_commands", ""));
        configEditor.setItemValue(menu, slot, "shift_right_click_commands", form.getOrDefault("shift_right_click_commands", ""));
        configEditor.setItemValue(menu, slot, "middle_click_commands", form.getOrDefault("middle_click_commands", ""));
        reload(menu);
        redirect(exchange, session, slot, "slot");
    }

    private void deleteItem(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = findMenu(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final Map<String, String> form = parseForm(readBody(exchange));
        final int slot = parseInt(form.get("slot"));
        final Menu menu = optionalMenu.get();
        configEditor.deleteItem(menu, slot);
        reload(menu);
        redirect(exchange, session, slot, "delete");
    }

    private @NotNull Optional<Session> getSession(final @NotNull String token) {
        final Session session = sessions.get(token);
        if (session == null || session.expiresAt.isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }

        return Optional.of(session);
    }

    private @NotNull Optional<Menu> findMenu(final @NotNull String menuName) {
        final Optional<Menu> menu = Menu.getMenuByName(menuName);
        if (menu.isPresent()) {
            return menu;
        }

        return Menu.getSubMenuByName(menuName);
    }

    private @NotNull String renderGrid(final @NotNull Menu menu, final @NotNull Session session, final int selectedSlot) {
        final StringBuilder builder = new StringBuilder();
        for (int slot = 0; slot < menu.options().size(); slot++) {
            final boolean filled = isFilled(menu, slot);
            builder.append("<a class=\"slot")
                    .append(filled ? " filled" : "")
                    .append(slot == selectedSlot ? " selected" : "")
                    .append("\" href=\"/dm-web/")
                    .append(escape(session.token))
                    .append("?slot=")
                    .append(slot)
                    .append("\" title=\"")
                    .append(escape(slotLabel(menu, slot)))
                    .append("\"><span class=\"slot-number\">")
                    .append(slot)
                    .append("</span><span class=\"slot-material\">")
                    .append(escape(compact(slotLabel(menu, slot), 13)))
                    .append("</span></a>");
        }
        return builder.toString();
    }

    private @NotNull String renderMenuForm(final @NotNull Menu menu, final @NotNull Session session, final int selectedSlot) {
        final String source = configEditor.resolveFile(menu).map(File::getPath).orElse("config.yml");
        return "<form class=\"panel menu-form\" method=\"post\" action=\"/dm-web/" + escape(session.token) + "/save-menu\">"
                + "<input type=\"hidden\" name=\"slot\" value=\"" + selectedSlot + "\">"
                + "<div class=\"panel-title\"><h3>Menu</h3><span>" + escape(source) + "</span></div>"
                + input("Title", "menu_title", configEditor.getMenuString(menu, "menu_title").orElse(menu.options().title()))
                + "<label>Size<input type=\"number\" min=\"9\" max=\"54\" step=\"9\" name=\"size\" value=\"" + menu.options().size() + "\"></label>"
                + "<button type=\"submit\">Save Menu</button>"
                + "</form>";
    }

    private @NotNull String renderItemForm(final @NotNull Menu menu, final @NotNull Session session, final int selectedSlot) {
        return "<div class=\"panel\">"
                + "<form id=\"slot-form\" method=\"post\" action=\"/dm-web/" + escape(session.token) + "/save-item\">"
                + "<input type=\"hidden\" name=\"slot\" value=\"" + selectedSlot + "\">"
                + "<div class=\"panel-title\"><h3>Slot " + selectedSlot + "</h3><span>" + escape(slotLabel(menu, selectedSlot)) + "</span></div>"
                + "<div class=\"fields two\">"
                + input("Material", "material", value(menu, selectedSlot, "material", ""))
                + input("Amount", "amount", value(menu, selectedSlot, "amount", "-1"))
                + input("Priority", "priority", value(menu, selectedSlot, "priority", "1"))
                + input("Model Data", "model_data", value(menu, selectedSlot, "model_data", ""))
                + "</div>"
                + input("Display Name", "display_name", value(menu, selectedSlot, "display_name", ""))
                + textarea("Lore", "lore", value(menu, selectedSlot, "lore", ""))
                + textarea("Item Flags", "item_flags", value(menu, selectedSlot, "item_flags", ""))
                + checkbox("Update Placeholders", "update", Boolean.parseBoolean(value(menu, selectedSlot, "update", "false")))
                + "<div class=\"fields two\">"
                + textarea("Click Commands", "click_commands", value(menu, selectedSlot, "click_commands", ""))
                + textarea("Left Click Commands", "left_click_commands", value(menu, selectedSlot, "left_click_commands", ""))
                + textarea("Right Click Commands", "right_click_commands", value(menu, selectedSlot, "right_click_commands", ""))
                + textarea("Shift Left Commands", "shift_left_click_commands", value(menu, selectedSlot, "shift_left_click_commands", ""))
                + textarea("Shift Right Commands", "shift_right_click_commands", value(menu, selectedSlot, "shift_right_click_commands", ""))
                + textarea("Middle Click Commands", "middle_click_commands", value(menu, selectedSlot, "middle_click_commands", ""))
                + "</div>"
                + "<div class=\"actions\"><button type=\"submit\">Save Slot</button></div></form>"
                + "<form class=\"delete-form\" method=\"post\" action=\"/dm-web/" + escape(session.token) + "/delete-item\">"
                + "<input type=\"hidden\" name=\"slot\" value=\"" + selectedSlot + "\">"
                + "<button class=\"danger\" type=\"submit\">Delete Slot</button></form></div>";
    }

    private @NotNull String renderRawForm(final @NotNull Menu menu, final @NotNull Session session, final int selectedSlot) throws IOException {
        return "<details class=\"panel raw\"><summary>Raw YAML</summary>"
                + "<form method=\"post\" action=\"/dm-web/" + escape(session.token) + "/save-raw\">"
                + "<input type=\"hidden\" name=\"slot\" value=\"" + selectedSlot + "\">"
                + "<textarea name=\"content\">" + escape(configEditor.readRaw(menu)) + "</textarea>"
                + "<button class=\"secondary\" type=\"submit\">Save Raw YAML</button></form></details>";
    }

    private @NotNull String input(final @NotNull String label, final @NotNull String name, final @NotNull String value) {
        return "<label>" + escape(label) + "<input name=\"" + escape(name) + "\" value=\"" + escape(value) + "\"></label>";
    }

    private @NotNull String textarea(final @NotNull String label, final @NotNull String name, final @NotNull String value) {
        return "<label>" + escape(label) + "<textarea name=\"" + escape(name) + "\">" + escape(value) + "</textarea></label>";
    }

    private @NotNull String checkbox(final @NotNull String label, final @NotNull String name, final boolean checked) {
        return "<label class=\"check\"><input type=\"checkbox\" name=\"" + escape(name) + "\" value=\"true\""
                + (checked ? " checked" : "") + "><span>" + escape(label) + "</span></label>";
    }

    private @NotNull String status(final @NotNull String saved) {
        if (saved.isBlank()) {
            return "";
        }

        return "<div class=\"notice\">Saved " + escape(saved) + ".</div>";
    }

    private @NotNull String value(final @NotNull Menu menu, final int slot, final @NotNull String option, final @NotNull String fallback) {
        return configEditor.getItemString(menu, slot, option).orElse(fallback);
    }

    private boolean isFilled(final @NotNull Menu menu, final int slot) {
        final Map<Integer, MenuItem> items = menu.getMenuItems().get(slot);
        return items != null && !items.isEmpty();
    }

    private @NotNull String slotLabel(final @NotNull Menu menu, final int slot) {
        final Map<Integer, MenuItem> items = menu.getMenuItems().get(slot);
        if (items == null || items.isEmpty()) {
            return "Empty";
        }

        return items.values().iterator().next().options().material();
    }

    private void reload(final @NotNull Menu menu) {
        plugin.getScheduler().runTask(() -> configEditor.reload(menu));
    }

    private void redirect(final @NotNull HttpExchange exchange, final @NotNull Session session, final int slot, final @NotNull String saved) throws IOException {
        send(exchange, 303, "", "text/plain", "/dm-web/" + session.token + "?slot=" + Math.max(0, slot) + "&saved=" + saved);
    }

    private @NotNull String readBody(final @NotNull HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private @NotNull Map<String, String> parseForm(final @Nullable String body) {
        final Map<String, String> form = new HashMap<>();
        if (body == null || body.isBlank()) {
            return form;
        }

        for (final String part : body.split("&")) {
            final int index = part.indexOf('=');
            if (index <= 0) {
                continue;
            }
            final String formKey = URLDecoder.decode(part.substring(0, index), StandardCharsets.UTF_8);
            form.put(formKey, URLDecoder.decode(part.substring(index + 1), StandardCharsets.UTF_8));
        }
        return form;
    }

    private int parseInt(final @Nullable String input) {
        try {
            return Integer.parseInt(input);
        } catch (final Exception exception) {
            return 0;
        }
    }

    private int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    private @NotNull String compact(final @NotNull String value, final int limit) {
        if (value.length() <= limit) {
            return value;
        }

        return value.substring(0, Math.max(0, limit - 1)) + "...";
    }

    private @NotNull String publicHost(final @Nullable String requestedHost) {
        final Optional<String> normalizedRequestHost = normalizeHost(requestedHost);
        if (normalizedRequestHost.isPresent()) {
            return normalizedRequestHost.get();
        }

        final String configuredHost = plugin.getServer().getIp();
        if (configuredHost != null && !configuredHost.isBlank() && !"0.0.0.0".equals(configuredHost)) {
            return normalizeHost(configuredHost).orElse(configuredHost);
        }

        return "localhost";
    }

    private @NotNull Optional<String> normalizeHost(final @Nullable String host) {
        if (host == null || host.isBlank()) {
            return Optional.empty();
        }

        String normalized = host.trim();
        final int schemeIndex = normalized.indexOf("://");
        if (schemeIndex >= 0) {
            normalized = normalized.substring(schemeIndex + 3);
        }

        final int pathIndex = normalized.indexOf('/');
        if (pathIndex >= 0) {
            normalized = normalized.substring(0, pathIndex);
        }

        if (normalized.startsWith("[")) {
            final int endIndex = normalized.indexOf(']');
            if (endIndex > 0) {
                return Optional.of(normalized.substring(0, endIndex + 1));
            }
        }

        final int firstColon = normalized.indexOf(':');
        if (firstColon >= 0 && firstColon == normalized.lastIndexOf(':')) {
            normalized = normalized.substring(0, firstColon);
        }

        return normalized.isBlank() ? Optional.empty() : Optional.of(normalized);
    }

    private @NotNull String escape(final @NotNull String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void send(final @NotNull HttpExchange exchange, final int status, final @NotNull String body, final @NotNull String contentType) throws IOException {
        send(exchange, status, body, contentType, null);
    }

    private void send(
            final @NotNull HttpExchange exchange,
            final int status,
            final @NotNull String body,
            final @NotNull String contentType,
            final @Nullable String location
    ) throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        final Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        if (location != null) {
            headers.set("Location", location);
        }
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private @NotNull String style() {
        return "<style>"
                + ":root{color-scheme:dark;--bg:#111315;--panel:#191d21;--panel2:#20262b;--line:#343b43;--text:#eef2f5;--muted:#9aa7b2;--accent:#12b886;--warn:#f59f00;--danger:#e03131}"
                + "*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:14px/1.4 Inter,Segoe UI,Arial,sans-serif}"
                + "header{height:56px;display:flex;align-items:center;justify-content:space-between;padding:0 18px;border-bottom:1px solid var(--line);background:#15181b;position:sticky;top:0;z-index:2}"
                + "header strong{font-size:15px}header span{margin-left:10px;color:var(--muted)}header a{color:var(--accent);text-decoration:none}"
                + "main{display:grid;grid-template-columns:minmax(340px,470px) minmax(420px,1fr);gap:16px;padding:16px;max-width:1320px;margin:0 auto}"
                + ".preview,.panel{background:var(--panel);border:1px solid var(--line);border-radius:8px}.preview{padding:14px;height:max-content;position:sticky;top:72px}"
                + ".section-title,.panel-title{display:flex;align-items:flex-start;justify-content:space-between;gap:14px;margin-bottom:12px}.section-title h2,.panel-title h3{margin:0;font-size:16px}.section-title span,.panel-title span{color:var(--muted);font-size:12px;text-align:right;word-break:break-all}"
                + ".grid{display:grid;grid-template-columns:repeat(9,minmax(32px,1fr));gap:5px}.slot{aspect-ratio:1/1;min-width:0;display:flex;flex-direction:column;justify-content:space-between;padding:5px;border:1px solid #3c444c;background:#242a30;color:var(--muted);text-decoration:none;border-radius:4px;overflow:hidden}"
                + ".slot.filled{background:#26352f;border-color:#3a7d62;color:#dff8ec}.slot.selected{outline:2px solid var(--accent);outline-offset:1px}.slot-number{font-size:11px}.slot-material{font:10px/1.1 Consolas,monospace;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}"
                + ".editor{display:grid;gap:16px}.panel{padding:14px}.fields{display:grid;gap:10px}.fields.two{grid-template-columns:repeat(2,minmax(0,1fr))}"
                + "label{display:grid;gap:5px;color:var(--muted);font-size:12px;margin-bottom:10px}input,textarea{width:100%;border:1px solid var(--line);background:#101316;color:var(--text);border-radius:6px;padding:9px 10px;font:13px Consolas,monospace}textarea{min-height:86px;resize:vertical}"
                + ".check{display:flex;align-items:center;gap:8px}.check input{width:auto}.actions{display:flex;gap:10px;align-items:center}.actions form{margin:0}"
                + "button{border:0;border-radius:6px;background:var(--accent);color:#06110d;padding:9px 13px;font-weight:700;cursor:pointer}.secondary{background:var(--warn);color:#171006}.danger{background:var(--danger);color:white}"
                + ".notice{margin-top:12px;border:1px solid #2f9e44;background:#16351f;color:#d3f9d8;border-radius:6px;padding:9px}.raw summary{cursor:pointer;color:var(--muted)}.raw textarea{min-height:360px;margin-top:12px}"
                + "@media(max-width:900px){main{grid-template-columns:1fr}.preview{position:static}.fields.two{grid-template-columns:1fr}}"
                + "</style>";
    }

    private @NotNull String script() {
        return "<script>"
                + "(()=>{const form=document.getElementById('slot-form');if(!form)return;"
                + "const material=form.querySelector('[name=\"material\"]');const selected=document.querySelector('.slot.selected .slot-material');"
                + "const title=document.querySelector('#slot-form .panel-title span');"
                + "const sync=()=>{if(selected&&material)selected.textContent=(material.value||'Empty').toUpperCase().slice(0,13);if(title&&material)title.textContent=material.value||'Empty'};"
                + "form.addEventListener('input',sync);sync();})();"
                + "</script>";
    }

    private static class Session {
        private final String token;
        private final String menuName;
        private final Instant expiresAt;

        private Session(final @NotNull String token, final @NotNull String menuName, final @NotNull Instant expiresAt) {
            this.token = token;
            this.menuName = menuName;
            this.expiresAt = expiresAt;
        }
    }
}
