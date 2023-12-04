package de.ard.audiothek.mock

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class FirestoreConfig(val applicationId: String, val apiKey: String, val projectId: String) {
    companion object {
        fun getDevConfig() = FirestoreConfig(
        "1:296208390323:android:864e62e61a0a0bf78478c6",
        "AIzaSyC51krAHfKpsbsCz9exzIADUCNx0WnAiU8",
        "at-dev-1c27f"
        )
    }
}

class OpenFirestoreDataSource(val context: Context, val firestoreConfig: FirestoreConfig) {
    companion object {
        const val TAG = "FirestoreDataSource"
        const val FIRESTORE_APP_NAME = "Dev Firestore"

        // root level collections
        const val AUDIOTHEK_ROOT = "/audiothek"

        // collections
        const val COLLECTION_LIVESTREAMS_PATH = "lists/livestreams/items"

        // fields
        const val FIELD_LIVESTREAM_ID = "id"
        const val FIELD_LIVESTREAM_ORDER = "order"
    }

    private lateinit var firebaseApp: FirebaseApp

    // always associated to the current Profile
    private val uid = "GgsrShQPT7hkyWX8aiZv25mlM593"

    private var docsSnapshot: MutableList<DocumentSnapshot> = mutableListOf()
    private var orders: List<Double> = listOf()

    fun initialize() {
        if (::firebaseApp.isInitialized) return
        try {
            firebaseApp = FirebaseApp.initializeApp(context, getOptions(), FIRESTORE_APP_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing firestore: ", e)
        }
    }

    private fun getOptions() = FirebaseOptions.Builder().apply {
        setApplicationId(firestoreConfig.applicationId)
        setApiKey(firestoreConfig.apiKey)
        setProjectId(firestoreConfig.projectId)
        setDatabaseUrl("https://$firestoreConfig.projectId.firebaseio.com")
        setStorageBucket("$firestoreConfig.projectId.appspot.com")
    }.build()

    private fun getCollectionReference(collectionPath: String): CollectionReference {
        return Firebase.firestore(firebaseApp).collection(collectionPath)
    }

    // TODO error handling
    fun getLivestreams(): Flow<List<Livestream>> {
        val path = "$AUDIOTHEK_ROOT/$uid/$COLLECTION_LIVESTREAMS_PATH"
        val collectionReference = getCollectionReference(path)
        return collectionReference.snapshots().map { querySnapshot ->
            docsSnapshot = querySnapshot.documents
            orders = docsSnapshot.map {
                it.get(FIELD_LIVESTREAM_ORDER).toString().toDouble()
            }.toList()
            querySnapshot.toObjects(Livestream::class.java).sortedBy { it.order }
        }
    }

    // TODO error handling
    suspend fun getLivestreamsSnapshot(): List<Livestream> {
        val path = "$AUDIOTHEK_ROOT/$uid/$COLLECTION_LIVESTREAMS_PATH"
        val collectionReference = getCollectionReference(path)
        val livestreams = collectionReference.get().await()
        return livestreams.toObjects(Livestream::class.java).sortedBy { it.order }
    }

    fun addLivestream(streamId: String) {
        if (docsSnapshot.any { it["id"] == streamId }) {
            println("Illegal argument exception, will not add already existing $streamId")
            return
        }
        val path = "$AUDIOTHEK_ROOT/$uid/$COLLECTION_LIVESTREAMS_PATH"
        val collectionReference = getCollectionReference(path)
        val documentReference = collectionReference.document()
        val order = orders.getNextLast()
        documentReference.set(Livestream(streamId, order))
    }

    fun updateLivestreamPosition(streamId: String, position: Int) {
        val docId = docsSnapshot.firstOrNull { it[FIELD_LIVESTREAM_ID] == streamId }
        docId?.let {
            val path = "$AUDIOTHEK_ROOT/$uid/$COLLECTION_LIVESTREAMS_PATH"
            val documentReference = getCollectionReference(path).document(it.id)
            val order = orders.getOrderAt(position)
            documentReference.update(FIELD_LIVESTREAM_ORDER, order)
        }
    }

    fun removeLivestream(streamId: String) {
        val docId = docsSnapshot.firstOrNull { it["id"] == streamId }
        docId?.let {
            val path = "$AUDIOTHEK_ROOT/$uid/$COLLECTION_LIVESTREAMS_PATH"
            val documentReference = getCollectionReference(path).document(it.id)
            documentReference.delete()
        }
    }
}