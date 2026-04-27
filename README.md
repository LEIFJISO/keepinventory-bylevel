本项目为Minecraft Mod，游戏版本1.21.1，加载器为neoforge 21.1.227。
包名：com.le.keepinventorybylevel
# 功能
随等级提升，让背包栏位逐渐死亡不掉落。
10级（可通过config配置）开始，首先保护副手，随后保护快捷栏(共9格)，然后保护盔甲栏，最后开始保护背包。
每升一级（可配置需要升几级才增加一次保护的格子数量）多保护一个格子。
参考https://zh.minecraft.wiki/w/%E6%A7%BD%E4%BD%8D
顺序同样可通过config调整。默认顺序如上方所述。
即按照：
offhand,hotbar.0,hotbar.1,hotbar.2,hotbar.3,hotbar.4,hotbar.5,hotbar.6,hotbar.7,hotbar.8,armor.head,armor.chest,armor.legs,armor.feet,inventory.0,inventory.1,...inventory.26

# 表示
打开物品栏时，被保护的格子在左下角显示一个图标。
该功能需要绑定快捷键，仅按下时显示，默认绑定为F1。
在config.json中可以修改快捷键。
```json
{
  "key": "`",
  "description": "显示保护的格子"
}
```