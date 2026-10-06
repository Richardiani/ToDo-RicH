# ToDo-RicH

ToDo-RicH is a production-ready Android to-do application built with Kotlin, Jetpack Compose, Material 3, Room, and Navigation Compose. It is designed for everyday productivity and works offline for core task management.

## Features

- Create, edit, delete, complete, and reactivate tasks
- Set title, description, priority, due date, and due time
- Organize tasks into categories
- Search, filter, and sort tasks in real time
- View active, completed, overdue, and due-today tasks
- Persistent local storage with Room database
- Material 3 dark/light theme support
- Accessibility-conscious UI and validation feedback

## Technology Stack

- Kotlin
- Android Jetpack Compose
- Material 3
- Room persistence library
- ViewModel + StateFlow
- Navigation Compose
- Kotlin Coroutines

## Building the project

From the project root:

```bash
./gradlew assembleRelease
```

## APK output

The release APK is generated at:

```text
app/build/outputs/apk/release/
```

Open the generated APK file in Android Studio or install it directly on a compatible device.

## Notes

The app is implemented as a clean, maintainable Android project with a Room-backed repository layer and a Compose-driven UI.
