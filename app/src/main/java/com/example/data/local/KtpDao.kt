package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KtpDao {
    @Query("SELECT * FROM ktp_records ORDER BY scannedAt DESC")
    fun getAllKtps(): Flow<List<KtpEntity>>

    @Query("SELECT * FROM ktp_records WHERE id = :id")
    fun getKtpById(id: Long): Flow<KtpEntity?>

    @Query("""
        SELECT * FROM ktp_records 
        WHERE nik LIKE '%' || :query || '%' 
           OR nama LIKE '%' || :query || '%' 
           OR kotaKabupaten LIKE '%' || :query || '%'
           OR pekerjaan LIKE '%' || :query || '%'
        ORDER BY scannedAt DESC
    """)
    fun searchKtps(query: String): Flow<List<KtpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKtp(ktp: KtpEntity): Long

    @Update
    suspend fun updateKtp(ktp: KtpEntity)

    @Delete
    suspend fun deleteKtp(ktp: KtpEntity)

    @Query("DELETE FROM ktp_records WHERE id = :id")
    suspend fun deleteKtpById(id: Long)

    @Query("SELECT COUNT(*) FROM ktp_records")
    fun getCount(): Flow<Int>
}
