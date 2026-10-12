# Complete Google Play Store Publishing Guide for HostelFix

This guide provides the complete step-by-step instructions to prepare, build, sign, and publish the **HostelFix** Android mobile app to the **Google Play Store**.

---

## 📋 Table of Contents
1. [Prerequisites & Google Play Console Account](#1-prerequisites--account)
2. [Generating the Release Keystore](#2-generating-the-release-keystore)
3. [Building the Production Android App Bundle (.aab)](#3-building-the-bundle)
4. [Google Play Console App Setup](#4-play-console-setup)
5. [Store Listing Copy & Metadata](#5-store-listing-copy)
6. [Graphic Assets Requirements](#6-graphic-assets)
7. [Data Safety Form Answers](#7-data-safety-form)
8. [Permissions & Privacy Policy](#8-permissions--privacy)
9. [Testing & Publishing Tracks](#9-testing--publishing)

---

## 1. Prerequisites & Account

1. **Google Play Developer Account**:
   - Visit [play.google.com/console](https://play.google.com/console).
   - Sign in with your Google account.
   - Pay the one-time $25 registration fee.
   - Complete identity verification.

2. **Backend Server Live on HTTPS**:
   - Google Play apps must communicate with secure HTTPS servers.
   - Deploy the backend to **Render**, **Railway**, or your custom domain (see `backend/Procfile`).
   - Update `buildConfigField("String", "BASE_URL", "\"https://your-backend-app.onrender.com/\"")` in `android/app/build.gradle.kts`.

3. **Google Maps API Key**:
   - Open [Google Cloud Console](https://console.cloud.google.com/).
   - Enable **Maps SDK for Android**.
   - Create an API key and restrict it to your package name `com.hostelfix` and your keystore SHA-1 fingerprint.
   - Place it in `android/local.properties`: `MAPS_API_KEY=AIzaSy...`

---

## 2. Generating the Release Keystore

To publish on Google Play, your app must be signed with a digital certificate.

Run the following command from the `HostelFix/android` folder in your terminal:

```bash
keytool -genkeypair -v -keystore release-keystore.jks -alias hostelfix -keyalg RSA -keysize 2048 -validity 10000
```

- When prompted for a password, enter a secure password (e.g. `HostelFix@2026`).
- Enter your name/organization details.
- Store `release-keystore.jks` securely in `HostelFix/android/release-keystore.jks`.
> ⚠️ **IMPORTANT**: Never commit `release-keystore.jks` or its password to a public GitHub repository. Add it to `.gitignore`.

---

## 3. Building the Production Android App Bundle (.aab)

Google Play requires the **Android App Bundle (.aab)** format instead of APK.

Run the build command from `HostelFix/android`:

### On Windows (PowerShell or CMD):
```powershell
.\gradlew.bat :app:bundleRelease
```

### On macOS / Linux:
```bash
./gradlew :app:bundleRelease
```

Once the build finishes successfully, your release bundle will be generated at:
`HostelFix/android/app/build/outputs/bundle/release/app-release.aab`

---

## 4. Google Play Console App Setup

1. In Play Console, click **Create app**.
2. **App name**: `HostelFix: Campus Care AI` (or `HostelSense`)
3. **Default language**: `English (United States)` or `English (India)`
4. **App or game**: `App`
5. **Free or paid**: `Free`
6. Accept the declarations and click **Create app**.

---

## 5. Store Listing Copy & Metadata

Copy and paste these pre-formatted descriptions directly into Google Play Console:

### App Name (max 30 characters)
```
HostelFix: Campus Care AI
```

### Short Description (max 80 characters)
```
AI-powered, GPS-verified hostel complaint management for campus residents.
```

### Full Description (max 4000 characters)
```
Managing hostel complaints has never been easier or faster. HostelFix (HostelSense) is an intelligent, digital complaint tracking platform designed specifically for university hostel residents.

Say goodbye to physical paper registers and uncoordinated messages. With HostelFix, every resident issue is verified, analyzed, routed, and resolved with full accountability.

KEY FEATURES:
📍 GPS Hostel Verification:
Confirm that you are physically present in your hostel block using Google Maps geofencing. Verified complaints are automatically prioritized by university maintenance.

🤖 Native AI & Natural Language Processing:
- Automated Category Classification (Plumbing, Electrical, Carpentry, Wi-Fi, Housekeeping).
- Instant Priority Prediction (Low, Medium, High, Critical).
- Smart Duplicate Detection: Identifies recurring issues in your block and keeps you updated.
- Automated Department Routing to civil, electrical, or network teams.

📸 Photo Evidence Upload:
Attach clear photos of defective taps, loose switchboards, or damaged furniture directly from your camera or gallery.

📊 Live Status Timeline:
Track your issue in real time from submission to AI analysis, staff assignment, in-progress repairs, and final resolution.

⭐ Student Rating & Feedback:
Rate the quality of the repair (1 to 5 stars) once completed, ensuring transparency and high campus maintenance standards.

🔔 Real-Time Alerts:
Receive push notifications via Firebase Cloud Messaging whenever a warden or technician updates your ticket.

Designed with modern Material 3 and Jetpack Compose for a seamless student experience.
```

---

## 6. Graphic Assets Requirements

Upload these in **Store Presence > Main store listing**:

| Asset | Dimensions | Format | Notes |
|---|---|---|---|
| **App Icon** | 512 x 512 px | PNG (32-bit) | Max 1 MB, no transparent background |
| **Feature Graphic** | 1024 x 500 px | JPG or PNG | Banner displayed at top of store listing |
| **Phone Screenshots** | Min 2, Max 8 | JPG or PNG | 16:9 or 18:9 aspect ratio (e.g. 1080 x 2400 px) |

> 💡 **Tip**: Capture screenshots directly from the Android Emulator:
> 1. Home Screen with verification badge & stat cards
> 2. GPS Location Verification Google Map
> 3. New Complaint Submission form with photo
> 4. Complaint Detail Screen showing AI Insights & Timeline
> 5. 5-Star Feedback dialog

---

## 7. Data Safety Form Answers

Google Play requires completing the **Data Safety** questionnaire in Play Console. Here are the exact answers to select:

1. **Does your app collect or share user data?** -> **Yes**
2. **Is all user data encrypted in transit?** -> **Yes** (HTTPS/TLS)
3. **Do you provide a way for users to request data deletion?** -> **Yes**

### Specific Data Types Collected:
- **Location**:
  - `Approximate Location`: Collected -> App functionality (GPS hostel verification)
  - `Precise Location`: Collected -> App functionality (geofence radius check). *Ephemeral/Not shared.*
- **Personal Info**:
  - `Name`: Collected -> Account management
  - `Email Address`: Collected -> Account management & login
  - `Phone Number`: Collected -> Account management (staff contact for repairs)
- **Photos and Videos**:
  - `Photos`: Collected -> Optional user-submitted complaint attachments.

---

## 8. Permissions & Privacy Policy

### Privacy Policy URL
Google Play strictly requires a live URL. Use your deployed backend endpoint:
```
https://your-domain.com/privacy-policy
```
*(The backend template is already built and live at `/privacy-policy`)*

### Location Permission Declaration
- **Foreground Location Only**: HostelFix only accesses location while the app is actively open and in use (`ACCESS_FINE_LOCATION`).
- **Does the app access location in the background?**: Select **No**.
  *(This avoids Google's rigorous Background Location Declaration review and guarantees fast approval!)*

---

## 9. Testing & Publishing Tracks

Google Play policies for personal developer accounts created after November 2023 require testing before public production release:

1. **Closed Testing Track**:
   - Create a **Closed Testing** release.
   - Upload `app-release.aab`.
   - Add 20 testers (classmates/friends with Gmail accounts).
   - Testers opt-in and use the app for 14 days.
2. **Apply for Production Access**:
   - Answer the short Google Play questionnaire about your testing feedback.
   - Google approves production access (usually 2-3 business days).
3. **Rollout to Production**:
   - Promote the release to **Production** track.
   - Your app goes live on the Google Play Store worldwide!
