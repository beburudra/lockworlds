package net.example.lockworlds.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class LockConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("lockworlds");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String WORLDS_KEY = "lockedWorlds";
    private static final String SERVERS_KEY = "lockedServers";

    private static LockConfig instance;

    private final Set<String> lockedWorlds = new HashSet<>();
    private final Set<String> lockedServers = new HashSet<>();
    private final Path configFile;

    private LockConfig() {
        configFile = FabricLoader.getInstance().getConfigDir().resolve("lockworlds.json");
        load();
    }

    public static LockConfig get() {
        if (instance == null) {
            instance = new LockConfig();
        }
        return instance;
    }

    public boolean isLocked(String folderName) {
        return lockedWorlds.contains(folderName);
    }

    public boolean isServerLocked(String serverKey) {
        return lockedServers.contains(serverKey);
    }

    public boolean toggleLock(String folderName) {
        boolean nowLocked = toggle(lockedWorlds, folderName);
        save();
        return nowLocked;
    }

    public boolean toggleServerLock(String serverKey) {
        boolean nowLocked = toggle(lockedServers, serverKey);
        save();
        return nowLocked;
    }

    private void load() {
        if (!Files.exists(configFile)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            loadSet(root, WORLDS_KEY, lockedWorlds);
            loadSet(root, SERVERS_KEY, lockedServers);
            LOGGER.info("[LockWorlds] Loaded {} locked world(s) and {} locked server(s) from config.", lockedWorlds.size(), lockedServers.size());
        } catch (Exception e) {
            LOGGER.error("[LockWorlds] Failed to load config, starting fresh.", e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(configFile.getParent());

            JsonObject root = new JsonObject();
            root.add(WORLDS_KEY, toJsonArray(lockedWorlds));
            root.add(SERVERS_KEY, toJsonArray(lockedServers));

            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            LOGGER.error("[LockWorlds] Failed to save config.", e);
        }
    }

    private static boolean toggle(Set<String> set, String key) {
        if (set.contains(key)) {
            set.remove(key);
            return false;
        }
        set.add(key);
        return true;
    }

    private static void loadSet(JsonObject root, String key, Set<String> target) {
        if (root.has(key) && root.get(key).isJsonArray()) {
            root.getAsJsonArray(key).forEach(element -> {
                if (element.isJsonPrimitive()) {
                    target.add(element.getAsString());
                }
            });
        }
    }

    private static com.google.gson.JsonArray toJsonArray(Set<String> values) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        values.stream().sorted().forEach(array::add);
        return array;
    }
}
