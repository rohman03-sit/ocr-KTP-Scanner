package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.KtpEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditKtpDialog(
    initialKtp: KtpEntity,
    onDismiss: () -> Unit,
    onSave: (KtpEntity) -> Unit
) {
    var nik by remember { mutableStateOf(initialKtp.nik) }
    var nama by remember { mutableStateOf(initialKtp.nama) }
    var tempatLahir by remember { mutableStateOf(initialKtp.tempatLahir) }
    var tanggalLahir by remember { mutableStateOf(initialKtp.tanggalLahir) }
    var jenisKelamin by remember { mutableStateOf(initialKtp.jenisKelamin) }
    var golonganDarah by remember { mutableStateOf(initialKtp.golonganDarah) }
    var alamat by remember { mutableStateOf(initialKtp.alamat) }
    var rtRw by remember { mutableStateOf(initialKtp.rtRw) }
    var kelDesa by remember { mutableStateOf(initialKtp.kelDesa) }
    var kecamatan by remember { mutableStateOf(initialKtp.kecamatan) }
    var agama by remember { mutableStateOf(initialKtp.agama) }
    var statusPerkawinan by remember { mutableStateOf(initialKtp.statusPerkawinan) }
    var pekerjaan by remember { mutableStateOf(initialKtp.pekerjaan) }
    var kewarganegaraan by remember { mutableStateOf(initialKtp.kewarganegaraan.ifBlank { "WNI" }) }
    var berlakuHingga by remember { mutableStateOf(initialKtp.berlakuHingga.ifBlank { "SEUMUR HIDUP" }) }
    var provinsi by remember { mutableStateOf(initialKtp.provinsi) }
    var kotaKabupaten by remember { mutableStateOf(initialKtp.kotaKabupaten) }

    val scrollState = rememberScrollState()

    val isNikValid = nik.length == 16 && nik.all { it.isDigit() }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialKtp.id == 0L) "Verifikasi Hasil OCR" else "Edit Data e-KTP",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Batal")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Periksa dan koreksi data hasil pembacaan OCR jika terdapat karakter yang belum sesuai sebelum disimpan ke database Room.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // NIK Field with validation counter
                OutlinedTextField(
                    value = nik,
                    onValueChange = { if (it.length <= 16) nik = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nomor Induk Kependudukan (NIK)*") },
                    placeholder = { Text("Contoh: 3174051208900003") },
                    supportingText = {
                        Text(
                            text = if (isNikValid) "NIK Valid (16 Digit)" else "${nik.length}/16 digit angka",
                            color = if (isNikValid) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    },
                    isError = nik.isNotEmpty() && !isNikValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_nik_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nama
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it.uppercase() },
                    label = { Text("Nama Lengkap*") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_nama_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tempat & Tanggal Lahir
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempatLahir,
                        onValueChange = { tempatLahir = it.uppercase() },
                        label = { Text("Tempat Lahir") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tanggalLahir,
                        onValueChange = { tanggalLahir = it },
                        label = { Text("Tgl Lahir") },
                        placeholder = { Text("DD-MM-YYYY") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Jenis Kelamin Selector
                Text(
                    text = "Jenis Kelamin",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = jenisKelamin.equals("LAKI-LAKI", ignoreCase = true),
                        onClick = { jenisKelamin = "LAKI-LAKI" },
                        label = { Text("LAKI-LAKI") },
                        leadingIcon = if (jenisKelamin.equals("LAKI-LAKI", ignoreCase = true)) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = jenisKelamin.equals("PEREMPUAN", ignoreCase = true),
                        onClick = { jenisKelamin = "PEREMPUAN" },
                        label = { Text("PEREMPUAN") },
                        leadingIcon = if (jenisKelamin.equals("PEREMPUAN", ignoreCase = true)) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Golongan Darah
                Text(
                    text = "Golongan Darah",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("A", "B", "AB", "O", "-").forEach { bg ->
                        FilterChip(
                            selected = golonganDarah.equals(bg, ignoreCase = true),
                            onClick = { golonganDarah = bg },
                            label = { Text(bg) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alamat
                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it.uppercase() },
                    label = { Text("Alamat") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // RT/RW, Kelurahan/Desa, Kecamatan
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rtRw,
                        onValueChange = { rtRw = it },
                        label = { Text("RT/RW") },
                        placeholder = { Text("001/002") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                    OutlinedTextField(
                        value = kelDesa,
                        onValueChange = { kelDesa = it.uppercase() },
                        label = { Text("Kel/Desa") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = kecamatan,
                    onValueChange = { kecamatan = it.uppercase() },
                    label = { Text("Kecamatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Agama Quick Chips
                Text(
                    text = "Agama",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ISLAM", "KRISTEN", "KATOLIK", "HINDU", "BUDDHA").forEach { ag ->
                        FilterChip(
                            selected = agama.equals(ag, ignoreCase = true),
                            onClick = { agama = ag },
                            label = { Text(ag, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = agama,
                    onValueChange = { agama = it.uppercase() },
                    label = { Text("Agama (Teks)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Perkawinan
                Text(
                    text = "Status Perkawinan",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("BELUM KAWIN", "KAWIN", "CERAI HIDUP", "CERAI MATI").forEach { st ->
                        FilterChip(
                            selected = statusPerkawinan.equals(st, ignoreCase = true),
                            onClick = { statusPerkawinan = st },
                            label = { Text(st, fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = statusPerkawinan,
                    onValueChange = { statusPerkawinan = it.uppercase() },
                    label = { Text("Status Perkawinan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pekerjaan
                OutlinedTextField(
                    value = pekerjaan,
                    onValueChange = { pekerjaan = it.uppercase() },
                    label = { Text("Pekerjaan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kewarganegaraan & Berlaku Hingga
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kewarganegaraan,
                        onValueChange = { kewarganegaraan = it.uppercase() },
                        label = { Text("Kewarganegaraan") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = berlakuHingga,
                        onValueChange = { berlakuHingga = it.uppercase() },
                        label = { Text("Berlaku Hingga") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Header Wilayah
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = provinsi,
                        onValueChange = { provinsi = it.uppercase() },
                        label = { Text("Provinsi") },
                        placeholder = { Text("PROVINSI DKI JAKARTA") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = kotaKabupaten,
                        onValueChange = { kotaKabupaten = it.uppercase() },
                        label = { Text("Kota/Kabupaten") },
                        placeholder = { Text("KOTA JAKARTA SELATAN") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialKtp.copy(
                        nik = nik.trim(),
                        nama = nama.trim(),
                        tempatLahir = tempatLahir.trim(),
                        tanggalLahir = tanggalLahir.trim(),
                        jenisKelamin = jenisKelamin.trim(),
                        golonganDarah = golonganDarah.trim(),
                        alamat = alamat.trim(),
                        rtRw = rtRw.trim(),
                        kelDesa = kelDesa.trim(),
                        kecamatan = kecamatan.trim(),
                        agama = agama.trim(),
                        statusPerkawinan = statusPerkawinan.trim(),
                        pekerjaan = pekerjaan.trim(),
                        kewarganegaraan = kewarganegaraan.trim(),
                        berlakuHingga = berlakuHingga.trim(),
                        provinsi = provinsi.trim(),
                        kotaKabupaten = kotaKabupaten.trim()
                    )
                    onSave(updated)
                },
                enabled = nik.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("save_ktp_confirm_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan ke Database")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
