package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.local.KtpEntity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object KtpOcrParser {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun parseFromBitmap(bitmap: Bitmap): Pair<KtpEntity, String> =
        withContext(Dispatchers.Default) {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            processInputImage(inputImage)
        }

    suspend fun parseFromUri(context: Context, uri: Uri): Pair<KtpEntity, String> =
        withContext(Dispatchers.IO) {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
                ?: throw IllegalArgumentException("Gagal membuka gambar KTP dari galeri.")
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            processInputImage(inputImage)
        }

    suspend fun parseFromResource(context: Context, resId: Int): Pair<KtpEntity, String> =
        withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                ?: throw IllegalArgumentException("Gagal memuat aset gambar KTP.")
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            processInputImage(inputImage)
        }

    private suspend fun processInputImage(inputImage: InputImage): Pair<KtpEntity, String> =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    val ktp = parseVisionText(visionText)
                    continuation.resume(Pair(ktp, fullText))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

    /**
     * Parses the vision text and text blocks into structured KTP fields.
     */
    fun parseVisionText(visionText: Text): KtpEntity {
        val rawText = visionText.text
        val lines = visionText.textBlocks
            .flatMap { it.lines }
            .map { it.text.trim() }
            .filter { it.isNotEmpty() }

        var provinsi = ""
        var kotaKabupaten = ""
        var nik = ""
        var nama = ""
        var tempatLahir = ""
        var tanggalLahir = ""
        var jenisKelamin = ""
        var golonganDarah = "-"
        var alamat = ""
        var rtRw = ""
        var kelDesa = ""
        var kecamatan = ""
        var agama = ""
        var statusPerkawinan = ""
        var pekerjaan = ""
        var kewarganegaraan = "WNI"
        var berlakuHingga = "SEUMUR HIDUP"

        // 1. Extract Headers (PROVINSI & KOTA / KABUPATEN)
        for (i in 0 until minOf(5, lines.size)) {
            val line = lines[i].uppercase(Locale.ROOT)
            if (line.contains("PROVINSI") || line.contains("PROV")) {
                provinsi = line.replace(":", "").trim()
            } else if (line.contains("KOTA") || line.contains("KABUPATEN") || line.contains("KAB ")) {
                kotaKabupaten = line.replace(":", "").trim()
            }
        }

        // 2. Extract NIK
        // Look for 16-digit patterns or lines containing NIK
        val nikRegex = Pattern.compile("(\\b[0-9]{16}\\b)")
        val nikMatcher = nikRegex.matcher(rawText)
        if (nikMatcher.find()) {
            nik = nikMatcher.group(1) ?: ""
        } else {
            // Find line with "NIK"
            for (line in lines) {
                val upper = line.uppercase(Locale.ROOT)
                if (upper.contains("NIK")) {
                    val candidate = cleanNikCandidate(line.replace(Regex("(?i)NIK"), ""))
                    if (candidate.length in 15..17) {
                        nik = candidate.take(16)
                        break
                    }
                }
            }
        }

        // 3. Line by Line Field Extraction
        for (idx in lines.indices) {
            val rawLine = lines[idx]
            val upper = rawLine.uppercase(Locale.ROOT)

            // Nama
            if (upper.contains("NAMA") && !upper.contains("AGAMA")) {
                var extracted = extractValueAfterColonOrKeyword(rawLine, listOf("NAMA", "NAME"))
                if (extracted.isBlank() && idx + 1 < lines.size) {
                    val nextLine = lines[idx + 1]
                    if (!isFieldHeader(nextLine)) {
                        extracted = nextLine
                    }
                }
                if (extracted.isNotBlank() && nama.isBlank()) {
                    nama = cleanFieldValue(extracted)
                }
            }

            // Tempat / Tgl Lahir
            if (upper.contains("TEMPAT") || upper.contains("LAHIR") || upper.contains("TGL LAHIR")) {
                val extracted = extractValueAfterColonOrKeyword(
                    rawLine,
                    listOf("TEMPAT/TGL LAHIR", "TEMPAT/TANGGAL LAHIR", "TEMPAT", "LAHIR")
                )
                if (extracted.isNotBlank()) {
                    val parts = extracted.split(",", ":")
                    if (parts.size >= 2) {
                        tempatLahir = cleanFieldValue(parts[0])
                        tanggalLahir = cleanDate(parts.subList(1, parts.size).joinToString(" "))
                    } else {
                        // Check if contains date pattern DD-MM-YYYY
                        val dateMatcher = Pattern.compile("(\\d{2}[-/ ]\\d{2}[-/ ]\\d{4})").matcher(extracted)
                        if (dateMatcher.find()) {
                            tanggalLahir = dateMatcher.group(1)?.replace(" ", "-")?.replace("/", "-") ?: ""
                            tempatLahir = cleanFieldValue(extracted.replace(tanggalLahir, "").replace(",", ""))
                        } else {
                            tempatLahir = cleanFieldValue(extracted)
                        }
                    }
                }
            }

            // Jenis Kelamin & Gol Darah
            if (upper.contains("JENIS KELAMIN") || upper.contains("KELAMIN")) {
                if (upper.contains("LAKI") || upper.contains("LAK1")) {
                    jenisKelamin = "LAKI-LAKI"
                } else if (upper.contains("PEREMPUAN") || upper.contains("WANITA")) {
                    jenisKelamin = "PEREMPUAN"
                }

                // Check Gol. Darah on same line
                if (upper.contains("DARAH") || upper.contains("GOL")) {
                    val bloodMatcher = Pattern.compile("(?i)(?:GOL(?:\\.|ONGAN)?\\s*DARAH\\s*[:]?\\s*)([ABO]+|-)", Pattern.CASE_INSENSITIVE).matcher(rawLine)
                    if (bloodMatcher.find()) {
                        golonganDarah = bloodMatcher.group(1)?.trim()?.uppercase(Locale.ROOT) ?: "-"
                    }
                }
            } else if (jenisKelamin.isBlank()) {
                if (upper.contains("LAKI-LAKI") || upper.contains("LAKI - LAKI")) {
                    jenisKelamin = "LAKI-LAKI"
                } else if (upper.contains("PEREMPUAN")) {
                    jenisKelamin = "PEREMPUAN"
                }
            }

            // Golongan Darah independent check
            if (golonganDarah == "-" && (upper.contains("GOL. DARAH") || upper.contains("GOL DARAH"))) {
                val bloodRegex = Regex("(?i)GOL(?:\\.|ONGAN)?\\s*DARAH\\s*[:]?\\s*([A-Z0-9+-]+)")
                val match = bloodRegex.find(rawLine)
                if (match != null) {
                    val candidate = match.groupValues[1].trim().uppercase(Locale.ROOT)
                    golonganDarah = when {
                        candidate.contains("AB") -> "AB"
                        candidate.contains("A") -> "A"
                        candidate.contains("B") -> "B"
                        candidate.contains("O") || candidate.contains("0") -> "O"
                        else -> "-"
                    }
                }
            }

            // Alamat
            if (upper.contains("ALAMAT") && !upper.contains("KEL/DESA") && !upper.contains("RT/RW")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("ALAMAT"))
                if (extracted.isNotBlank() && alamat.isBlank()) {
                    alamat = cleanFieldValue(extracted)
                }
            }

            // RT/RW
            if (upper.contains("RT/RW") || upper.contains("RT / RW") || upper.contains("RT/ RW")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("RT/RW", "RT / RW", "RT/ RW", "RT"))
                val rtMatcher = Pattern.compile("(\\d{2,3}\\s*/\\s*\\d{2,3})").matcher(extracted)
                if (rtMatcher.find()) {
                    rtRw = rtMatcher.group(1)?.replace(" ", "") ?: ""
                } else if (extracted.isNotBlank()) {
                    rtRw = cleanFieldValue(extracted)
                }
            }

            // Kel/Desa
            if (upper.contains("KEL/DESA") || upper.contains("KELURAHAN") || upper.contains("DESA")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("KEL/DESA", "KELURAHAN/DESA", "KELURAHAN", "DESA"))
                if (extracted.isNotBlank() && kelDesa.isBlank()) {
                    kelDesa = cleanFieldValue(extracted)
                }
            }

            // Kecamatan
            if (upper.contains("KECAMATAN") || upper.contains("KEC.")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("KECAMATAN", "KEC."))
                if (extracted.isNotBlank() && kecamatan.isBlank()) {
                    kecamatan = cleanFieldValue(extracted)
                }
            }

            // Agama
            if (upper.contains("AGAMA")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("AGAMA")).uppercase(Locale.ROOT)
                agama = when {
                    extracted.contains("ISLAM") || upper.contains("ISLAM") -> "ISLAM"
                    extracted.contains("KRISTEN") || upper.contains("KRISTEN") -> "KRISTEN"
                    extracted.contains("KATOLIK") || upper.contains("KATOLIK") -> "KATOLIK"
                    extracted.contains("HINDU") || upper.contains("HINDU") -> "HINDU"
                    extracted.contains("BUDDHA") || upper.contains("BUDHA") || upper.contains("BUDDHA") -> "BUDDHA"
                    extracted.contains("KONGHUCU") || upper.contains("KHONGHUCU") -> "KHONGHUCU"
                    else -> cleanFieldValue(extracted)
                }
            }

            // Status Perkawinan
            if (upper.contains("STATUS") || upper.contains("PERKAWINAN")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("STATUS PERKAWINAN", "STATUS", "PERKAWINAN")).uppercase(Locale.ROOT)
                statusPerkawinan = when {
                    extracted.contains("BELUM KAWIN") || upper.contains("BELUM KAWIN") -> "BELUM KAWIN"
                    extracted.contains("CERAI HIDUP") || upper.contains("CERAI HIDUP") -> "CERAI HIDUP"
                    extracted.contains("CERAI MATI") || upper.contains("CERAI MATI") -> "CERAI MATI"
                    extracted.contains("KAWIN") || upper.contains("KAWIN") -> "KAWIN"
                    else -> cleanFieldValue(extracted)
                }
            }

            // Pekerjaan
            if (upper.contains("PEKERJAAN")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("PEKERJAAN"))
                if (extracted.isNotBlank() && pekerjaan.isBlank()) {
                    pekerjaan = cleanFieldValue(extracted)
                }
            }

            // Kewarganegaraan
            if (upper.contains("KEWARGANEGARAAN")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("KEWARGANEGARAAN")).uppercase(Locale.ROOT)
                kewarganegaraan = when {
                    extracted.contains("WNI") || upper.contains("WNI") -> "WNI"
                    extracted.contains("WNA") || upper.contains("WNA") -> "WNA"
                    else -> "WNI"
                }
            }

            // Berlaku Hingga
            if (upper.contains("BERLAKU") || upper.contains("HINGGA")) {
                val extracted = extractValueAfterColonOrKeyword(rawLine, listOf("BERLAKU HINGGA", "BERLAKU", "HINGGA"))
                if (extracted.uppercase(Locale.ROOT).contains("SEUMUR") || upper.contains("SEUMUR HIDUP")) {
                    berlakuHingga = "SEUMUR HIDUP"
                } else if (extracted.isNotBlank()) {
                    berlakuHingga = cleanFieldValue(extracted)
                }
            }
        }

        // Secondary fallback pass across entire rawText if any critical field is missing
        val rawUpper = rawText.uppercase(Locale.ROOT)

        if (provinsi.isBlank()) {
            val provMatch = Regex("(?i)PROVINSI\\s+([A-Z\\s]+)").find(rawText)
            if (provMatch != null) {
                provinsi = "PROVINSI " + provMatch.groupValues[1].split("\n")[0].trim()
            }
        }

        if (kotaKabupaten.isBlank()) {
            val kotaMatch = Regex("(?i)(KOTA|KABUPATEN)\\s+([A-Z\\s]+)").find(rawText)
            if (kotaMatch != null) {
                kotaKabupaten = kotaMatch.groupValues[1].uppercase(Locale.ROOT) + " " + kotaMatch.groupValues[2].split("\n")[0].trim()
            }
        }

        if (nik.isBlank()) {
            // Check for loose 16-character numbers with common OCR substitutions
            val candidate = findLoose16Digit(rawText)
            if (candidate.isNotBlank()) {
                nik = candidate
            }
        }

        if (agama.isBlank()) {
            when {
                rawUpper.contains("ISLAM") -> agama = "ISLAM"
                rawUpper.contains("KRISTEN") -> agama = "KRISTEN"
                rawUpper.contains("KATOLIK") -> agama = "KATOLIK"
                rawUpper.contains("HINDU") -> agama = "HINDU"
                rawUpper.contains("BUDDHA") -> agama = "BUDDHA"
            }
        }

        if (statusPerkawinan.isBlank()) {
            when {
                rawUpper.contains("BELUM KAWIN") -> statusPerkawinan = "BELUM KAWIN"
                rawUpper.contains("CERAI HIDUP") -> statusPerkawinan = "CERAI HIDUP"
                rawUpper.contains("CERAI MATI") -> statusPerkawinan = "CERAI MATI"
                rawUpper.contains("KAWIN") -> statusPerkawinan = "KAWIN"
            }
        }

        if (jenisKelamin.isBlank()) {
            when {
                rawUpper.contains("LAKI-LAKI") || rawUpper.contains("LAKI - LAKI") -> jenisKelamin = "LAKI-LAKI"
                rawUpper.contains("PEREMPUAN") -> jenisKelamin = "PEREMPUAN"
            }
        }

        return KtpEntity(
            nik = nik,
            nama = nama,
            tempatLahir = tempatLahir,
            tanggalLahir = tanggalLahir,
            jenisKelamin = jenisKelamin,
            golonganDarah = golonganDarah,
            alamat = alamat,
            rtRw = rtRw,
            kelDesa = kelDesa,
            kecamatan = kecamatan,
            agama = agama,
            statusPerkawinan = statusPerkawinan,
            pekerjaan = pekerjaan,
            kewarganegaraan = kewarganegaraan,
            berlakuHingga = berlakuHingga,
            provinsi = provinsi,
            kotaKabupaten = kotaKabupaten,
            rawOcrText = rawText
        )
    }

    private fun extractValueAfterColonOrKeyword(line: String, keywords: List<String>): String {
        // If there's a colon, take what's after the colon
        if (line.contains(":")) {
            val afterColon = line.substringAfter(":").trim()
            if (afterColon.isNotBlank()) return afterColon
        }

        // Otherwise strip the keyword
        var result = line
        for (kw in keywords) {
            val regex = Regex("(?i)\\b$kw\\b[:]?")
            result = result.replace(regex, "")
        }
        return result.trim()
    }

    private fun cleanFieldValue(value: String): String {
        return value
            .replace(Regex("^[:;\\-=\\s]+"), "")
            .replace(Regex("[:;]+"), "")
            .trim()
    }

    private fun cleanDate(raw: String): String {
        val cleaned = raw.replace(Regex("[^0-9\\-/ ]"), "").trim()
        val matcher = Pattern.compile("(\\d{2}[-/ ]\\d{2}[-/ ]\\d{4})").matcher(cleaned)
        return if (matcher.find()) {
            matcher.group(1)?.replace(" ", "-")?.replace("/", "-") ?: cleaned
        } else {
            cleaned
        }
    }

    private fun cleanNikCandidate(candidate: String): String {
        val cleaned = candidate
            .replace(":", "")
            .replace(" ", "")
            .replace("O", "0")
            .replace("o", "0")
            .replace("D", "0")
            .replace("I", "1")
            .replace("l", "1")
            .replace("L", "1")
            .replace("Z", "2")
            .replace("S", "5")
            .replace("s", "5")
            .replace("B", "8")
            .filter { it.isDigit() }
        return cleaned
    }

    private fun findLoose16Digit(text: String): String {
        val lines = text.lines()
        for (line in lines) {
            val candidate = cleanNikCandidate(line)
            if (candidate.length == 16) {
                return candidate
            }
        }
        return ""
    }

    private fun isFieldHeader(text: String): Boolean {
        val upper = text.uppercase(Locale.ROOT)
        val headers = listOf("TEMPAT", "LAHIR", "JENIS", "ALAMAT", "RT", "KEL", "KEC", "AGAMA", "STATUS", "PEKERJAAN")
        return headers.any { upper.contains(it) }
    }
}
