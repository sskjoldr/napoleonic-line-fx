# Napoleonic Line Effects / Эффекты наполеоновской линии

Client-only smoke and bullet tracers for [Napoleonic Line Infantry](https://github.com/sskjoldr/napoleonic-line). Visual only: powder smoke, muzzle flash, and tracers. No sounds. Without this jar the main mod uses ordinary campfire smoke and no tracers.

Клиентский подмод дыма и трассеров для основного мода [napoleonic-line](https://github.com/sskjoldr/napoleonic-line). Только визуал: пороховой дым, вспышка у дула и трассеры, без звуков. Без этого мода основной мод рисует обычный дым костра и не рисует трассеры.

## Requirements / Требования

- Minecraft 1.20.1, Forge 47.x (47.4.20), Java 17
- Main mod **napoleonic_line** 1.0.0+ (required at runtime; this jar is client-only)
- Villager Recruits 1.15.2 and ewewukek's Musket Mod are dependencies of the **main** mod, not of this one

Install `napoleonic_line-1.1.0.jar` on the server and the client, and `napoleonic_line_fx-1.1.0.jar` on the client only.

Since 1.1.0 the smoke, muzzle flash, and tracers also appear on shots from Epic Knights: Thirty Years War guns (matchlock musket, wheellock pistol) when the main mod 1.1.0 and Thirty Years War are installed. The shot sound always comes from the gun or the main mod; this jar stays visual only.

С 1.1.0 дым, вспышка и трассеры появляются и на выстрелах мушкетов Epic Knights: Thirty Years War (нужен основной мод 1.1.0). Звук выстрела даёт само оружие или основной мод, этот мод остаётся только визуальным.

## Build / Сборка

This repository is self-contained. It compiles against copies of three main-mod API classes (`MusketShotClientEvent`, `NapoleonicClientConfig`, `WindManager`) under `src/api/java`. Those classes are **excluded from the built jar**; the main mod provides them at runtime. Keep them in sync if the main API changes.

```bash
./gradlew build
```

The jar is written to `build/libs/napoleonic_line_fx-1.1.0.jar`.

## License

MIT.
