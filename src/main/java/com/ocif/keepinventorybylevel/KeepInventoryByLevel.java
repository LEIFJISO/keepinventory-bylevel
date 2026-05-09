package com.ocif.keepinventorybylevel;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(KeepInventoryByLevel.MODID)
public class KeepInventoryByLevel {
    public static final String MODID = "keepinventorybylevel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KeepInventoryByLevel(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(DeathEventHandler.INSTANCE);
        modEventBus.addListener(ClientEventHandler::onClientSetup);
        modEventBus.addListener(ClientEventHandler::onRegisterKeyMappings);
    }
}
