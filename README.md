# Napoleonic Line Effects / Эффекты наполеоновской линии

Client-only smoke and bullet tracers for [Napoleonic Line Infantry](https://github.com/sskjoldr/napoleonic-line). Without this jar the main mod uses ordinary campfire smoke, no tracers, and arquebus shots are silent: the arquebus sound `musket_fire_4.ogg` ships in this jar.

Клиентский подмод дыма и трассеров для основного мода [napoleonic-line](https://github.com/sskjoldr/napoleonic-line). Без этой банки основной мод рисует обычный дым костра, не рисует трассеры, а аркебузы стреляют беззвучно: звук `musket_fire_4.ogg` вшит в этот мод.

## Requirements / Требования

- Minecraft 1.20.1, Forge 47.x (47.4.20), Java 17
- Main mod **napoleonic_line** 1.0.0+ (required at runtime; this jar is client-only)
- Villager Recruits 1.15.2 and ewewukek's Musket Mod are dependencies of the **main** mod, not of this one

Install `napoleonic_line-1.0.0.jar` on the server and the client, and `napoleonic_line_fx-1.0.0.jar` on the client only.

## Build / Сборка

This repository is self-contained. It compiles against copies of three main-mod API classes (`MusketShotClientEvent`, `NapoleonicClientConfig`, `WindManager`) under `src/api/java`. Those classes are **excluded from the built jar**; the main mod provides them at runtime. Keep them in sync if the main API changes.

```bash
./gradlew build
```

The jar is written to `build/libs/napoleonic_line_fx-1.0.0.jar`.

## License

MIT. Arquebus sound: [musket_fire_4.wav (#648747) by J8054](https://freesound.org/people/J8054/sounds/648747/), [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
