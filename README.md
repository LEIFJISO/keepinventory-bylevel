# Keep Inventory By Level

Minecraft Mod — 按等级逐步保留背包物品死亡不掉落。

- **Minecraft**: 1.21.1
- **加载器**: NeoForge 21.1.227
- **包名**: `com.le.keepinventorybylevel`

---

## 功能

### 等级保护

玩家经验等级达到配置的起始等级后，死亡时逐步保留背包物品，等级越高保留越多。

默认从 **10 级** 开始保护，每升 **1 级** 多保护 1 个格子，保护顺序：

```
副手 → 快捷栏(0~8) → 盔甲栏(头/胸/腿/脚) → 主物品栏(0~26)
```

共 41 个格子，约 50 级即可全部保护。

### 经验损失

死亡损失的经验量可自定义，支持 4 种模式：

| 配置值 | 含义 | 示例 |
|--------|------|------|
| `"50%"` | 损失总经验**点数**的百分比 | 1000XP → 500XP |
| `"10l"` | 损失固定**等级数** | 12级 → 2级 |
| `"100"` | 损失固定**经验点数** | 扣 100 点 |
| `""` (空) | 原版行为，不干预 | — |

默认 `"50%"`。

### 保护图标

打开物品栏时，按 **`** 键（反引号，可修改）切换显示受保护格子的图标 —— 9×9 金色小方块标注在格子左下角。

- 默认：**不显示**
- 按 `` ` `` 切换为显示
- 再按 `` ` `` 切换回隐藏

---

## 配置文件

首次运行后在 `config/keepinventorybylevel-common.toml` 生成：

```toml
[KeepInventoryByLevel]
# 起始保护等级
startLevel = 10
# 每多少级多保护一个格子
levelsPerSlot = 1
# 保护顺序（可自定义调整）
slotOrder = ["offhand", "hotbar.0", "hotbar.1", ...]
# 死亡经验损失
xpLoss = "50%"
```

`slotOrder` 可选值：
- `offhand` — 副手
- `hotbar.0` ~ `hotbar.8` — 快捷栏
- `armor.head` / `armor.chest` / `armor.legs` / `armor.feet` — 盔甲栏
- `inventory.0` ~ `inventory.26` — 主物品栏

---

## 按键

| 按键 | 默认 | 功能 |
|------|------|------|
| 切换保护图标 | `` ` `` (反引号) | 打开物品栏时按，切换显示/隐藏保护图标 |

可在「选项 → 控制 → Keep Inventory By Level」中修改。

---

## 构建

```bash
./gradlew build
```

输出：`build/libs/keepinventorybylevel-1.0.6.jar`
