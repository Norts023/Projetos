package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.UnlockDao
import com.skytycoon.app.data.local.entity.UnlockEntity
import com.skytycoon.app.domain.model.Unlock
import com.skytycoon.app.domain.repository.UnlockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnlockRepositoryImpl @Inject constructor(
    private val dao: UnlockDao
) : UnlockRepository {

    override fun getAll(): Flow<List<Unlock>> =
        dao.getAll().map { it.map { e -> e.toDomain() } }

    override suspend fun isUnlocked(contentId: String): Boolean =
        dao.isUnlocked(contentId) > 0

    override suspend fun unlock(contentId: String, currentGameMinutes: Long) =
        dao.insert(UnlockEntity(contentId = contentId, unlockedAt = currentGameMinutes))
}
