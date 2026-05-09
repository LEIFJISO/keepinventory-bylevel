# Changelog

### <!-- 2 -->🚜 Refactor

- migrate package name from com.le to com.ocif

### <!-- 0 -->🚀 Features

- implement Keep Inventory By Level mod
- add configurable experience loss on death
- invert key behavior, add texture icon, fix config comments, bump v1.0.2

### <!-- 1 -->🐛 Bug Fixes

- remove @EventBusSubscriber from ClientEventHandler
- set KeyConflictContext.GUI so hide key works when inventory is open
- use consumeClick() for toggle key in GUI per NeoForge docs
- use ScreenEvent.KeyPressed.Pre instead of Post for toggle key

### <!-- 3 -->📚 Documentation

- rewrite README with full feature documentation

### <!-- 7 -->⚙️ Miscellaneous Tasks

- change default key from F1 to GRAVE (), bump v1.0.4
- default protection overlay to hidden, toggle to show
