package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KtpEntity

@Composable
fun KtpCardView(
    ktp: KtpEntity,
    modifier: Modifier = Modifier,
    showDetailedFields: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E40AF),
                            Color(0xFF1E3A8A),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                // Header Republik Indonesia
                Text(
                    text = "REPUBLIK INDONESIA",
                    color = Color(0xFFFDE047),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (ktp.provinsi.isNotBlank()) {
                    Text(
                        text = ktp.provinsi.uppercase(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (ktp.kotaKabupaten.isNotBlank()) {
                    Text(
                        text = ktp.kotaKabupaten.uppercase(),
                        color = Color(0xFF93C5FD),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // NIK Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "NIK",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NIK",
                        color = Color(0xFF93C5FD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ktp.nik.ifBlank { "----------------" },
                        color = Color(0xFFFEF08A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Identity Body
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column: Identity Data Fields
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        KtpRowField(label = "Nama", value = ktp.nama)
                        KtpRowField(
                            label = "Tempat/Tgl Lahir",
                            value = listOf(ktp.tempatLahir, ktp.tanggalLahir)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                        )
                        KtpRowField(
                            label = "Jenis Kelamin",
                            value = if (ktp.golonganDarah != "-" && ktp.golonganDarah.isNotBlank()) {
                                "${ktp.jenisKelamin} (Gol. ${ktp.golonganDarah})"
                            } else {
                                ktp.jenisKelamin
                            }
                        )

                        if (showDetailedFields) {
                            KtpRowField(label = "Alamat", value = ktp.alamat)
                            if (ktp.rtRw.isNotBlank()) {
                                KtpRowField(label = "   RT/RW", value = ktp.rtRw)
                            }
                            if (ktp.kelDesa.isNotBlank()) {
                                KtpRowField(label = "   Kel/Desa", value = ktp.kelDesa)
                            }
                            if (ktp.kecamatan.isNotBlank()) {
                                KtpRowField(label = "   Kecamatan", value = ktp.kecamatan)
                            }
                            KtpRowField(label = "Agama", value = ktp.agama)
                            KtpRowField(label = "Status", value = ktp.statusPerkawinan)
                            KtpRowField(label = "Pekerjaan", value = ktp.pekerjaan)
                            KtpRowField(label = "Kewarganegaraan", value = ktp.kewarganegaraan)
                            KtpRowField(label = "Berlaku Hingga", value = ktp.berlakuHingga)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Column: Chip & Photo Placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Microchip emblem
                        Box(
                            modifier = Modifier
                                .size(36.dp, 28.dp)
                                .background(Color(0xFFD97706), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = "Smart Chip",
                                tint = Color(0xFFFEF3C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Photo Card Frame
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 80.dp)
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .border(1.5.dp, Color(0xFF64748B), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF334155), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Foto KTP",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "PASFOTO",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KtpRowField(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color(0xFF93C5FD),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(105.dp)
        )
        Text(
            text = ": ",
            color = Color(0xFF93C5FD),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}
