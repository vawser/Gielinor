package plugin;

import plugin.api.API;
import plugin.api.MiniMenuEntry;
import plugin.api.MiniMenuType;
import rt4.*;

import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for loading and broadcasting methods to all plugins.
 * @author ceikry
 */
public class PluginRepository {
    static HashMap<PluginInfo, Plugin> loadedPlugins = new HashMap<>();
    public static HashMap<String, Object> pluginStorage = new HashMap<>();

    public static void registerPlugin(PluginInfo info, Plugin plugin) {
        loadedPlugins.put(info, plugin);
    }

    public static int lastMiniMenu;

    public static void reloadPlugins() {
        for (KeyAdapter k : API.registeredKeyListeners) {
            GameShell.canvas.removeKeyListener(k);
        }
        for (MouseAdapter m : API.registeredMouseListeners) {
            GameShell.canvas.removeMouseListener(m);
            GameShell.canvas.removeMouseMotionListener(m);
        }
        for (MouseWheelListener mw : API.registeredWheelListeners) {
            GameShell.canvas.removeMouseWheelListener(mw);
        }
        API.registeredWheelListeners.clear();
        API.registeredMouseListeners.clear();
        API.registeredKeyListeners.clear();

        HashMap<PluginInfo, Plugin> pluginsToKeep = new HashMap<>();

        // Check and store plugins with OnPluginsReloaded method
        loadedPlugins.forEach((info, plugin) -> {
            try {
                boolean keep = plugin.OnPluginsReloaded();
                if (keep) {
                    pluginsToKeep.put(info, plugin);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loadedPlugins.clear();
        loadedPlugins.putAll(pluginsToKeep);
        SaveStorage();
        Init();
    }

    public static void Init() {
        if (GlobalJsonConfig.instance == null)
            return;
        File pluginsDirectory = new File(GlobalJsonConfig.instance.pluginsFolder);

        if (!pluginsDirectory.exists()) {
            System.out.println("Skipping plugin initialization - " + pluginsDirectory.getAbsolutePath() + " does not exist.");
            return;
        }

        File pluginStorage = new File(GlobalJsonConfig.instance.pluginsFolder + File.separator + "plsto");
        if (pluginStorage.exists()) {
           try (FileInputStream fis = new FileInputStream(pluginStorage)) {
               ObjectInputStream ois = new ObjectInputStream(fis);
               PluginRepository.pluginStorage = (HashMap<String, Object>) ois.readObject();
               ois.close();
           } catch (Exception e) {e.printStackTrace();}
        }

        Runtime.getRuntime().addShutdownHook(new Thread(PluginRepository::SaveStorage));

        try {
            URL[] classPath = {pluginsDirectory.toURI().toURL()};
            URLClassLoader loader = new URLClassLoader(classPath);

            for(File file : Objects.requireNonNull(pluginsDirectory.listFiles())) {
                if(!file.isDirectory()) continue;
                if(file.getName().equals("META-INF")) continue;

                File infoFile = new File(file.getAbsoluteFile() + File.separator + "plugin.properties");
                File pluginRoot = new File(file.getAbsoluteFile() + File.separator + "plugin.class");

                if (!pluginRoot.exists()) {
                    System.err.println("Unable to load plugin " + file.getName() + " because plugin.class is absent!");
                    continue;
                }

                Class<?> clazz = loader.loadClass(file.getName() + ".plugin");

                PluginInfo info = null;
                try {
                    if (infoFile.exists())
                        info = PluginInfo.loadFromFile(infoFile);
                    else
                        info = PluginInfo.loadFromClass(clazz);
                } catch (Exception e) {
                    System.err.println("Unable to load plugin " + file.getName() + " because there were issues parsing its info: ");
                    e.printStackTrace();
                }

                if (info == null) {
                    System.err.println("Unable to load plugin " + file.getName() + " because it contains no information about author, version, etc!");
                    continue;
                }

                if (loadedPlugins.containsKey(info)) {
                    System.out.println("Skipping reloading of plugin " + file.getName() + " as it already exists and has OnPluginsReloaded.");
                    continue;
                }

                try {
                    Plugin thisPlugin = (Plugin) clazz.newInstance();
                    thisPlugin._init();
                    registerPlugin(info, thisPlugin);
                } catch (Exception e) {
                    System.err.println("Error loading plugin " + file.getName() + ":");
                    e.printStackTrace();
                    return;
                }

                List<File> otherClasses = Arrays.stream(Objects.requireNonNull(file.listFiles()))
                        .filter((f) ->
                                !f.getName().equals("plugin.class") && f.getName().contains(".class"))
                        .collect(Collectors.toList());

                for (File f : otherClasses) {
                    loader.loadClass(file.getName() + "." + f.getName().replace(".class",""));
                }

                System.out.println("Successfully loaded plugin " + file.getName() + ", version " + info.version);
            }
        } catch (Exception e) {
            System.err.println("Unexpected exception during plugin initialization:");
            e.printStackTrace();
        }
    }

    public static void Update() {
        loadedPlugins.values().forEach(Plugin::Update);
    }

    public static void Draw() {
        List<Plugin> pluginsSnapshot = new ArrayList<>(loadedPlugins.values());
        pluginsSnapshot.forEach(Plugin::_draw);
    }

    public static void LateDraw() {
        List<Plugin> pluginsSnapshot = new ArrayList<>(loadedPlugins.values());
        pluginsSnapshot.forEach(Plugin::_lateDraw);
    }

    public static void NPCOverheadDraw(Npc npc, int screenX, int screenY) {
        loadedPlugins.values().forEach((plugin) -> plugin.NPCOverheadDraw(npc, screenX, screenY));
    }

    public static void PlayerOverheadDraw(Player player, int screenX, int screenY) {
        loadedPlugins.values().forEach((plugin) -> plugin.PlayerOverheadDraw(player, screenX, screenY));
    }

    public static void ProcessCommand(JagString commandStr) {
        String[] tokens = commandStr.toString().split(" ");
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        List<Plugin> pluginsSnapshot = new ArrayList<>(loadedPlugins.values());
        pluginsSnapshot.forEach((plugin) -> plugin.ProcessCommand(tokens[0], args));
    }

    public static void ComponentDraw(int componentIndex, Component component, int screenX, int screenY) {
        loadedPlugins.values().forEach((plugin) -> plugin.ComponentDraw(componentIndex, component, screenX, screenY));
    }

    public static void OnVarpUpdate(int id, int value) {
        loadedPlugins.values().forEach((plugin) -> plugin.OnVarpUpdate(id, value));
    }

    public static void OnXPUpdate(int skill, int xp) {
        loadedPlugins.values().forEach((plugin) -> plugin.OnXPUpdate(skill, xp));
    }

    public static void OnLogout() {
        List<Plugin> pluginsSnapshot = new ArrayList<>(loadedPlugins.values());
        pluginsSnapshot.forEach(Plugin::OnLogout);
    }

    public static void DrawMiniMenu(MiniMenuEntry entry) {
        loadedPlugins.values().forEach((plugin) -> plugin.DrawMiniMenu(entry));
    }

    public static void OnMiniMenuCreate() {
        API.customMiniMenuIndex = 0;
        loadedPlugins.values().forEach((plugin) -> plugin.OnMiniMenuCreate(API.GetMiniMenuEntries()));
    }

    public static void OnLogin() {
        loadedPlugins.values().forEach((plugin) -> plugin.OnLogin());
    }

    public static void OnKillingBlowNPC(int npcId, int x, int z) {
        loadedPlugins.values().forEach((plugin) -> plugin.OnKillingBlowNPC(npcId, x, z));
    }

    public static void SaveStorage() {
        if (pluginStorage.containsKey("_keystoreDirty")) {
            pluginStorage.remove("_keystoreDirty");
            try(FileOutputStream fos = new FileOutputStream(GlobalJsonConfig.instance.pluginsFolder + File.separator + "plsto")) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(PluginRepository.pluginStorage);
                oos.close();
            } catch (Exception e) {e.printStackTrace();}
        }
    }
}
