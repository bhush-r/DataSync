# DataSync — Data Synchronization App

DataSync is a production-ready Kotlin Android application built using **MVVM Architecture** and **Clean Architecture** principles. It features real-time, bidirectional synchronization of device telemetry (Contacts, Call Logs, and SMS) with **Firebase Firestore**, coupled with **Role-Based Access Control (RBAC)** for Admin and User roles, background automation, and offline functionality.

---

## Features

**Authentication & Session Management**
- Single-screen unified Login & Registration powered by Firebase Authentication.
- Dynamic role evaluation on sign-in (`ADMIN` for `@datasync.com` domain emails, `USER` for standard accounts).
- Session persistence using Jetpack DataStore and secure token storage.
- Device FCM token registration upon login.

**Role-Based Access Control (RBAC)**
- **Admin Role:** Access to the top-right 3-Dot Admin Panel, live user directory, and Development Mode switch.
- **User Role:** Restricted access with dedicated standard dashboard metrics and user features.
- **Development Mode Indicator:** Live status badge on the Dashboard for Admin users.

**Dashboard & Metrics**
- Real-time counters for Contacts, Call Logs, and SMS.
- Server-side `count()` aggregation queries to optimize Firestore reads by 90%+.
- Full Last Sync Date & Time timestamping (`dd MMM yyyy, hh:mm a`).
- Manual "Sync Now" trigger with deterministic progress tracking.

**Device Telemetry Read & Sync**
- **Contacts:** Scans on-device address book; syncs deduplicated contacts via phone normalization.
- **Call Logs:** Syncs incoming, outgoing, and missed call history with call durations.
- **SMS:** Reads inbox messages and generates cryptographic hashes to prevent duplicates.
- **Real-time Observer:** `DataSyncObserverService` monitors system `ContentObserver` events to automatically trigger background syncs when new calls or messages occur (with a 5-second debounce buffer).

**Records CRUD Operations**
- Add, View, Edit, and Delete records in Firestore with confirmation dialogs and search filtering.

**Error Handling & UX**
- Offline support via local Firestore cache.
- Custom `Resource<T>` loading state indicators across all network and sync flows.
- Clean status bar padding (`WindowInsetsCompat`) preventing layout clipping.

---

## Real-time Auto Sync & Performance Optimizations

1. **Debounced Real-time ContentObserver** (`DataSyncObserverService`):
   - Listens for changes on `CallLog.Calls.CONTENT_URI` and `Telephony.Sms.CONTENT_URI`.
   - Features a 5-second debounce buffer to bundle rapid telemetry events (such as active call status changes or incoming multi-part SMS) into a single background sync pass, avoiding rate-limit hits.
2. **90%+ Firestore Quota Reduction** (`count()` Aggregation API):
   - Uses Firestore's server-side `.count().get(AggregateSource.SERVER)` aggregation query instead of fetching full document trees.
   - Loading total stats for 2,000+ items consumes only 3 reads instead of 2,000+ document reads.

---

## How Role-Based Authentication Works

- **Admin Role Registration/Login:**
   - Logging in or registering with any email ending in `@datasync.com` (e.g., `admin@datasync.com`) automatically sets the account role to `ADMIN`.
   - Admin UI Features: Shows `Role: ADMIN`, renders the Development Mode status card on the Dashboard, and enables the Admin Panel in the top 3-dot menu.
- **Standard User Account:**
   - Registering or logging in with any other email (e.g., `user@gmail.com`) assigns the `USER` role.
   - User UI Features: Shows `Role: USER`, hides Development Mode, and removes access to the Admin Panel.

---

## Architecture Overview

The app follows **MVVM (Model-View-ViewModel)** with Clean Architecture layering:

