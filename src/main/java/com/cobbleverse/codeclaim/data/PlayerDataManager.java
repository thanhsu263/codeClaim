package com.cobbleverse.codeclaim.data;

import com.cobbleverse.codeclaim.CodeClaim;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Lưu dữ liệu người chơi đã nhập code nào, và tổng số lần mỗi code được dùng.
 * Dữ liệu được lưu vào file data/codeclaim_data.json (tự động ghi sau mỗi lần nhập).
 */
public class PlayerDataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path dataPath;

    /**
     * playerRedeemed: UUID -> Set<code>
     * Lưu trữ xem player nào đã nhập code nào.
     */
    private final Map<String, Set<String>> playerRedeemed = new HashMap<>();

    /**
     * codeUsageCount: code -> số lần đã dùng
     */
    private final Map<String, Integer> codeUsageCount = new HashMap<>();

    public PlayerDataManager() {
        this.dataPath = FabricLoader.getInstance()
                .getGameDir()
                .resolve("data")
                .resolve("codeclaim_data.json");
    }

    public void load() {
        if (!Files.exists(dataPath)) {
            CodeClaim.LOGGER.info("[CodeClaim] Chưa có file dữ liệu, bắt đầu mới.");
            return;
        }
        try (Reader r = new InputStreamReader(
                new FileInputStream(dataPath.toFile()), StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;

            if (root.has("playerRedeemed")) {
                for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("playerRedeemed").entrySet()) {
                    Set<String> codes = new HashSet<>();
                    for (JsonElement el : e.getValue().getAsJsonArray()) {
                        codes.add(el.getAsString());
                    }
                    playerRedeemed.put(e.getKey(), codes);
                }
            }
            if (root.has("codeUsageCount")) {
                for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("codeUsageCount").entrySet()) {
                    codeUsageCount.put(e.getKey(), e.getValue().getAsInt());
                }
            }
            CodeClaim.LOGGER.info("[CodeClaim] Đã load dữ liệu {} người chơi.", playerRedeemed.size());
        } catch (Exception e) {
            CodeClaim.LOGGER.error("[CodeClaim] Lỗi đọc dữ liệu: {}", e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(dataPath.getParent());
            JsonObject root = new JsonObject();

            JsonObject pr = new JsonObject();
            for (Map.Entry<String, Set<String>> e : playerRedeemed.entrySet()) {
                JsonArray arr = new JsonArray();
                for (String code : e.getValue()) arr.add(code);
                pr.add(e.getKey(), arr);
            }
            root.add("playerRedeemed", pr);

            JsonObject cu = new JsonObject();
            for (Map.Entry<String, Integer> e : codeUsageCount.entrySet()) {
                cu.addProperty(e.getKey(), e.getValue());
            }
            root.add("codeUsageCount", cu);

            try (Writer w = new OutputStreamWriter(
                    new FileOutputStream(dataPath.toFile()), StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            CodeClaim.LOGGER.error("[CodeClaim] Không lưu được dữ liệu: {}", e.getMessage());
        }
    }

    /** Kiểm tra player đã nhập code này chưa */
    public boolean hasRedeemed(UUID playerUUID, String code) {
        Set<String> set = playerRedeemed.get(playerUUID.toString());
        return set != null && set.contains(code.toLowerCase());
    }

    /** Lấy số lần code đã được dùng */
    public int getUsageCount(String code) {
        return codeUsageCount.getOrDefault(code.toLowerCase(), 0);
    }

    /** Ghi nhận player nhập code thành công */
    public void markRedeemed(UUID playerUUID, String code) {
        String key = code.toLowerCase();
        playerRedeemed.computeIfAbsent(playerUUID.toString(), k -> new HashSet<>()).add(key);
        codeUsageCount.merge(key, 1, Integer::sum);
        save();
    }
}
