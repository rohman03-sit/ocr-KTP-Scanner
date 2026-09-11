package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.KtpEntity
import com.example.ui.components.KtpCardView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleKtp = KtpEntity(
      nik = "3174051208900003",
      nama = "BUDI SANTOSO",
      tempatLahir = "JAKARTA",
      tanggalLahir = "12-08-1990",
      jenisKelamin = "LAKI-LAKI",
      golonganDarah = "O",
      alamat = "JL. SUDIRMAN NO. 45",
      rtRw = "005/002",
      kelDesa = "SENAYAN",
      kecamatan = "KEBAYORAN BARU",
      agama = "ISLAM",
      statusPerkawinan = "KAWIN",
      pekerjaan = "KARYAWAN SWASTA",
      provinsi = "PROVINSI DKI JAKARTA",
      kotaKabupaten = "KOTA JAKARTA SELATAN"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        KtpCardView(ktp = sampleKtp)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

