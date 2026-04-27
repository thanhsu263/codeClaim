package com.cobbleverse.codeclaim.config;

import com.cobbleverse.codeclaim.CodeClaim;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Quản lý file cấu hình codeclaim.json trong thư mục config/
 * Tự động tạo file mẫu nếu chưa tồn tại.
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath;

    /** Map: code_string -> CodeEntry */
    public Map<String, CodeEntry> codes = new LinkedHashMap<>();

    /** Các tin nhắn hệ thống (hỗ trợ màu § hoặc & ) */
    public String msgInvalidCode      = "&cCode '&4{code}&c' không tồn tại!";
    public String msgAlreadyClaimed   = "&cBạn đã nhập code '&4{code}&c' rồi!";
    public String msgMaxUses          = "&cCode '&4{code}&c' đã hết lượt sử dụng!";
    public String msgSuccess          = "&aĐã nhận thưởng từ code '&2{code}&a' thành công!";
    public String msgReloaded         = "&aCodeClaim đã reload config!";

    public ConfigManager() {
        this.configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("codeclaim.json");
    }

    public void load() {
        if (!Files.exists(configPath)) {
            createDefault();
            return;
        }
        try (Reader reader = new InputStreamReader(
                new FileInputStream(configPath.toFile()), StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                CodeClaim.LOGGER.warn("[CodeClaim] File config rỗng, dùng cấu hình mặc định.");
                return;
            }
            parseMessages(root);
            parseCodes(root);
            CodeClaim.LOGGER.info("[CodeClaim] Đã load {} code từ config.", codes.size());
        } catch (Exception e) {
            CodeClaim.LOGGER.error("[CodeClaim] Lỗi đọc config: {}", e.getMessage());
        }
    }

    private void parseMessages(JsonObject root) {
        if (!root.has("messages")) return;
        JsonObject msgs = root.getAsJsonObject("messages");
        msgInvalidCode    = getOrDefault(msgs, "invalid_code",    msgInvalidCode);
        msgAlreadyClaimed = getOrDefault(msgs, "already_claimed", msgAlreadyClaimed);
        msgMaxUses        = getOrDefault(msgs, "max_uses",        msgMaxUses);
        msgSuccess        = getOrDefault(msgs, "success",         msgSuccess);
        msgReloaded       = getOrDefault(msgs, "reloaded",        msgReloaded);
    }

    private String getOrDefault(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    private void parseCodes(JsonObject root) {
        codes.clear();
        if (!root.has("codes")) return;
        JsonObject codesObj = root.getAsJsonObject("codes");
        for (Map.Entry<String, JsonElement> entry : codesObj.entrySet()) {
            String codeKey = entry.getKey();
            JsonObject codeObj = entry.getValue().getAsJsonObject();
            int maxUses = codeObj.has("max_uses") ? codeObj.get("max_uses").getAsInt() : -1;
            List<String> commands = new ArrayList<>();
            if (codeObj.has("commands")) {
                for (JsonElement el : codeObj.getAsJsonArray("commands")) {
                    commands.add(el.getAsString());
                }
            }
            codes.put(codeKey, new CodeEntry(maxUses, commands));
        }
    }

    private void createDefault() {
        try {
            JsonObject root = new JsonObject();

            // messages
            JsonObject msgs = new JsonObject();
            msgs.addProperty("invalid_code",    msgInvalidCode);
            msgs.addProperty("already_claimed", msgAlreadyClaimed);
            msgs.addProperty("max_uses",        msgMaxUses);
            msgs.addProperty("success",         msgSuccess);
            msgs.addProperty("reloaded",        msgReloaded);
            root.add("messages", msgs);

            // codes mẫu
            JsonObject codesObj = new JsonObject();

            JsonObject vip = new JsonObject();
            vip.addProperty("max_uses", 100);
            JsonArray vipCmds = new JsonArray();
            vipCmds.add("give %player% minecraft:diamond 5");
            vipCmds.add("say %player% vừa nhận thưởng VIP!");
            vip.add("commands", vipCmds);
            codesObj.add("VIP2024", vip);

            JsonObject once = new JsonObject();
            once.addProperty("max_uses", 1);
            JsonArray onceCmds = new JsonArray();
            onceCmds.add("give %player% minecraft:netherite_ingot 1");
            once.add("commands", onceCmds);
            codesObj.add("GRAND_PRIZE", once);

            root.add("codes", codesObj);

            Files.createDirectories(configPath.getParent());
            try (Writer w = new OutputStreamWriter(
                    new FileOutputStream(configPath.toFile()), StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
            CodeClaim.LOGGER.info("[CodeClaim] Đã tạo file config mẫu tại: {}", configPath);
            parseCodes(root);
        } catch (Exception e) {
            CodeClaim.LOGGER.error("[CodeClaim] Không tạo được config: {}", e.getMessage());
        }
    }
}
