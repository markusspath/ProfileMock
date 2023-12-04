# ProfileMock
A mocked Profile Repository


you should be able to instantiate it like

```kotlin
    private val profileRepository: ProfileRepository = MockProfileRepository()
    profileRepository.addLivestream("qwerty")
    ...
```

or accessing an open Firestore

```kotlin
    private val firestoreConfig = FirestoreConfig.getDevConfig()
    private val fireStoreDataSource: OpenFirestoreDataSource = OpenFirestoreDataSource(context, firestoreConfig)
        .also { it.initialize() }
    private val profileRepository: ProfileRepository = OpenFirestoreProfileRepository(fireStoreDataSource)
    profileRepository.addLivestream("qwertz")
    ...
```