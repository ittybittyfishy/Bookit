
Book Nook

By: Olivia Fishbough, Itzel Medina, Veronica Nguyen, Yunjong Noh

**Overview**

Book Nook is an Android application designed for book enthusiasts to enhance their reading experience. The app allows users to discover, organize, and review books while connecting with friends and fellow readers. Built using Android Studio, Book Nook integrates with the Google Books API for seamless book searching and personalization features.

**Core Functionality**

    Database Integration:
        Connects to the Google Books API for a vast library of books.

    Account Registration:
        Enables users to create accounts and store their personal information securely.

    Search and Filters:
        Search for books using user-provided criteria with customizable filters.

    Book Organization:
        Track reading progress by setting the current status of each book (e.g., Reading, Finished, Want to Read). Or create and store books in Custom Collections.

    Book Reviews:
        Write reviews using a structured template or free-form input.

    Personalized Recommendations:
        Suggestions tailored to the user's preferences and reading history, refined through user feedback.

    My Books Tab:
        A dedicated space for managing personal book collections.

    Accurate Ratings:
        Supports star ratings, including half and zero stars.

    Reading Insights and Achievements:
        Tracks reading habits and offers badges to celebrate milestones.

    Social Features:
        Create or join groups, view updates, add friends, and share book recommendations.

    Profile Customization:
        Includes banners, profile pictures, and personalized reading stats.

    Theme Toggle:
        Switch between light and dark modes for a customized visual experience.

**Prerequisites**

    - Android Studio (version 4.2 or later)
    - Java Development Kit (JDK) 8 or higher
    - Ensure you have a stable internet connection for Firebase services to function properly.

**Installation**

    Step 1: Clone the Repository

    git clone https://github.com/ittybittyfishy/Bookit 
    cd BookNook

    Step 2: Configure Firebase

        Go to the Firebase Console.
        Create a project and register your Android app with your package name.
        Download the google-services.json file and place it in the /app directory.

    Step 3: Build and Run the App

        Open the project in Android Studio.
        Sync the Gradle files.
        Run the app on an emulator or physical device.

**Usage Instructions**

    Sign In/Sign Up:
        Create an account to access all features.

    Search for Books:
        Use the search bar to find books and apply filters for refined results.

    Organize Your Library:
        Add books to your collection and track your reading status.

    Write Reviews:
        Share your thoughts on books using structured templates or freeform options.

    Discover Recommendations:
        Receive tailored book suggestions based on your preferences and reading history.

    Join Groups and Add Friends:
        Connect with others, participate in discussions, and view updates.

    Track Your Progress:
        View reading insights and earn badges for achievements.

**Troubleshooting**

    Firebase Connection Issues:
        Ensure the google-services.json file is placed correctly in the /app directory.
        Verify Firebase rules allow proper access.

    Dependencies Not Resolving:
        Re-sync Gradle in Android Studio.

    App Crashes:
        Check logcat in Android Studio for error details.
        A common crash is leaving the home page before the reccomendations finish loading upon login!