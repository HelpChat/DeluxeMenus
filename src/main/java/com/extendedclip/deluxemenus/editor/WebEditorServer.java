package com.extendedclip.deluxemenus.editor;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
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

    public @NotNull String createSession(final @NotNull Menu menu, final int requestedPort) throws IOException {
        ensureStarted(requestedPort);

        final String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(menu.options().name(), Instant.now().plus(Duration.ofMinutes(15))));

        final String host = plugin.getServer().getIp() == null || plugin.getServer().getIp().isBlank()
                ? "localhost"
                : plugin.getServer().getIp();
        return "http://" + host + ":" + server.getAddress().getPort() + "/dm-web/" + token;
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
            return;
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

        final String token = parts[2];
        final Optional<Session> optionalSession = getSession(token);
        if (optionalSession.isEmpty()) {
            send(exchange, 403, "Invalid or expired editor session.", "text/plain");
            return;
        }

        if (parts.length >= 4 && "save".equals(parts[3]) && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            save(exchange, optionalSession.get());
            return;
        }

        if (parts.length >= 4 && "save-item".equals(parts[3]) && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            saveItem(exchange, optionalSession.get());
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "Method not allowed", "text/plain");
            return;
        }

        render(exchange, optionalSession.get());
    }

    private void render(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = Menu.getMenuByName(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final Menu menu = optionalMenu.get();
        final String html = "<!doctype html><html><head><meta charset=\"utf-8\"><title>DeluxeMenus Editor</title>"
                + "<style>body{font-family:Arial,sans-serif;margin:0;background:#101418;color:#e8edf2}"
                + "header{padding:16px 22px;background:#1b242d;border-bottom:1px solid #32404d}"
                + "main{display:grid;grid-template-columns:420px 1fr;gap:18px;padding:18px}.panel{background:#151c23;border:1px solid #2e3b46;padding:14px}"
                + ".grid{display:grid;grid-template-columns:repeat(9,42px);gap:5px}.slot{height:42px;background:#202a33;border:1px solid #40515f;display:flex;align-items:center;justify-content:center;font-size:12px;color:#94a3b8}"
                + ".filled{background:#304050;color:#fff}input,textarea,select{width:100%;box-sizing:border-box;background:#0b0f13;color:#d7e1ea;border:1px solid #40515f;padding:9px;margin:5px 0 10px;font-family:Consolas,monospace}"
                + "textarea{height:78px}.raw{height:42vh}button{background:#2f7dd1;color:white;border:0;padding:10px 14px;margin-top:4px;cursor:pointer}.danger{background:#5d6873}</style></head><body>"
                + "<header><strong>DeluxeMenus Web Editor</strong> - " + escape(menu.options().name()) + "</header><main><section>"
                + "<div class=\"panel\"><h3>Preview</h3><div class=\"grid\">" + renderGrid(menu) + "</div></div></section><section>"
                + renderItemForms(menu, session)
                + "<div class=\"panel\"><h3>Raw YAML</h3>"
                + "<form method=\"post\" action=\"/dm-web/" + escape(session.token()) + "/save\">"
                + "<textarea class=\"raw\" name=\"content\">" + escape(configEditor.readRaw(menu)) + "</textarea><br><button class=\"danger\" type=\"submit\">Save raw YAML and reload</button></form></div>"
                + "</section></main></body></html>";
        send(exchange, 200, html, "text/html; charset=utf-8");
    }

    private void save(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = Menu.getMenuByName(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        final String content = parseFormValue(body, "content");
        configEditor.saveRaw(optionalMenu.get(), content);
        final String menuName = optionalMenu.get().options().name();
        plugin.getScheduler().runTask(() -> {
            Menu.unload(plugin, menuName);
            plugin.getConfiguration().loadGUIMenu(menuName);
        });
        send(exchange, 200, "Saved. You can go back and refresh the editor.", "text/plain");
    }

    private void saveItem(final @NotNull HttpExchange exchange, final @NotNull Session session) throws IOException {
        final Optional<Menu> optionalMenu = Menu.getMenuByName(session.menuName);
        if (optionalMenu.isEmpty()) {
            send(exchange, 404, "Menu is not loaded.", "text/plain");
            return;
        }

        final Map<String, String> form = parseForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        final int slot = parseInt(form.get("slot"));
        final Menu menu = optionalMenu.get();
        configEditor.setItemValue(menu, slot, "material", form.getOrDefault("material", "STONE"));
        configEditor.setItemValue(menu, slot, "display_name", form.getOrDefault("display_name", ""));
        configEditor.setItemValue(menu, slot, "lore", form.getOrDefault("lore", ""));
        configEditor.setItemValue(menu, slot, "left_click_commands", form.getOrDefault("left_click_commands", ""));
        configEditor.setItemValue(menu, slot, "right_click_commands", form.getOrDefault("right_click_commands", ""));
        configEditor.setItemValue(menu, slot, "click_commands", form.getOrDefault("click_commands", ""));

        final String menuName = menu.options().name();
        plugin.getScheduler().runTask(() -> {
            Menu.unload(plugin, menuName);
            plugin.getConfiguration().loadGUIMenu(menuName);
        });
        send(exchange, 303, "", "text/plain", "/dm-web/" + session.token());
    }

    private @NotNull Optional<Session> getSession(final @NotNull String token) {
        final Session session = sessions.get(token);
        if (session == null || session.expiresAt.isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        session.token = token;
        return Optional.of(session);
    }

    private @NotNull String renderGrid(final @NotNull Menu menu) {
        final StringBuilder builder = new StringBuilder();
        for (int slot = 0; slot < menu.options().size(); slot++) {
            final Map<Integer, MenuItem> items = menu.getMenuItems().get(slot);
            final boolean filled = items != null && !items.isEmpty();
            final String label = filled ? items.values().iterator().next().options().material() : String.valueOf(slot);
            builder.append("<div class=\"slot")
                    .append(filled ? " filled" : "")
                    .append("\" title=\"")
                    .append(escape(label))
                    .append("\">")
                    .append(slot)
                    .append("</div>");
        }
        return builder.toString();
    }

    private @NotNull String renderItemForms(final @NotNull Menu menu, final @NotNull Session session) {
        final StringBuilder builder = new StringBuilder("<div class=\"panel\"><h3>Slot editor</h3>");
        for (int slot = 0; slot < menu.options().size(); slot++) {
            final Map<Integer, MenuItem> items = menu.getMenuItems().get(slot);
            if (items == null || items.isEmpty()) {
                continue;
            }
            builder.append(renderItemForm(menu, session, slot));
        }
        builder.append(renderItemForm(menu, session, 0));
        builder.append("</div>");
        return builder.toString();
    }

    private @NotNull String renderItemForm(final @NotNull Menu menu, final @NotNull Session session, final int defaultSlot) {
        return "<form method=\"post\" action=\"/dm-web/" + escape(session.token()) + "/save-item\">"
                + "<label>Slot</label><input name=\"slot\" value=\"" + defaultSlot + "\">"
                + "<label>Material</label><input name=\"material\" value=\"" + escape(value(menu, defaultSlot, "material")) + "\">"
                + "<label>Display name</label><input name=\"display_name\" value=\"" + escape(value(menu, defaultSlot, "display_name")) + "\">"
                + "<label>Lore</label><textarea name=\"lore\">" + escape(value(menu, defaultSlot, "lore")) + "</textarea>"
                + "<label>Left click commands</label><textarea name=\"left_click_commands\">" + escape(value(menu, defaultSlot, "left_click_commands")) + "</textarea>"
                + "<label>Right click commands</label><textarea name=\"right_click_commands\">" + escape(value(menu, defaultSlot, "right_click_commands")) + "</textarea>"
                + "<label>Click commands</label><textarea name=\"click_commands\">" + escape(value(menu, defaultSlot, "click_commands")) + "</textarea>"
                + "<button type=\"submit\">Save slot</button></form><hr>";
    }

    private @NotNull String value(final @NotNull Menu menu, final int slot, final @NotNull String option) {
        return configEditor.getItemString(menu, slot, option).orElse("");
    }

    private @NotNull String parseFormValue(final @NotNull String body, final @NotNull String key) {
        return parseForm(body).getOrDefault(key, "");
    }

    private @NotNull Map<String, String> parseForm(final @NotNull String body) {
        final Map<String, String> form = new ConcurrentHashMap<>();
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

    private @NotNull String escape(final @NotNull String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void send(final @NotNull HttpExchange exchange, final int status, final @NotNull String body, final @NotNull String contentType) throws IOException {
        send(exchange, status, body, contentType, null);
    }

    private void send(final @NotNull HttpExchange exchange, final int status, final @NotNull String body, final @NotNull String contentType, final @Nullable String location) throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        if (location != null) {
            exchange.getResponseHeaders().set("Location", location);
        }
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static class Session {
        private final String menuName;
        private final Instant expiresAt;
        private String token;

        private Session(final @NotNull String menuName, final @NotNull Instant expiresAt) {
            this.menuName = menuName;
            this.expiresAt = expiresAt;
        }

        private @NotNull String token() {
            return token;
        }
    }
}