```
com.bhushan.datasync
├── DataSyncApplication.kt         # Hilt Application entry point & WorkManager setup
├── data
│   └── repository                 # Repositories (Firestore batching & ContentResolver calls)
│       ├── AuthRepositoryImpl.kt
│       ├── CallLogRepositoryImpl.kt
│       ├── ContactRepositoryImpl.kt
│       ├── DashboardRepositoryImpl.kt
│       ├── RecordRepositoryImpl.kt
│       ├── RegisterRepositoryImpl.kt
│       ├── SmsRepositoryImpl.kt
│       └── UserRepositoryImpl.kt
├── di                             # Dependency Injection Modules (Hilt)
│   ├── AppModule.kt               # Provides WorkManager
│   ├── FirebaseModule.kt          # Provides Auth, Firestore, Messaging singletons
│   └── RepositoryModule.kt        # Binds abstract repository interfaces
├── domain
│   ├── model                      # Core Entities (User, ContactItem, CallLogItem, SmsItem, RecordItem)
│   └── repository                 # Abstract contracts and interface definitions
├── permission                     # Centralized Runtime Permission Management
│   └── PermissionManager.kt
├── presentation                   # View Layer (Activities, Fragments, ViewModels, Adapters)
│   ├── admin                      # Admin Panel, Users List Adapter & ViewModel
│   ├── auth                       # Unified Login & Registration Activities + ViewModels
│   ├── calllogs                   # Call Log Fragment with Chip filters & Search
│   ├── common                     # BaseActivity with authentication lifecycle guard
│   ├── contacts                   # Synced Contacts Fragment & Search
│   ├── dashboard                  # Dashboard Fragment with metrics & Sync progress
│   ├── records                    # Records CRUD BottomSheet & List Fragment
│   ├── sms                        # Synced SMS Messages Fragment
│   └── splash                     # Splash Activity & Session Router
├── receiver                       # Broadcast Receivers
│   └── BootCompletedReceiver.kt   # Reschedules periodic sync jobs on device boot
├── service                        # Background Services
│   ├── DataSyncFcmService.kt      # Remote FCM messaging & push notifications
│   └── DataSyncObserverService.kt # Real-time ContentObserver for SMS & Calls
├── sync                           # Sync Operations Engine
│   ├── SyncManager.kt             # Orchestrates Contacts -> Call Logs -> SMS pipeline
│   ├── SyncScheduler.kt           # Enqueues WorkManager background jobs
│   └── SyncWorker.kt              # Hilt-assisted CoroutineWorker
└── utils                          # Helpers, Extensions, DataStore SessionManager, DateUtils
```

---

## Tech Stack & Libraries Used

| Component | Library / Framework | Purpose |
|---|---|---|
| Language | Kotlin 1.9+ | Primary language |
| Architecture | MVVM + Clean Architecture | Separation of concerns |
| DI Engine | Google Hilt | Dependency Injection |
| Database & Auth | Firebase Authentication & Cloud Firestore | User management & NoSQL database |
| Background Sync | AndroidX WorkManager (Hilt Integration) | Scheduled periodic and one-time tasks |
| Push Notifications | Firebase Cloud Messaging (FCM) | Remote sync triggers & notifications |
| Navigation | Jetpack Navigation Component | Single-activity fragment routing |
| Local Cache | Jetpack DataStore Preferences | Offline-first session state caching |
| UI Components | Material Components 3 & View Binding | Modern Android design system |
| Coroutines & Flow | Kotlin Coroutines & StateFlow | Asynchronous data streams |

---

## Project Setup Instructions

### Prerequisites
- **Android Studio:** Studio Ladybug / Jellyfish (2024.1+) or newer.
- **JDK:** Version 17.
- **SDK:** Target SDK 34/35, Minimum SDK 24 (Android 7.0).

### Build & Run

**1. Clone the Repository:**
```bash
git clone https: https://github.com/bhush-r/DataSync.git
cd DataSync
```

**2. Open in Android Studio:**
- Select `File → Open` and choose the DataSync root directory.

**3. Gradle Sync:**
- Allow Gradle to fetch dependencies automatically.

**4. Build APK:**
- Go to `Build → Build Bundle(s) / APK(s) → Build APK(s)`.

---

## Firebase Configuration Steps

