# RAAHAT AI — Urban Flood Response Intelligence

RAAHAT AI is a premium Android emergency orchestration demo for Islamabad. It fuses citizen, rainfall, traffic, drainage, and road-risk signals; forecasts flood spread; compares response strategies; orchestrates a simulated response; and measures before/after impact.

## Run

1. Open this directory in Android Studio, or run `./gradlew :app:assembleDebug`.
2. Install with `./gradlew :app:installDebug` while an emulator/device is connected.
3. Launch **RAAHAT AI** and choose **Launch Demo Scenario** for the deterministic presentation flow.

The installable debug APK is also available in `outputs/RAAHAT-AI-debug.apk`.

## Architecture

- Jetpack Compose + Material 3 UI
- `RaahatViewModel` owns navigation and timed demo state
- Deterministic weighted `SignalFusionEngine` is the safety/consistency layer
- Provider interfaces isolate citizen, weather, traffic, drainage, alerts, and dispatch
- `AIReasoningService` supports a Gemini-backed implementation with local fallback
- `AIOutputValidator` validates confidence, reasoning, actions, and forecast probability

The demo requires no credentials or network access. Live Firebase, Maps, and Gemini implementations can replace provider interfaces without altering the presentation flow.
