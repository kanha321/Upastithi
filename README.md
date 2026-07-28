# 📘 Upasthiti (v3.0.0)

[![Version](https://img.shields.io/badge/version-v3.0.0-blue.svg)](https://github.com/kanha321/Upastithi/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF.svg)](https://kotlinlang.org/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%7C%20Voyager%20%7C%20RoomDB-00C853.svg)](https://developer.android.com/training/data-storage/room)

_**Upasthiti** (उपस्थिति)_ is an Android application designed for university students to manage academic timetables and track class attendance.

Unlike traditional calendar apps that require manual data entry, **Upasthiti** uses an **on-device PDF spatial grid parser** to read official university timetable PDFs in seconds. Once imported, it generates daily schedules, tracks subject-wise attendance with margin safety calculations, supports timetable editing, and enables data sharing via custom **`.upasthiti`** files.

---

## 📌 Features

### 📄 1. On-Device PDF & JSON Timetable Parser
- **Local PDF Parsing**: Processes official university timetable PDFs on-device using spatial text-bounding box heuristics — no cloud or server required.
- **Grid Layout Reconstruction**: Automatically groups text into time slots, course codes, course titles, class types (Lecture vs Practical/Lab), room numbers, and faculty details.
- **Multi-Timetable Scanning**: Scans multi-page PDFs, detects multiple department/semester timetables, and lets students select their section.
- **First-Launch Onboarding**: Splash screen verifies if a timetable is loaded and prompts to upload a `.pdf` or `.upasthiti` file if missing.

### 📊 2. Attendance Tracking & Per-Subject Modes
- **Class Attendance Stepper**: Mark classes as **Present**, **Absent**, or **Cancelled** with group/batch tracking.
- **Per-Subject Calculation Modes**:
  - **`Per-Slot` Mode**: Each 1-hour slot counts as 1 separate class (e.g., 2 slots = 2 classes).
  - **`Per-Day` Mode**: Groups all slots for a subject on the same date into 1 day. Attending at least 1 slot on that date counts the day as Present.
  - Mode selection is customizable per-subject and remembered across restarts.
- **Subject Analytics & Safe Margins**: Calculates attendance percentages, total lectures conducted, present/absent counts, and safe margin thresholds (classes you can skip or need to attend to reach 75%).
- **Room DB Persistence**: Thread-safe SQLite local storage via Room DB for all attendance records.

### 📅 3. Schedule & Calendar Pager
- **Day Pager**: View daily class schedules with room numbers, teacher names, class type, and group/batch tags (e.g. `Group G1`).
- **Date Indicators**: Visual status indicators distinguishing past/current dates, future dates, and out-of-month dates.

### ✏️ 4. Timetable Customization & Class Editor
- **Class Editor**: Add new classes, edit existing slots, change locations, shift class timings/rooms, or delete single time slots.
- **Bidirectional Course Code ↔ Title Autofill**: Selecting a Course Code (e.g., `CS11101`) automatically populates the Course Title (`Programming for Problem Solving`) and vice versa.
- **Teacher Initials Filter**: Filters out short initials (e.g. `AK`, `SKS`) to show full professor names.
- **Baseline Reset**: Purges custom edits and restores the original PDF timetable.

### 📤 5. `.upasthiti` File Format & Sharing System
- **Custom `.upasthiti` Format**: Lightweight JSON format for sharing dynamic timetables and attendance records.
- **Export Hub**:
  - **Attendance Data**: Export attendance history as `MCA_1st_Sem_Attendance_YYYY-MM-DD.upasthiti`.
  - **Timetable Data**: Export **Original** or **Modified** timetables with semester-based filenames (`MCA_1st_Sem_Modified.upasthiti`).
  - **App Sharing**: System share sheet link to share the app with classmates.
- **Import Support**: Imports `.upasthiti` files alongside `.pdf` files.

### 🔄 6. Legacy App Migration & Auto-Updater
- **Legacy Package Detection**: Uses Android 11+ `<queries>` to check if the legacy package (`com.kanhaji.upastithi`) is installed.
- **Uninstall Prompt**: Prompts the user once per session to trigger Android's system package uninstaller for the old app.
- **In-App Auto-Updater**: Native Ktor streaming APK downloader with live progress and system package installer integration.

---

## 🔧 Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose + Material Design 3
- **Navigation**: Voyager Navigation Architecture (`Screen` ➔ `Component` ➔ `Pages` ➔ `Components`)
- **PDF Engine**: Apache PDFBox Android (`com.tom_roush.pdfbox.android`)
- **Local Persistence**: Room DB (SQLite) + Dual-JSON storage
- **Networking**: Ktor Client (`io.ktor`)
- **Serialization**: `kotlinx.serialization` for `.upasthiti` JSON encoding
- **Application ID**: `com.kanhaji.upasthiti`

---

## 🧠 Name Origin

_**Upasthiti** (उपस्थिति)_ is a Sanskrit word meaning **"Presence" or "Attendance"**.

---

## 📥 Installation

1. Download the latest release APK from the [Releases](https://github.com/kanha321/Upastithi/releases) page.
2. Open **Upasthiti** on your Android device.
3. Import your university timetable **PDF** or a **`.upasthiti`** file.
