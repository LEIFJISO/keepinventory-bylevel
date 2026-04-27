package com.le.keepinventorybylevel;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SlotProtectionManager {

    private static final Map<String, Integer> SLOT_ID_TO_INDEX = new LinkedHashMap<>();

    static {
        registerSlot("offhand", 40);

        for (int i = 0; i <= 8; i++) {
            registerSlot("hotbar." + i, i);
        }

        registerSlot("armor.head", 39);
        registerSlot("armor.chest", 38);
        registerSlot("armor.legs", 37);
        registerSlot("armor.feet", 36);

        for (int i = 0; i <= 26; i++) {
            registerSlot("inventory." + i, 9 + i);
        }
    }

    private static void registerSlot(String id, int index) {
        SLOT_ID_TO_INDEX.put(id, index);
    }

    public static int getUnifiedIndex(String slotId) {
        Integer index = SLOT_ID_TO_INDEX.get(slotId);
        if (index == null) {
            throw new IllegalArgumentException("Unknown slot ID: " + slotId);
        }
        return index;
    }

    public static ItemStack getItem(Inventory inv, int unifiedIndex) {
        if (unifiedIndex >= 0 && unifiedIndex < 36) {
            return inv.items.get(unifiedIndex);
        } else if (unifiedIndex >= 36 && unifiedIndex < 40) {
            return inv.armor.get(unifiedIndex - 36);
        } else if (unifiedIndex == 40) {
            return inv.offhand.get(0);
        }
        return ItemStack.EMPTY;
    }

    public static void setItem(Inventory inv, int unifiedIndex, ItemStack stack) {
        if (unifiedIndex >= 0 && unifiedIndex < 36) {
            inv.items.set(unifiedIndex, stack);
        } else if (unifiedIndex >= 36 && unifiedIndex < 40) {
            inv.armor.set(unifiedIndex - 36, stack);
        } else if (unifiedIndex == 40) {
            inv.offhand.set(0, stack);
        }
    }

    public static List<String> getProtectedSlotIds(int experienceLevel) {
        int startLevel = Config.START_LEVEL.getAsInt();
        int levelsPerSlot = Config.LEVELS_PER_SLOT.getAsInt();
        List<String> slotOrder = Config.SLOT_ORDER.get();

        List<String> protectedSlots = new ArrayList<>();

        if (experienceLevel < startLevel || slotOrder.isEmpty()) {
            return protectedSlots;
        }

        int protectedCount = (experienceLevel - startLevel) / levelsPerSlot + 1;
        protectedCount = Math.min(protectedCount, slotOrder.size());

        for (int i = 0; i < protectedCount; i++) {
            protectedSlots.add(slotOrder.get(i));
        }

        return protectedSlots;
    }

    public static List<Integer> getProtectedUnifiedIndices(int experienceLevel) {
        List<String> slotIds = getProtectedSlotIds(experienceLevel);
        List<Integer> indices = new ArrayList<>();
        for (String slotId : slotIds) {
            Integer index = SLOT_ID_TO_INDEX.get(slotId);
            if (index != null) {
                indices.add(index);
            }
        }
        return indices;
    }
}
