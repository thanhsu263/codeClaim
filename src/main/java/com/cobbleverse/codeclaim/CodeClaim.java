package com.cobbleverse.codeclaim;

import com.cobbleverse.codeclaim.command.ClaimCommand;
import com.cobbleverse.codeclaim.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeClaim implements ModInitializer {
    public static final String MOD_ID = "codeclaim";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ConfigManager configManager;

    @Override
    public void onInitialize() {
        LOGGER.info("[CodeClaim] Đang khởi động mod nhập code...");

        configManager = new ConfigManager();
        configManager.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ClaimCommand.register(dispatcher);
        });

        LOGGER.info("[CodeClaim] Mod khởi động thành công! Dùng /claim <code> để nhận thưởng.");
    }
}
