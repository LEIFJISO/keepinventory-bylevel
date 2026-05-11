package com.ocif.keepinventorybylevel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

public class DeathEventHandler {
    public static final DeathEventHandler INSTANCE = new DeathEventHandler();

    private static final String MOD_TAG = "keepinventorybylevel";
    private static final String ITEMS_KEY = "protected_items";
    private static final String XP_LEVEL_KEY = "xp_level";
    private static final String XP_PROGRESS_KEY = "xp_progress";

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String xpLossConfig = Config.XP_LOSS.get().trim();
        CompoundTag modTag = new CompoundTag();

        if (!xpLossConfig.isEmpty()) {
            modTag.putInt(XP_LEVEL_KEY, player.experienceLevel);
            modTag.putFloat(XP_PROGRESS_KEY, player.experienceProgress);
        }

        int level = player.experienceLevel;
        List<String> protectedSlotIds = SlotProtectionManager.getProtectedSlotIds(level);

        if (!protectedSlotIds.isEmpty()) {
            ListTag itemsList = new ListTag();
            Inventory inv = player.getInventory();

            for (String slotId : protectedSlotIds) {
                int index = SlotProtectionManager.getUnifiedIndex(slotId);
                ItemStack stack = SlotProtectionManager.getItem(inv, index).copy();
                if (!stack.isEmpty()) {
                    CompoundTag entry = new CompoundTag();
                    entry.putInt("slot", index);
                    Tag itemTag = stack.save(player.registryAccess());
                    if (itemTag != null) {
                        entry.put("item", itemTag);
                        itemsList.add(entry);
                    }
                    SlotProtectionManager.setItem(inv, index, ItemStack.EMPTY);
                }
            }

            if (!itemsList.isEmpty()) {
                modTag.put(ITEMS_KEY, itemsList);
            }
        }

        if (!modTag.isEmpty()) {
            player.getPersistentData().put(MOD_TAG, modTag);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer newPlayer) || !(event.getOriginal() instanceof ServerPlayer oldPlayer)) {
            return;
        }

        CompoundTag modTag = oldPlayer.getPersistentData().getCompound(MOD_TAG);

        if (modTag.isEmpty()) {
            return;
        }

        if (modTag.contains(ITEMS_KEY, Tag.TAG_LIST)) {
            ListTag itemsList = modTag.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
            Inventory inv = newPlayer.getInventory();
            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag entry = itemsList.getCompound(i);
                int index = entry.getInt("slot");
                ItemStack existing = SlotProtectionManager.getItem(inv, index);
                if (!existing.isEmpty()) {
                    newPlayer.drop(existing, true, false);
                }
                Tag itemTag = entry.get("item");
                if (itemTag != null) {
                    ItemStack item = ItemStack.parse(newPlayer.registryAccess(), itemTag).orElse(ItemStack.EMPTY);
                    SlotProtectionManager.setItem(inv, index, item);
                }
            }
        }

        if (modTag.contains(XP_LEVEL_KEY, Tag.TAG_INT)) {
            int oldLevel = modTag.getInt(XP_LEVEL_KEY);
            float oldProgress = modTag.getFloat(XP_PROGRESS_KEY);
            applyXpLoss(newPlayer, new PlayerXpSnapshot(oldLevel, oldProgress));
        }

        oldPlayer.getPersistentData().remove(MOD_TAG);
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
