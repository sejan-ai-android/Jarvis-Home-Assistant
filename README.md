# J.A.R.V.I.S. // AI Assistant

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Gemini](https://img.shields.io/badge/AI-Gemini%20API-FF6F00?logo=google&logoColor=white)](https://ai.google.dev/)
[![Room](https://img.shields.io/badge/Database-Room%20SQLite-4285F4)](https://developer.android.com/training/data-storage/room)
[![Security](https://img.shields.io/badge/Security-AES--256--GCM-00E5FF)](#encrypted-voice-biometric-authentication)

> **"Online and at your service, Sir."**  
> A futuristic, Iron Man-inspired AI assistant built natively for Android using Jetpack Compose and Material Design 3. Powered by Google's Gemini models and local reactive engines, J.A.R.V.I.S. delivers real-time smart home device controls, calendar schedule optimization, confidential project telemetry tracking, email intelligence, and encrypted voice biometric authentication across synchronized peripheral terminals.

---

## ✦ Key Features

### 1. Natural Language & Vocal Assistant
- **Gemini Intelligence**: Connected to Gemini with contextual awareness of your real-time smart home state, agenda, active project milestones, and inbox.
- **Instant Direct Commands**: Fast-path heuristic parser for low-latency commands (*"Turn on lab arc lights"*, *"Optimize today's schedule"*, *"Check urgent emails"*, *"Lock terminal"*).
- **Audio Voice Synthesis**: Built-in Android `TextToSpeech` engine calibrated for a crisp, refined assistant delivery, paired with Android speech recognition.
- **Interactive Arc Reactor Core**: Animated multi-layered vector visualizer with counter-rotating energy tracks, pulsing triangular power core, and real-time audio frequency waveforms.

### 2. Smart Home Matrix & Energy Telemetry
- **Subsystem Controls**: Toggle and modulate Lighting Arrays, HVAC Climate Cores, Perimeter Security Shields, Holographic Workspaces, and Ionic Scrubbers.
- **Live Wattage Telemetry**: Dynamic total power consumption meter tracking active electrical load across the facility.
- **Zonal Filtering**: Filter controls by facility sectors (*Command Lab*, *Living Quarters*, *Perimeter*, *Workshop*).
- **Master Directives**: One-tap *"Engage All"* and *"Eco Standby"* power management protocols.

### 3. Calendar Synchronization & AI Scheduling
- **Daily Agenda Matrix**: Complete timeline tracking commitments, time slots, priority ratings, and categories (*FOCUS*, *MEETING*, *LAB*, *RESEARCH*, *SECURITY*).
- **AI Schedule Suggestions**: Analyzes cognitive load and reorganizes tasks by urgency, scheduling prime deep-work blocks before midday with protective 15-minute decompression buffers.
- **Interactive Management**: Mark tasks complete, delete obsolete items, or schedule new commitments directly from the HUD.

### 4. Personal Projects & Email Triage
- **Project Telemetry Tracker**: Real-time status for confidential initiatives (*Project Vanguard / Armor Mark 85*, *Project Prometheus / Clean Energy Grid*, *Project Synapse*, *Project Aegis*).
  - Progress percentages with interactive sliders.
  - Current operational milestones and pending action alerts.
- **Executive Email Intelligence**:
  - Urgent priority badge filtering and received timestamps.
  - **JARVIS AI Summaries**: Concise, 1-sentence distillations of incoming transmissions.
  - **One-Tap AI Response Dispatch**: Pre-drafted cryptographic responses ready for instantaneous dispatch.

### 5. Encrypted Voice Biometrics & Cross-Device Sync
- **Acoustic Signature Analysis**: Evaluates fundamental frequency centroids (`142.5 Hz`), harmonics-to-noise ratios, and formant structures.
- **Hardware Enclave Tokens**: Generates cryptographic `AES-256-GCM` token hashes tied to your acoustic biometric profile.
- **Cross-Device Authentication Matrix**: Seamless zero-friction authorization synchronized across peripheral terminals:
  - Mobile Terminal (Primary Handheld)
  - Stark Optical Glass HUD (Smart Eyewear)
  - Lab Central Hologram Core (Mainframe)
  - Repulsor Gauntlet Biometric Ring (Wearable)
- **Security Lock State**: Lock the terminal to instantly restrict high-privilege subsystems until verified by spoken passphrase (*"Jarvis, authorize protocol Mark 85"*).

---

## 🛠️ Architecture & Technology Stack

- **UI Framework**: Modern Jetpack Compose with Material 3 Dark HUD Theming.
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with unidirectional data flow and clean repository pattern.
- **State Management**: Kotlin Coroutines & `StateFlow` collected via `collectAsStateWithLifecycle`.
- **Local Persistence**: **Room Database** (SQLite) with pre-populated default profiles and reactive `Flow` observation.
- **AI Integration**: Google Gemini API via secure HTTPS requests with system instruction grounding.
- **Secret Management**: Google Secrets Gradle Plugin reading from `.env` (`BuildConfig.GEMINI_API_KEY`).
- **Audio Pipeline**: Android `TextToSpeech` (UtteranceProgressListener) + Android `SpeechRecognizer` + RMS dB waveform sampling.
- **Graphics & Motion**: Canvas drawing API for the Arc Reactor HUD with infinite rotation and scale transitions.

---

## 📂 Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # Permissions & launcher configuration
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt          # Main edge-to-edge entry point & HUD navigation
│   │   │   │   ├── data/
│   │   │   │   │   ├── audio/
│   │   │   │   │   │   └── SpeechManager.kt         # TextToSpeech & voice recognition engine
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── JarvisDao.kt             # Room DAO for all entities
│   │   │   │   │   │   └── JarvisDatabase.kt        # Room database with initial seeds
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── JarvisModels.kt          # SmartDevice, Task, Project, Email, Biometrics
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   └── JarvisAiEngine.kt        # Gemini API & Natural Language processing
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── JarvisRepository.kt      # Data repository bridging DAO & ViewModel
│   │   │   │   │   └── security/
│   │   │   │   │       └── VoiceBiometricsEngine.kt # Acoustic feature extraction & AES-256 tokens
│   │   │   │   └── ui/
│   │   │   │       ├── JarvisViewModel.kt           # Central ViewModel coordinating features
│   │   │   │       ├── components/
│   │   │   │       │   ├── ArcReactorVisualizer.kt  # Custom Canvas animated HUD visualizer
│   │   │   │       │   └── HudCards.kt              # Reusable cybernetic cards, pills, waveforms
│   │   │   │       ├── screens/
│   │   │   │       │   ├── CommandScreen.kt         # Main conversation & voice HUD
│   │   │   │       │   ├── SmartHomeScreen.kt       # Smart home devices & energy meters
│   │   │   │       │   ├── ScheduleScreen.kt        # Calendar tasks & AI optimization
│   │   │   │       │   ├── ProjectsEmailsScreen.kt  # Project telemetry & email intelligence
│   │   │   │       │   └── BiometricsScreen.kt      # Voice biometrics & multi-device sync
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt                 # Cyber Cyan, Hologram Gold, Titanium Void
│   │   │   │           ├── Theme.kt                 # Material 3 Dark HUD color scheme
│   │   │   │           └── Type.kt                  # Typography definitions
│   │   │   └── res/
│   │   │       ├── drawable/                        # Custom Arc Reactor launcher icons
│   │   │       └── values/strings.xml               # Resource strings
│   │   └── test/
│   │       └── java/com/example/                    # Robolectric & Roborazzi unit tests
├── .env.example                                     # Template for Gemini API key
├── build.gradle.kts                                 # Root Gradle configuration
└── settings.gradle.kts                              # Project settings
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 17 or Java 21
- **Android SDK**: Compile SDK 36, Min SDK 26 (Android 8.0+)

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/jarvis-ai-assistant.git
   cd jarvis-ai-assistant
   ```

2. **Configure Your API Key**:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   Open `.env` and add your Gemini API Key from [Google AI Studio](https://aistudio.google.com/):
   ```properties
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
   *(Note: If no API key is provided, J.A.R.V.I.S. will gracefully fall back to its internal heuristic dialogue generator).*

3. **Build the Project**:
   ```bash
   gradle assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```

5. **Deploy**:
   Install the APK on your connected device or emulator via Android Studio or command line:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🔒 Security & Privacy

- **Hardware Keystore Tokens**: Biometric profiles generate cryptographic hashes designed to simulate secure enclave storage.
- **Least-Privilege Permissions**: Only `RECORD_AUDIO` is requested at runtime for voice input commands; all other permissions (`INTERNET`, `VIBRATE`, `ACCESS_NETWORK_STATE`) are declared strictly for normal operation.
- **Zero Hardcoded Secrets**: Secrets are injected at build-time using `com.google.android.libraries.mapsplatform.secrets-gradle-plugin` to protect API keys.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
