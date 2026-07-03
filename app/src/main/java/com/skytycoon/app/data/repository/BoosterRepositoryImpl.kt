package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.BoosterDao
import com.skytycoon.app.data.local.entity.ActiveBoosterEntity
import com.skytycoon.app.data.local.entity.BoosterInventoryEntity
import com.skytycoon.app.domain.model.ActiveBooster
import com.skytycoon.app.domain.model.BoosterInventoryItem
import com.skytycoon.app.domain.model.BoosterType
import com.skytycoon.app.domain.repository.BoosterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoosterRepositoryImpl @Inject constructor(private val dao: BoosterDao) : BoosterRepository {

    override fun getInventory(): Flow<List<BoosterInventoryItem>> =
        dao.getInventory().map { list -> list.map { it.toDomain() } }

    override fun getActiveBoosters(currentGameMinutes: Long): Flow<List<ActiveBooster>> =
        dao.getActiveBoosters(currentGameMinutes).map { list -> list.map { it.toDomain() } }

    override suspend fun activateBooster(inventoryId: Long, currentGameMinutes: Long): Boolean {
        val item = dao.getInventoryItemById(inventoryId) ?: return false
        if (item.quantity <= 0) return false
        dao.updateInventoryItem(item.copy(quantity = item.quantity - 1))
        dao.insertActiveBooster(
            ActiveBoosterEntity(
                boosterType = item.boosterType,
                name = item.name,
                multiplier = item.multiplier,
                expiresAtGameMinutes = currentGameMinutes + item.durationGameMinutes
            )
        )
        return true
    }

    override suspend fun deleteExpiredBoosters(currentGameMinutes: Long) =
        dao.deleteExpiredBoosters(currentGameMinutes)

    override suspend fun seedDefaultBoosters() {
        if (dao.inventoryCount() > 0) return
        listOf(
            BoosterInventoryEntity(boosterType = BoosterType.TIME_BOOST.name, name = "Acelerador de Tempo",
                description = "Dobra a velocidade de simulação por 1h de jogo", durationGameMinutes = 60, multiplier = 2.0, quantity = 2),
            BoosterInventoryEntity(boosterType = BoosterType.MAINTENANCE_BOOST.name, name = "Kit de Manutenção Rápida",
                description = "Reduz custos de manutenção em 50% por 2h de jogo", durationGameMinutes = 120, multiplier = 0.5, quantity = 1),
            BoosterInventoryEntity(boosterType = BoosterType.MARKETING_BOOST.name, name = "Campanha de Marketing",
                description = "Aumenta ocupação em 20% por 3h de jogo", durationGameMinutes = 180, multiplier = 1.2, quantity = 1)
        ).forEach { dao.insertInventoryItem(it) }
    }
}
