package com.le.keepinventorybylevel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathEventHandler {
    public static final DeathEventHandler INSTANCE = new DeathEventHandler();

    private final Map<UUID, Map<Integer, ItemStack>> protectedItems = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        int level = player.experienceLevel;
        List<String> protectedSlotIds = SlotProtectionManager.getProtectedSlotIds(level);

        if (protectedSlotIds.isEmpty()) {
            return;
        }

        Map<Integer, ItemStack> saved = new HashMap<>();
        Inventory inv = player.getInventory();

        for (String slotId : protectedSlotIds) {
            int index = SlotProtectionManager.getUnifiedIndex(slotId);
            ItemStack stack = SlotProtectionManager.getItem(inv, index).copy();
            if (!stack.isEmpty()) {
                saved.put(index, stack);
                SlotProtectionManager.setItem(inv, index, ItemStack.EMPTY);
            }
        }

        if (!saved.isEmpty()) {
            protectedItems.put(player.getUUID(), saved);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        Map<Integer, ItemStack> saved = protectedItems.remove(newPlayer.getUUID());
        if (saved == null || saved.isEmpty()) {
            return;
        }

        Inventory inv = newPlayer.getInventory();
        for (Map.Entry<Integer, ItemStack> entry : saved.entrySet()) {
            int index = entry.getKey();
            ItemStack existing = SlotProtectionManager.getItem(inv, index);
            if (!existing.isEmpty()) {
                newPlayer.drop(existing, true, false);
            }
            SlotProtectionManager.setItem(inv, index, entry.getValue());
        }
    }
}
