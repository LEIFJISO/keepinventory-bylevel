package com.le.keepinventorybylevel;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

@EventBusSubscriber(modid = KeepInventoryByLevel.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    public static final String KEY_CATEGORY = "key.categories.keepinventorybylevel";
    public static final String KEY_SHOW_PROTECTION = "key.keepinventorybylevel.show_protection";

    public static KeyMapping showProtectionKey;

    private ClientEventHandler() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::onScreenRenderPost);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        showProtectionKey = new KeyMapping(
                KEY_SHOW_PROTECTION,
                InputConstants.KEY_F1,
                KEY_CATEGORY
        );
        event.register(showProtectionKey);
    }

    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (showProtectionKey == null) {
            return;
        }

        if (!showProtectionKey.isDown()) {
            return;
        }

        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        int level = mc.player.experienceLevel;
        List<Integer> protectedIndices = SlotProtectionManager.getProtectedUnifiedIndices(level);

        if (protectedIndices.isEmpty()) {
            return;
        }

        Inventory playerInv = mc.player.getInventory();
        GuiGraphics graphics = event.getGuiGraphics();

        for (net.minecraft.world.inventory.Slot slot : screen.getMenu().slots) {
            if (slot.container != playerInv) {
                continue;
            }

            int slotIndex = slot.getSlotIndex();
            if (!protectedIndices.contains(slotIndex)) {
                continue;
            }

            int slotX = screen.getGuiLeft() + slot.x;
            int slotY = screen.getGuiTop() + slot.y;

            int iconSize = 5;
            int iconLeft = slotX + 1;
            int iconTop = slotY + 10;

            graphics.fill(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize, 0xCCFFD700);
            graphics.fill(iconLeft - 1, iconTop - 1, iconLeft + iconSize + 1, iconTop, 0xFF8B6914);
            graphics.fill(iconLeft - 1, iconTop + iconSize, iconLeft + iconSize + 1, iconTop + iconSize + 1, 0xFF8B6914);
            graphics.fill(iconLeft - 1, iconTop, iconLeft, iconTop + iconSize, 0xFF8B6914);
            graphics.fill(iconLeft + iconSize, iconTop, iconLeft + iconSize + 1, iconTop + iconSize, 0xFF8B6914);
        }
    }
}
