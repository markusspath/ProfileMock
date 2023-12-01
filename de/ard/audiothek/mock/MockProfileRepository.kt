package de.ard.audiothek.mock

class MockProfileRepository: ProfileRepository {

    private val _livestreams: MutableStateFlow<List<Livestream>> = MutableStateFlow(emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    private val livestreams = _livestreams.asStateFlow()
        .mapLatest { livestreams -> livestreams.sortedBy { it.order } }

    override fun getLivestreams(): Flow<List<Livestream>> {
        return livestreams
    }

    override suspend fun getLivestreamsSnapshot(): List<Livestream> {
        return _livestreams.value.sortedBy { it.order }
    }

    override fun addLivestream(streamId: String) {
        if (_livestreams.value.any { it.id == streamId }) return
        _livestreams.value = _livestreams.value.plus(
            Livestream(streamId, _livestreams.value.map { it.order }.getNextLast())
        )
    }

    override fun updateLivestreamPosition(streamId: String, position: Int) {
        val stream = _livestreams.value.firstOrNull { it.id == streamId }
        stream?.let {it ->
            _livestreams.value = _livestreams.value.minus(it)
            _livestreams.value = _livestreams.value.plus(
                // we need to check for reordering in the store
                Livestream(streamId, _livestreams.value.map { it.order }.getOrderAt(position)!!)
            )
        }
    }

    override fun removeLivestream(streamId: String) {
        val stream = _livestreams.value.firstOrNull { it.id == streamId }
        stream?.let {
            _livestreams.value = _livestreams.value.minus(it)
        }
    }
}