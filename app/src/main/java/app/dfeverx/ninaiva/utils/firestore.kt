package app.dfeverx.ninaiva.utils

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeoutException

fun DocumentReference.listenWithTimeout(timeout: Long): Flow<DocumentSnapshot> = callbackFlow {
    val listenerRegistration = addSnapshotListener { snapshot, exception ->
        if (exception != null) {
            close(exception)
            return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
            trySend(snapshot).isSuccess
        }
    }

    awaitClose { listenerRegistration.remove() }

    // Timeout logic
    withTimeoutOrNull(timeout) {
        awaitClose { listenerRegistration.remove() }
    } ?: throw TimeoutException("Listener timed out after $timeout ms")
}

fun Query.listenWithTimeout(timeout: Long): Flow<DocumentSnapshot> = callbackFlow {
    val listenerRegistration = addSnapshotListener { snapshot, exception ->
        if (exception != null) {
            close(exception)
            return@addSnapshotListener
        }
        if (snapshot != null) {
            for (doc in snapshot.documents) {
                trySend(doc).isSuccess
            }
        }
    }

    awaitClose { listenerRegistration.remove() }

    // Timeout logic
    withTimeoutOrNull(timeout) {
        awaitClose { listenerRegistration.remove() }
    } ?: throw TimeoutException("Listener timed out after $timeout ms")
}



