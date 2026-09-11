package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ktp_records")
data class KtpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nik: String = "",
    val nama: String = "",
    val tempatLahir: String = "",
    val tanggalLahir: String = "",
    val jenisKelamin: String = "",
    val golonganDarah: String = "-",
    val alamat: String = "",
    val rtRw: String = "",
    val kelDesa: String = "",
    val kecamatan: String = "",
    val agama: String = "",
    val statusPerkawinan: String = "",
    val pekerjaan: String = "",
    val kewarganegaraan: String = "WNI",
    val berlakuHingga: String = "SEUMUR HIDUP",
    val provinsi: String = "",
    val kotaKabupaten: String = "",
    val scannedAt: Long = System.currentTimeMillis(),
    val rawOcrText: String = ""
)