**1. Create Firebase Project:**
- Go to the [Firebase Console](https://console.firebase.google.com/) and create a project named `DataSync`.

**2. Add Android App:**
- Package Name: `com.bhushan.datasync.debug` (or `com.bhushan.datasync`).

**3. Generate & Add SHA-1 Fingerprint:**
- Open terminal inside Android Studio and run:
```bash
./gradlew signingReport
```
- Copy the SHA-1 fingerprint from the debug build variant and paste it in `Firebase Console → Project Settings → Add Fingerprint`.

**4. Download Configuration File:**
- Download `google-services.json` and place it inside the `app/` folder.

**5. Configure Firestore Security Rules:**
- Go to `Firestore Database → Rules` tab and deploy the following rule-set:

```javascript

```

---

## Screenshots & Application Flows

*(Replace the placeholder links below with your actual project screenshots or image paths)*

| 1. Splash Screen | 2. Login | 3. Registration |
|---|---|---|
| <img src="https://github.com/user-attachments/assets/ddcac1c3-a1f3-47b4-889a-533200305593" width="220" alt="Splash Screen"/> | <img src="https://github.com/user-attachments/assets/51023e2f-613c-4dac-84c2-66a055f56614" width="220" alt="Login"/> | <img src="https://github.com/user-attachments/assets/1b66ea71-6309-412b-b4e0-49514daf794c" width="220" alt="Registration"/> |
| Branded launch screen with session check | Unified email/password sign-in | New account creation form |

| 4. User Dashboard | 5. Admin Dashboard | 6. Admin Panel & Users |
|---|---|---|
| <img src="https://github.com/user-attachments/assets/30f3edc0-ce25-49cc-bb07-024268b13fa2" width="220" alt="User Dashboard"/> | <img src="https://github.com/user-attachments/assets/149f599e-bb4d-4fab-913d-91334a52ddee" width="220" alt="Admin Dashboard"/> | <img src="https://github.com/user-attachments/assets/2f1d04f4-0a74-4198-8441-62583fc49882" width="220" alt="Admin Panel"/> |
| Role: USER, Dev Mode hidden | Role: ADMIN with Dev Mode Card | Live User Directory & Toggle Switch |

| 7. Synced Contacts | 8. Synced Call Logs | 9. Synced SMS Messages |
|---|---|---|
| <img src="https://github.com/user-attachments/assets/ff36c2fe-35e2-4d51-8f6c-2a1ecbb3b43a" width="220" alt="Synced Contacts"/> | <img src="https://github.com/user-attachments/assets/f792c161-5aad-44cc-b623-9453a87d862c" width="220" alt="Synced Call Logs"/> | <img src="https://github.com/user-attachments/assets/0fdfc650-85d1-4021-9575-4114df601791" width="220" alt="Synced SMS Messages"/> |
| Address book with search | Incoming/Outgoing/Missed filters | SMS preview & timestamps |

| 10. Records CRUD & Edit | 11. Logout | 12. Runtime Permissions |
|---|---|---|
| <img src="https://github.com/user-attachments/assets/ecbdd97a-c98b-4f7d-b99c-d5bb3215e310" width="220" alt="Records CRUD & Edit"/> | <img src="https://github.com/user-attachments/assets/bdea8288-394e-4adc-b183-ed38f243110a" width="220" alt="Logout"/> | <img src="https://github.com/user-attachments/assets/ac21872e-fe1d-4d65-abcc-5507d93b3d4c" width="220" alt="Runtime Permissions"/> |
| BottomSheet Add/Edit form | Logout confirmation alert | Contacts, Call Logs & SMS grant |

---
**Screenshot of APK View:** [View APK Screenshot](https://drive.google.com/drive/folders/1Q8m5tMWEsGmeUub66Ku-J2Brv8CHTzSr?usp=sharing)

## Output APK Path & Direct APK Link

Upon building the project, the signed/debug APK can be located at:

```
app/build/outputs/apk/debug/app-debug.apk
```

**Direct APK download:** [Download DataSync APK](https://drive.google.com/file/d/15qT7dLhkEGVREcbugdmZID6S1z18-bEv/view?usp=sharing)

