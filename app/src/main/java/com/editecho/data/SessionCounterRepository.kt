package com.editecho.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCounterRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("sessionCounters")

    /**
     * Starts a new session by creating a document with the given sessionId
     * and setting the startTime to the server timestamp.
     *
     * @param sessionId The unique identifier for the session
     * @return Task<Void> representing the success or failure of the operation
     */
    fun startSession(sessionId: String): Task<Void> {
        val sessionData = hashMapOf(
            "startTime" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "duration" to 0L
        )
        
        return collection.document(sessionId)
            .set(sessionData)
    }

    /**
     * Updates the duration of an existing session without overwriting the startTime.
     *
     * @param sessionId The unique identifier for the session
     * @param durationMillis The duration of the session in milliseconds
     * @return Task<Void> representing the success or failure of the operation
     */
    fun updateSessionDuration(sessionId: String, durationMillis: Long): Task<Void> {
        val updateData = hashMapOf(
            "duration" to durationMillis
        )
        
        return collection.document(sessionId)
            .set(updateData, SetOptions.merge())
    }
} 