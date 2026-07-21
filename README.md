# Suresh Maths

**Suresh Maths** is a premium educational mathematics platform designed to provide comprehensive notes, worksheets, model papers, quizzes, and administrative management. It features distinct interfaces for both students and administrators to streamline the learning process.

## 🚀 Key Features
- **Student Portal:** Direct access to class materials, categorized chapters, and mathematical resources.
- **Admin Dashboard:** Secure, role-based access control enabling administrators to manage content, oversee users, and control application configurations.
- **Interactive Assessments:** Built-in quizzes to track student progress and validate mathematical learning.
- **Integrated Document Viewer:** A dedicated viewer for study materials such as worksheets, model papers, and detailed notes.
- **Authentication System:** Secure login and registration flows, coupled with customized onboarding and rich user profile management.
- **Deep Linking:** Dynamic processing of intent links to launch directly into specific parts of the app.
- **Dynamic Theming:** Built-in support for Light and Dark modes, respecting user and system preferences seamlessly.

---

## 🛠 Tech Stack
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Modern declarative Android UI)
- **Design System:** Material Design 3 (M3) components and typography
- **Backend & Cloud Services:** Firebase Integration (Authentication, Database, etc.)
- **Asynchronous Programming:** Kotlin Coroutines & Flows
- **State Management:** `StateFlow` and `collectAsStateWithLifecycle` for robust, lifecycle-aware UI state rendering

---

## 📐 Architecture
The application strictly adheres to the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a robust, maintainable, and scalable codebase:

- **View (UI Layer):** 
  Constructed entirely with Jetpack Compose. Separated into distinct screens (e.g., `splash`, `login`, `home`, `admin_dashboard`) and reusable composables. The UI layer strictly acts as an observer, reacting to State changes emitted by the ViewModel.
  
- **ViewModel (Business Logic Layer):** 
  Centralized business logic and state management (handled via `MathsViewModel`). It manages data manipulation, handles authentication state, and converts raw data from backend resources into clean `StateFlow` streams that the views can easily collect.

- **Data Layer:** 
  Manages connections to remote services (such as Firebase). Ensures secure and efficient data retrieval, offline state parsing, and data posting for profiles and educational material.

## 📦 Navigation & Routing
Instead of standard fragment transactions, the app utilizes a smooth, state-based declarative routing technique (e.g., tracking `currentScreen` natively). This allows dynamic, logic-driven navigation based on authentication tokens and user roles (Admin vs. Student).

## 📱 Device Support
- Implements `enableEdgeToEdge()` for an immersive visual experience.
- Uses dynamic layout adjustments utilizing constraints and Configuration orientations for cross-device support (Phones & Tablets).
