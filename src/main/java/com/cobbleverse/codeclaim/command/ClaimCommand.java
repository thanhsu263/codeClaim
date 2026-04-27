package com.cobbleverse.codeclaim.command;

import com.cobbleverse.codeclaim.CodeClaim;
import com.cobbleverse.codeclaim.config.CodeEntry;
import com.cobbleverse.codeclaim.config.ConfigManager;
import com.cobbleverse.codeclaim.data.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Lệnh /claim <code>  - tất cả người chơi đều dùng được (permission level 0)
 * Lệnh /claimadmin reload - chỉ OP mới dùng được (permission level 2)
 */
public class ClaimCommand {

    private static PlayerDataManager dataManager;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dataManager = new PlayerDataManager();
        dataManager.load();

        // /claim <code>  — mọi người chơi
        dispatcher.register(
            CommandManager.literal("claim")
                .requires(src -> src.isExecutedByPlayer()) // phải là player, không phải console
                .then(
                    CommandManager.argument("code", StringArgumentType.word())
                        .executes(ctx -> executeClaim(ctx, StringArgumentType.getString(ctx, "code")))
                )
        );

        // /claimadmin reload  — chỉ OP level 2
        dispatcher.register(
            CommandManager.literal("claimadmin")
                .requires(src -> src.hasPermissionLevel(2))
                .then(
                    CommandManager.literal("reload")
                        .executes(ClaimCommand::executeReload)
                )
                .then(
                    CommandManager.literal("info")
                        .then(
                            CommandManager.argument("code", StringArgumentType.word())
                                .executes(ctx -> executeInfo(ctx, StringArgumentType.getString(ctx, "code")))
                        )
                )
        );
    }

    // ─── /claim <code> ───────────────────────────────────────────────────────

    private static int executeClaim(CommandContext<ServerCommandSource> ctx, String inputCode) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            return 0;
        }

        ConfigManager cfg = CodeClaim.configManager;

        // Tìm code khớp (không phân biệt hoa thường)
        String matchedKey = null;
        CodeEntry entry = null;
        for (String key : cfg.codes.keySet()) {
            if (key.equalsIgnoreCase(inputCode)) {
                matchedKey = key;
                entry = cfg.codes.get(key);
                break;
            }
        }

        if (matchedKey == null || entry == null) {
            sendMessage(player, cfg.msgInvalidCode.replace("{code}", inputCode));
            return 0;
        }

        // Kiểm tra player đã nhập chưa
        if (dataManager.hasRedeemed(player.getUuid(), matchedKey)) {
            sendMessage(player, cfg.msgAlreadyClaimed.replace("{code}", inputCode));
            return 0;
        }

        // Kiểm tra giới hạn tổng số lần dùng
        if (entry.maxUses > 0 && dataManager.getUsageCount(matchedKey) >= entry.maxUses) {
            sendMessage(player, cfg.msgMaxUses.replace("{code}", inputCode));
            return 0;
        }

        // Thực thi lệnh thưởng
        for (String cmd : entry.commands) {
            String formatted = cmd.replace("%player%", player.getName().getString());
            try {
                source.getServer().getCommandManager()
                        .executeWithPrefix(source.getServer().getCommandSource(), formatted);
            } catch (Exception e) {
                CodeClaim.LOGGER.error("[CodeClaim] Lỗi khi chạy lệnh '{}': {}", formatted, e.getMessage());
            }
        }

        // Ghi nhận
        dataManager.markRedeemed(player.getUuid(), matchedKey);
        sendMessage(player, cfg.msgSuccess.replace("{code}", inputCode));

        CodeClaim.LOGGER.info("[CodeClaim] {} đã nhập code '{}'", player.getName().getString(), matchedKey);
        return 1;
    }

    // ─── /claimadmin reload ───────────────────────────────────────────────────

    private static int executeReload(CommandContext<ServerCommandSource> ctx) {
        CodeClaim.configManager.load();
        dataManager.load();
        ctx.getSource().sendFeedback(
            () -> Text.literal(colorize(CodeClaim.configManager.msgReloaded)), false);
        return 1;
    }

    // ─── /claimadmin info <code> ──────────────────────────────────────────────

    private static int executeInfo(CommandContext<ServerCommandSource> ctx, String code) {
        ConfigManager cfg = CodeClaim.configManager;
        CodeEntry entry = null;
        String matchedKey = null;
        for (String k : cfg.codes.keySet()) {
            if (k.equalsIgnoreCase(code)) { matchedKey = k; entry = cfg.codes.get(k); break; }
        }
        if (entry == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cCode không tồn tại: " + code), false);
            return 0;
        }
        int used = dataManager.getUsageCount(matchedKey);
        String maxStr = entry.maxUses < 0 ? "∞" : String.valueOf(entry.maxUses);
        final String finalKey = matchedKey;
        final CodeEntry finalEntry = entry;
        final String msg = "§6[CodeClaim] §eCode: §f" + finalKey + "\n" +
            "§eĐã dùng: §f" + used + " / " + maxStr + "\n" +
            "§eLệnh: §f" + String.join(", ", finalEntry.commands);
        ctx.getSource().sendFeedback(() -> Text.literal(msg), false);
        return 1;
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private static void sendMessage(ServerPlayerEntity player, String raw) {
        player.sendMessage(Text.literal(colorize(raw)), false);
    }

    /** Chuyển &x và §x thành màu Minecraft */
    private static String colorize(String s) {
        return s.replace("&", "§");
    }
}
