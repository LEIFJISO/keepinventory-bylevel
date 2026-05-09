package com.ocif.keepinventorybylevel;

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
    private final Map<UUID, PlayerXpSnapshot> xpSnapshots = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String xpLossConfig = Config.XP_LOSS.get().trim();
        if (!xpLossConfig.isEmpty()) {
            xpSnapshots.put(player.getUUID(), new PlayerXpSnapshot(
                    player.experienceLevel,
                    player.experienceProgress
            ));
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
        if (saved != null && !saved.isEmpty()) {
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

        PlayerXpSnapshot snapshot = xpSnapshots.remove(newPlayer.getUUID());
        if (snapshot != null) {
            applyXpLoss(newPlayer, snapshot);
        }
    }

    private void applyXpLoss(ServerPlayer player, PlayerXpSnapshot snapshot) {
        String xpLossConfig = Config.XP_LOSS.get().trim();

        if (xpLossConfig.isEmpty()) {
            return;
        }

        int oldLevel = snapshot.level;
        float oldProgress = snapshot.progress;

        if (xpLossConfig.endsWith("%")) {
            try {
                double percentage = Double.parseDouble(xpLossConfig.replace("%", "")) / 100.0;
                int totalOldPoints = levelToTotalXp(oldLevel) + (int)(getXpForLevel(oldLevel + 1) * oldProgress);
                int lostPoints = (int)(totalOldPoints * percentage);
                int newPoints = Math.max(0, totalOldPoints - lostPoints);
                setPlayerXpFromTotal(player, newPoints);
            } catch (NumberFormatException ignored) {
            }
        } else if (xpLossConfig.endsWith("l") || xpLossConfig.endsWith("L")) {
            try {
                int levelsToLose = Integer.parseInt(xpLossConfig.replaceAll("[lL]", ""));
                int newLevel = Math.max(0, oldLevel - levelsToLose);
                setPlayerXp(player, newLevel, oldProgress);
            } catch (NumberFormatException ignored) {
            }
        } else {
            try {
                int pointsToLose = Integer.parseInt(xpLossConfig);
                int totalOldPoints = levelToTotalXp(oldLevel) + (int)(getXpForLevel(oldLevel + 1) * oldProgress);
                int newPoints = Math.max(0, totalOldPoints - pointsToLose);
                setPlayerXpFromTotal(player, newPoints);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void setPlayerXp(ServerPlayer player, int level, float progress) {
        player.experienceLevel = level;
        player.experienceProgress = progress;
        player.totalExperience = levelToTotalXp(level) + (int)(getXpForLevel(level + 1) * progress);
    }

    private static int getXpForLevel(int level) {
        if (level <= 16) {
            return 2 * level + 7;
        } else if (level <= 31) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    private static int levelToTotalXp(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int)(2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int)(4.5 * level * level - 162.5 * level + 2220);
        }
    }

    private void setPlayerXpFromTotal(ServerPlayer player, int totalXp) {
        int level = 0;
        while (totalXp >= getXpForLevel(level)) {
            totalXp -= getXpForLevel(level);
            level++;
        }
        int xpForNext = getXpForLevel(level);
        float progress = xpForNext > 0 ? (float) totalXp / xpForNext : 0f;
        setPlayerXp(player, level, progress);
    }

    private record PlayerXpSnapshot(int level, float progress) {}
}
