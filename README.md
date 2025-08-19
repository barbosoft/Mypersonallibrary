# MyPersonalLibrary

Aplicació Android per gestionar una biblioteca personal.

[![Android CI](https://github.com/barbosoft/Mypersonallibrary/actions/workflows/android-ci.yml/badge.svg)](https://github.com/barbosoft/Mypersonallibrary/actions/workflows/android-ci.yml)

## ✨ Funcionalitats
- Llista de llibres amb **cerca** i **ordre** (Títol / Autor / ISBN).
- Formulari de creació/edició.
- **Escàner d’ISBN** amb **CameraX + ML Kit**.
- **Pull-to-refresh** amb **Material3** (`PullToRefreshBox`).
- Navegació amb **Navigation Compose**.
- HTTP amb **Retrofit** i imatges amb **Coil**.
- **Tests** unitaris i **instrumentats** (Compose UI Test).

## 🧱 Stack
Kotlin · Jetpack **Compose** · **Material3** · **Navigation** · **ViewModel/StateFlow** · **Coroutines**  
**CameraX**, **ML Kit Barcode** · **Retrofit** · **Coil**

## 🗂 Estructura
```text
org.biblioteca.mypersonallibrary
 ├─ data/                # DTOs, Retrofit, repositori
 ├─ domain/              # Filtrat/ordre de llibres
 ├─ navigation/          # Rutes (sealed class Screen)
 ├─ scanner/             # ScanActivity (CameraX + ML Kit)
 ├─ ui/
 │   ├─ components/      # SearchField, OrderDropdown, BooksList, ...
 │   └─ screens/         # LlibreListScreen, LlibreFormScreen, ...
 └─ viewModel/           # LlibreViewModel

▶️ Execució local

Requisits: Android Studio Jellyfish o superior i JDK 17.
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

🧪 Tests

Unitaris (JVM)
./gradlew test

Instrumentats (emulador)
./gradlew connectedAndroidTest
