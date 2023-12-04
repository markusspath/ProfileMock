package de.ard.audiothek.mock

import kotlinx.coroutines.flow.Flow

class OpenFirestoreProfileRepository(val firestoreDataSource: OpenFirestoreDataSource): ProfileRepository {

    override fun getLivestreams(): Flow<List<Livestream>> {
        return firestoreDataSource.getLivestreams()
    }

    override suspend fun getLivestreamsSnapshot(): List<Livestream> {
        return firestoreDataSource.getLivestreamsSnapshot()
    }

    override fun addLivestream(streamId: String) {
        firestoreDataSource.addLivestream(streamId)
    }

    override fun updateLivestreamPosition(streamId: String, position: Int) {
        firestoreDataSource.updateLivestreamPosition(streamId, position)
    }

    override fun removeLivestream(streamId: String) {
        firestoreDataSource.removeLivestream(streamId)
    }

    private suspend fun resetLivestreams() {
        val current = firestoreDataSource.getLivestreamsSnapshot()
        current.forEachIndexed { index, livestream ->
            updateLivestreamPosition(livestream.id, index)
        }
    }
}