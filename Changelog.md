# Changelog

---

## [3.0.0] - 01:15 AM / 29 July 2026

### Major Version 3.0 Release & Architecture Overhaul

- **Package Name Corrective Refactoring (`com.kanhaji.upasthiti`)**:
  - Corrected application ID and namespace to `com.kanhaji.upasthiti`.
  - Refactored full codebase package structure across 70+ source files.

- **Legacy App Detection & Automated Uninstaller**:
  - Added Android package visibility `<queries>` and uninstallation prompt.
  - Automatically detects if the legacy app (`com.kanhaji.upastithi`) is installed 1 second post-launch and provides a 1-tap uninstall dialog.

- **Per-Subject Attendance Modes (`⚙ Slot` vs `⚙ Day`)**:
  - Added mode toggle chips (`⚙ Slot` / `⚙ Day`) on subject attendance cards.
  - **Per-Slot Mode**: Standard slot-by-slot attendance percentage calculation.
  - **Per-Day Mode (Lenient Option A)**: Groups slots by date, counting a date as Present if at least 1 slot on that date was marked Present or Proxy.
  - Saves preferences independently per subject in DataStore.

- **Universal Dynamic Timetable Parser**:
  - 100% on-device PDF grid timetable parser supporting B.Tech, MCA, MBA, M.Tech, and all academic branches.
  - Automatically extracts courses, section schedules, locations, and faculty metadata directly from uploaded PDFs.

- **Scroll-Aware Auto-Hiding Bottom Navigation**:
  - Attached `NestedScrollConnection` to main list views.
  - Smoothly hides the `FloatingSpringBottomBar` via slide-down animation on downward scroll and restores via slide-up animation on upward scroll or tab switch.

- **Contextual Help Icons & Centered Dialogs**:
  - Anchored top app bar help (`?`) icon for Attendance and Timetable screens without tab-switch animation jitter.
  - Material 3 explanation dialogs with centered headers explaining attendance calculation modes, shifting classes, and timetable editing.

- **About & Community Settings**:
  - Integrated **Check for Updates** in Settings that verifies the latest release and opens the GitHub Releases page directly.
  - Added 1-tap direct links for **Developer Profile** and **Upasthiti Repository**.

- **Material 3 Expressive UI & Real-Time Collision Guard**:
  - Upgraded Compose Material 3 library to stable `1.4.0` with Material 3 Expressive APIs.
  - Integrated 2-step **M3 Clock Dial (`TimePicker`)** for start/end time selection.
  - Built real-time interval intersection detection ($\max(S_1, S_2) < \min(E_1, E_2)$) when adding or editing classes with conflict warning banners.

---

## [2.5.0] - 03:10 AM / 13 January 2026

### Academic Updates

- Updated **MCA 4th Sem Timetable**.

---

## [2.1.0] - 02:15 PM / 28 August 2025

### New Features & Improvements

- **App Theming**:
  - Fixed color issues in **Light Theme** for better readability and consistency.
- **In-App Updater**:
  - Added support for in-app updates so users can update seamlessly without leaving the app.
- **Academic Updates**:
  - Added updated **1st Year Timetable**.

---

## [2.0.0] - 12:41 PM / 23 August 2025

### Complete UI Overhaul

- **Navigation Enhancement**: Split calendar and attendance sections into separate sections with bottom navigation bar.
- **Academic Updates**: Added MCA 1st Sem TimeTable.
- **Theming System**:
  - Added proper theming support with light/dark theme options.
  - Implemented pitch black theming.
  - Added wallpaper-based colors and custom color options.
- **UI/UX Improvements**:
  - Added icons to attendance status indicators.
  - Added fun random messages when clicking on weekends (Saturdays/Sundays).
  - Added outlined dots for each day that correctly hint attendance status for classes.
  - Outlined Sunday with red and Saturday with blue borders.
  - Timetable cards now show room numbers instead of subject codes.
  - Added teacher names in attendance section for each subject.
- **Calendar Features**:
  - Added option to navigate to today.
  - Added option to view timetable for future dates while still restricting attendance marking.
- **Bug Fixes**:
  - Fixed day starting with Monday on some devices.
  - Fixed dots showing in wrong order.

---

## [1.1.0] - 09:48 AM / 31 July 2025

### Release Highlights

- Corrected timetable for Wednesday (dirty fix).
- Cleaned up and organized the codebase.
- Bumped the version code.
- Integrated calendar changes from previous dev versions:
  - Custom calendar using a new library to match the Material You design language.
  - Calendar is now compatible with dark theme, making every component compatible with dark theme.
  - Disabled future attendance marking.
  - Attendance percentage is now rounded up to 2 decimal places.
  - Added dots on the calendar to show the attendances of each class.

---

## [2.0.0-dev1.1] - 12:34 AM / 30 July 2025

### Calendar Enhancements (dev build)

- Added dots on the calendar to show the attendances of each class.

---

## [2.0.0-dev1.0] - 09:03 PM / 29 July 2025

### Complete Calendar Overhaul (dev build)

- Created a custom calendar using a new library to match the Material You design language.
- Calendar is now compatible with dark theme, making every component compatible with dark theme.
- Disabled future attendance marking.
- Attendance percentage is now rounded up to 2 decimal places.

---

## [1.0.2] - 11:10 AM / 28 July 2025

### Enhancements

- Updated the attendance dialog to color-code the border and text of each class based on its saved attendance status.

---

## [1.0.1] - 11:08 AM / 27 July 2025

### Minor Changes

- Added a hint for adding attendance records between calendar and records sections.
- Changed the application package name (this won't change in future versions).

---

## [1.0.0] - 08:11 AM / 27 July 2025

### Initial Release (Don't use this version as it has a different package name)

**Features:**
- Subject-wise attendance tracking
- Attendance percentage calculation per subject
- Simple and intuitive user interface
