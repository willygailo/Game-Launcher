## 🎮 Game Launcher PRO v19.9.7 (Build 1997)

### 🦅 MLBB 2026 Mod Menu & Combat Matrix Overdrive
- **Panoramic 10.0X Drone View**: Added ultra-panoramic `10.0X` zoom tier (`battle_10x.bytes`) alongside 1.5X, 2X, 3X, 4X, and 5X. Dynamic camera height scaling up to 500 with zero lag, anti-blackscreen protection, and full map/mode compatibility (Rank, Classic, Custom, Practice).
- **Extra Damage Multiplier (10% - 1000% / 10x)**: Full IL2CPP RVA hook (`0x0182C4D0`) with dynamic ARM64 pattern scanning fallback for universal hero damage scaling.
- **Extra Attack Speed Multiplier (0.5x - 5.0x)**: Real-time attack animation rate overdrive via `HeroAttackSpeed_GetMultiplier` (`0x01831E20`).
- **Extra Defense & Damage Mitigation (0.5x - 5.0x)**: Integrated `HeroDefense_GetArmor` (`0x01859E40`) hook and Master Combat Matrix physical/magic armor overrides.
- **Cooldown Reduction Slider (0% - 100%)**: Flexible skill cooldown scaling via `SkillManager_GetCooldownTime` (`0x0184A100`) with instant near-zero cooldown toggle.
- **Infinite Mana & Infinite Energy**: Continuous pool regeneration hooking via `ManaEnergyRegen_GetRate` (`0x01882350`) with zero skill cost enforcement.
- **Universal Mode & Hero Coverage**: Applies to every hero across all roles (Assassin, Fighter, Mage, Marksman, Tank, Support) in all game modes.

### 🎨 UI & Design Refresh
- High-resolution anime mascot backgrounds deployed for Home (`bg_home.jpg`) and Settings (`bg_settings.jpg`).
- Modernized Game Mod Fragment with dedicated cyber sliders and real-time multiplier indicators.
- Version bumped to `19.9.7` (Version Code `1997`).
