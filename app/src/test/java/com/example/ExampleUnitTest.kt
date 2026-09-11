package com.example

import com.example.data.local.KtpEntity
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun ktpEntity_defaultValues_areValid() {
    val ktp = KtpEntity(
      nik = "3174051208900003",
      nama = "BUDI SANTOSO",
      jenisKelamin = "LAKI-LAKI"
    )
    assertEquals(16, ktp.nik.length)
    assertEquals("WNI", ktp.kewarganegaraan)
    assertEquals("SEUMUR HIDUP", ktp.berlakuHingga)
    assertEquals("-", ktp.golonganDarah)
  }
}

