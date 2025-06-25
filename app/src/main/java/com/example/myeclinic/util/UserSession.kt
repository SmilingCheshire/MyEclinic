package com.example.myeclinic.util

import com.example.myeclinic.model.User
/**
 * Singleton object for managing the currently logged-in user's session.
 *
 * Holds a reference to the authenticated [User] object, which can be accessed
 * throughout the app for authorization, personalization, and session-based logic.
 */
object UserSession {
    var currentUser: User? = null
}