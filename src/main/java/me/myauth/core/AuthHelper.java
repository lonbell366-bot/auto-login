package me.myauth.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.myauth.MyAuthClient;
import me.myauth.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AuthHelper {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void loginWithToken(String rawToken, Consumer<String> statusCallback) {
        String cleanToken = rawToken.trim();
        if (cleanToken.isEmpty()) {
            statusCallback.accept("§cToken không được để trống!");
            return;
        }

        statusCallback.accept("§eĐang kiểm tra Token với Mojang...");

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
                        .header("Authorization", "Bearer " + cleanToken)
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    statusCallback.accept("§cToken hết hạn hoặc không hợp lệ! (Mã: " + response.statusCode() + ")");
                    return;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String username = json.get("name").getAsString();
                String rawUuid = json.get("id").getAsString();
                UUID uuid = parseUndashedUuid(rawUuid);

                MinecraftClient.getInstance().execute(() -> {
                    applySession(username, uuid, cleanToken);
                    saveOrUpdateAccount(username, uuid.toString(), cleanToken);
                    statusCallback.accept("§aĐăng nhập thành công: §f" + username);
                });
            } catch (Exception e) {
                statusCallback.accept("§cLỗi kết nối: " + e.getMessage());
            }
        });
    }

    public static void applySession(String username, UUID uuid, String accessToken) {
        Session session = new Session(
                username,
                uuid,
                accessToken,
                Optional.empty(),
                Optional.empty(),
                Session.AccountType.MSA
        );
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).setCustomSession(session);
    }

    private static void saveOrUpdateAccount(String username, String uuid, String token) {
        MyAuthClient.savedAccounts.removeIf(acc -> acc.username().equalsIgnoreCase(username));
        MyAuthClient.savedAccounts.add(0, new MyAuthClient.SavedAccount(username, uuid, token));
        MyAuthClient.saveAccounts();
    }

    public static UUID parseUndashedUuid(String raw) {
        if (raw.contains("-")) return UUID.fromString(raw);
        String dashed = raw.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(dashed);
    }
}
