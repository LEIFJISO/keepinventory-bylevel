package com.le.keepinventorybylevel;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ClientEventHandler {

    public static final String KEY_CATEGORY = "key.categories.keepinventorybylevel";
    public static final String KEY_HIDE_PROTECTION = "key.keepinventorybylevel.hide_protection";

    public static final ResourceLocation PROTECTION_ICON =
            ResourceLocation.fromNamespaceAndPath(KeepInventoryByLevel.MODID, "textures/gui/protection_icon.png");

    public static KeyMapping hideProtectionKey;

    private ClientEventHandler() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::onScreenRenderPost);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        hideProtectionKey = new KeyMapping(
                KEY_HIDE_PROTECTION,
                InputConstants.KEY_F1,
                KEY_CATEGORY
        );
        hideProtectionKey.setKeyConflictContext(KeyConflictContext.GUI);
        event.register(hideProtectionKey);
    }

    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (hideProtectionKey == null) {
            return;
        }

        if (hideProtectionKey.isDown()) {
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

            graphics.blit(PROTECTION_ICON, slotX + 1, slotY + 10, 0, 0, 9, 9, 9, 9);
        }
    }
}
