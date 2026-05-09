## [unreleased]

### 🚜 Refactor

- Migrate package name from com.le to com.ocif

### 📚 Documentation

- Generate CHANGELOG with git-cliff
## [1.0.6] - 2026-04-27

### 🚀 Features

- Implement Keep Inventory By Level mod
- Add configurable experience loss on death
- Invert key behavior, add texture icon, fix config comments, bump v1.0.2

### 🐛 Bug Fixes

- Remove @EventBusSubscriber from ClientEventHandler
- Set KeyConflictContext.GUI so hide key works when inventory is open
- Use consumeClick() for toggle key in GUI per NeoForge docs
- Use ScreenEvent.KeyPressed.Pre instead of Post for toggle key

### 📚 Documentation

- Rewrite README with full feature documentation

### ⚙️ Miscellaneous Tasks

- Change default key from F1 to GRAVE (), bump v1.0.4
- Default protection overlay to hidden, toggle to show
