# Implementation Plan - Modern TV Layout

This plan outlines the steps to transform the current simple TV app layout into a modern, professional streaming UI as shown in the provided reference image.

## Proposed Changes

### 1. Theme and Styling
Update the application's color palette to match the dark navy and charcoal tones of the reference image.

#### [MODIFY] [Color.kt](file:///C:/Users/vikra/AndroidStudioProjects/MyTVPlayer/app/src/main/java/com/example/mytvplayer/ui/theme/Color.kt)
- Add custom colors for the background, sidebar, and accents.

#### [MODIFY] [Theme.kt](file:///C:/Users/vikra/AndroidStudioProjects/MyTVPlayer/app/src/main/java/com/example/mytvplayer/ui/theme/Theme.kt)
- Update `darkColorScheme` to use the new colors.

### 2. UI Components
Create new components for the sidebar and hero section.

#### [NEW] [SideBar.kt](file:///C:/Users/vikra/AndroidStudioProjects/MyTVPlayer/app/src/main/java/com/example/mytvplayer/ui/components/SideBar.kt)
- Implement a vertical navigation bar containing:
    - App Logo (Cineflix style).
    - Navigation items (Home, Movies, TV Shows, etc.).
    - Bottom items (Settings, User Profile).

#### [NEW] [HeroSection.kt](file:///C:/Users/vikra/AndroidStudioProjects/MyTVPlayer/app/src/main/java/com/example/mytvplayer/ui/components/HeroSection.kt)
- Implement the featured content banner with:
    - Large background image.
    - Metadata (Title, Year, Description).
    - "Play Now" and "My List" buttons.

### 3. Home Screen Refactor
Modify the main landing screen to use a layout that integrates the sidebar and main content.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/vikra/AndroidStudioProjects/MyTVPlayer/app/src/main/java/com/example/mytvplayer/MainActivity.kt)
- Update `MainScreen` to use a `Row` layout:
    - Left: `SideBar`.
    - Right: A `LazyColumn` containing the `HeroSection` followed by the horizontal `MovieRows`.
- Refine `MovieCard` styling to match the reference (aspect ratio, typography, and ratings badge).

## Verification Plan

### Manual Verification
- Deploy to an Android TV emulator or device.
- Verify focus behavior (navigation between sidebar and content).
- Check the layout responsiveness and visual fidelity against the reference image.
