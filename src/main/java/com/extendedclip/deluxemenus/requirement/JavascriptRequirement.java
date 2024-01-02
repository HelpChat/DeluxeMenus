package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import java.util.logging.Level;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class JavascriptRequirement extends Requirement {

    private static final ScriptEngineFactory factory = new NashornScriptEngineFactory();
    private static ScriptEngineManager engine;
    private final String expression;

    public JavascriptRequirement(String expression) {
        this.expression = expression;
        if (engine != null) return;

        ServicesManager manager = Bukkit.getServer().getServicesManager();
        if (manager.isProvidedFor(ScriptEngineManager.class)) {
            final RegisteredServiceProvider<ScriptEngineManager> provider = manager.getRegistration(ScriptEngineManager.class);
            if (provider != null) engine = provider.getProvider();
        }
        if (engine == null) {
            engine = new ScriptEngineManager();
            manager.register(ScriptEngineManager.class, engine, DeluxeMenus.getInstance(), ServicePriority.Highest);
        }
        engine.registerEngineName("JavaScript", factory);
        engine.put("BukkitServer", Bukkit.getServer());
    }

    @Override
    public boolean evaluate(MenuHolder holder) {
        String exp = holder.setPlaceholders(expression);
        try {
            engine.put("BukkitPlayer", holder.getViewer());
            Object result = engine.getEngineByName("JavaScript").eval(exp);

            if (result instanceof Boolean) return (boolean) result;

            DeluxeMenus.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Requirement javascript <" + expression + "> is invalid and does not return a boolean!"
            );
            return false;

        } catch (final ScriptException | NullPointerException exception) {
            DeluxeMenus.debug(DebugLevel.HIGHEST,Level.WARNING,"Error in requirement javascript syntax - " + expression);
            DeluxeMenus.printStacktrace("Error in requirement javascript syntax - " + expression, exception);
            return false;
        }
    }

}
