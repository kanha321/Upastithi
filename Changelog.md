# Changelog

---

## [3.1.0] - 04:04 AM / 09 August 2026

### Vector-Grid PDF Parser Rewrite

- **Vector-Line Grid Extraction (New Primary Parser)**:
  - Replaced text-proximity heuristics with direct extraction of vector-drawn line segments from the PDF content stream via `PDFGraphicsStreamEngine`.
  - Grid structure (rows, columns, merged cells) is now derived from actual drawn lines — the same lines visible in the PDF.
  - Thin filled rectangles (common table border style, width < 3pt) are automatically decomposed into edge lines.
  - Merged cell detection via missing interior dividers — accurately handles multi-hour labs of any duration.

- **Eliminated All Hardcoded Pixel Thresholds**:
  - Removed `targetSpan = 3` (assumed all labs are 3 hours) — lab duration now comes from actual cell column span.
  - Removed `centerX < 390f` legend column split — replaced with dynamic largest-gap detection.
  - Removed `DEFAULT_SLOT_CENTERS` (10 hardcoded X-coordinates) — column edges come from vector lines.
  - Removed fixed Y-gap (`4.5f`) and X-gap (`3.5f`) thresholds — now computed from median character dimensions.
  - Degenerate narrow columns (< 10pt, border artifacts) are automatically filtered.

- **Improved Legend Parsing**:
  - Faculty definition section (`RT: Dr. Name`) is now correctly separated from course entries, preventing the last course from absorbing all faculty initials.
  - Subject → teacher mapping is the primary faculty lookup; cell-text scanning is fallback only.
  - All known faculty initials are stripped from location text, not just the matched one.

- **Robust Table Boundary Detection**:
  - Table region boundary derived from the longest vertical border lines (outer table frame), preventing legend table rows from being included in the grid.

- **PDF Page Preview State Persistence**:
  - Fixed an issue where reopening the app or switching tabs reset the PDF preview card to page 0 instead of displaying the page corresponding to the active timetable (e.g. 5th Sem on page 2).
  - Synchronized `currentSelectedPageIndex` state with `activeTimetableData.pageIndex` and `PrefsManager` across all app lifecycle events.

- **Dedicated Target File Pickers (.pdf & .upasthiti)**:
  - Updated the initial launch upload dialog to present two separate buttons for PDF timetables and `.upasthiti` backup files.
  - Implemented Storage Access Framework `OpenDocument` contracts with targeted MIME-type filtering (`application/pdf` for PDFs and `application/json`, `application/octet-stream`, `text/plain` for `.upasthiti` backups) to hide irrelevant media files in the file picker.

- **Text-Heuristic Fallback Preserved**:
  - Original spatial-clustering parser retained as automatic fallback for PDFs without vector-drawn grid lines.

- **Unified Help Dialogs**:
  - Standardized top app bar `?` help dialogs across all tabs with attendance status color guides.

- **Weekend & Holiday Dialog**:
  - Added a dedicated holiday dialog for Saturday & Sunday with typewriter animated messages and wide block cursor.

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
