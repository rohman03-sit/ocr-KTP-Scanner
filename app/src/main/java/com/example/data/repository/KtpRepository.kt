package com.example.data.repository

import com.example.data.local.KtpDao
import com.example.data.local.KtpEntity
import kotlinx.coroutines.flow.Flow

class KtpRepository(private val ktpDao: KtpDao) {
    val allKtps: Flow<List<KtpEntity>> = ktpDao.getAllKtps()
    val count: Flow<Int> = ktpDao.getCount()

    fun search(query: String): Flow<List<KtpEntity>> = ktpDao.searchKtps(query)

    fun getById(id: Long): Flow<KtpEntity?> = ktpDao.getKtpById(id)

    suspend fun insert(ktp: KtpEntity): Long = ktpDao.insertKtp(ktp)

    suspend fun update(ktp: KtpEntity) = ktpDao.updateKtp(ktp)

    suspend fun delete(ktp: KtpEntity) = ktpDao.deleteKtp(ktp)

    suspend fun deleteById(id: Long) = ktpDao.deleteKtpById(id)
}
