package de.ard.audiothek.mock

interface ProfileRepository {

    fun getLivestreams(): Flow<List<Livestream>>
    suspend fun getLivestreamsSnapshot(): List<Livestream>
    fun addLivestream(streamId: String)
    fun updateLivestreamPosition(streamId: String, position: Int)
    fun removeLivestream(streamId: String)

}