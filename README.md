# Napoleonic Line Effects / Эффекты наполеоновской линии

Client-only smoke and bullet tracers for [Napoleonic Line Infantry](https://github.com/sskjoldr/napoleonic-line). Visual only: powder smoke, muzzle flash, and tracers. No sounds. Without this jar the main mod uses ordinary campfire smoke and no tracers.

Клиентский подмод дыма и трассеров для основного мода [napoleonic-line](https://github.com/sskjoldr/napoleonic-line). Только визуал: пороховой дым, вспышка у дула и трассеры, без звуков. Без этого мода основной мод рисует обычный дым костра и не рисует трассеры.

## Requirements / Требования

- Minecraft 1.20.1, Forge 47.x (47.4.20), Java 17
- Main mod **napoleonic_line** 1.0.0+ (required at runtime; this jar is client-only)
- Villager Recruits 1.15.2 and ewewukek's Musket Mod are dependencies of the **main** mod, not of this one

Install `napoleonic_line-1.0.1.jar` on the server and the client, and `napoleonic_line_fx-1.0.1.jar` on the client only.

## Build / Сборка

This repository is self-contained. It compiles against copies of three main-mod API classes (`MusketShotClientEvent`, `NapoleonicClientConfig`, `WindManager`) under `src/api/java`. Those classes are **excluded from the built jar**; the main mod provides them at runtime. Keep them in sync if the main API changes.

```bash
./gradlew build
```

The jar is written to `build/libs/napoleonic_line_fx-1.0.1.jar`.

## License

MIT.
