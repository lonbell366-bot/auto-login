package me.myauth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.myauth.core.ProxyConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyAuthClient implements ClientModInitializer {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File ACCOUNTS_FILE = FabricLoader.getInstance().getConfigDir().resolve("myauth_tokens.json").toFile();

    public static List<SavedAccount> savedAccounts = new ArrayList<>();
    public static ProxyConfig proxyConfig = new ProxyConfig();

    public static boolean hoverLootEnabled = true;
    public static boolean noCursorResetEnabled = true;
    public static boolean autoReconnectEnabled = true;
    public static int autoReconnectDelaySeconds = 5;

    public static ServerInfo lastServerInfo = null;
    public static ServerAddress lastServerAddress = null;

    public static double lastMouseX = -1;
    public static double lastMouseY = -1;

    @Override
    public void onInitializeClient() {
        loadAccounts();
    }

    public static void saveAccounts() {
        try (FileWriter writer = new FileWriter(ACCOUNTS_FILE)) {
            GSON.toJson(savedAccounts, writer);
        } catch (Exception ignored) {}
    }

    public static void loadAccounts() {
        if (!ACCOUNTS_FILE.exists()) return;
        try (FileReader reader = new FileReader(ACCOUNTS_FILE)) {
            Type listType = new TypeToken<ArrayList<SavedAccount>>(){}.getType();
            List<SavedAccount> loaded = GSON.fromJson(reader, listType);
            if (loaded != null) savedAccounts = loaded;
        } catch (Exception ignored) {}
    }

    public record SavedAccount(String username, String uuid, String accessToken) {}
}
