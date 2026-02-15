package de.jakob.lotm.util.shapeShifting;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class NameStorage {
    private static final Type GSON_TYPE = new TypeToken<HashMap<UUID, String>>() {}.getType();
    private static final String USERNAME_REGEX = "^[0-9a-zA-Z_]{1,16}$";
    private static final String CONFIG_FILE_NAME = "thzlotmaddon_nicknames.json";

    public static HashMap<UUID, String> mapping = new HashMap<>();

    private static File getConfigFile() {
        return FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME).toFile();
    }

    public static void save() {
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            new Gson().toJson(mapping, writer);
        } catch (Exception e) {
        }
    }

    public static void load() {
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(configFile)) {
            mapping = new Gson().fromJson(reader, GSON_TYPE);
            if (mapping == null) mapping = new HashMap<>();
            if (new HashSet<>(mapping.values()).size() != mapping.size()) {
                mapping.clear();
            }
            mapping.values().removeIf(name -> !name.matches(USERNAME_REGEX));
        } catch (Exception e) {
            mapping = new HashMap<>();
        }
    }

    public static void init() {
        load();
    }

    public static void setNickname(UUID uuid, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            mapping.remove(uuid);
        } else {
            mapping.put(uuid, nickname);
        }
    }
}