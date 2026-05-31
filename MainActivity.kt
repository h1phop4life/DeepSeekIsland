package com.dynamicisland.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // Mevcut değerleri topla
            val horizontalPosition by preferencesManager.horizontalPosition.collectAsState(initial = "left")
            val offsetX by preferencesManager.offsetX.collectAsState(initial = 0)
            val offsetY by preferencesManager.offsetY.collectAsState(initial = 0)
            val smallDur by preferencesManager.smallDurationSec.collectAsState(initial = 5)
            val expandedDur by preferencesManager.expandedDurationSec.collectAsState(initial = 10)
            val textColorHex by preferencesManager.textColor.collectAsState(initial = "#FFFFFF")
            val buttonColorHex by preferencesManager.buttonColor.collectAsState(initial = "#FFFFFF")
            val backgroundColorHex by preferencesManager.backgroundColor.collectAsState(initial = "#000000")
            val textDynamic by preferencesManager.textDynamic.collectAsState(initial = false)
            val buttonDynamic by preferencesManager.buttonDynamic.collectAsState(initial = false)
            val backgroundDynamic by preferencesManager.backgroundDynamic.collectAsState(initial = false)

            // Başlangıçta pil optimizasyonu uyarısı
            LaunchedEffect(Unit) {
                val pm = context.packageManager
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                if (pm.resolveActivity(intent, 0) != null) {
                    // Kullanıcıdan izin iste
                    startActivity(intent)
                }
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Dynamic Island Ayarları",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Pozisyon Seçimi
                        Text("Ada Konumu", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("left" to "Sol", "center" to "Orta", "right" to "Sağ").forEach { (key, label) ->
                                FilterChip(
                                    selected = horizontalPosition == key,
                                    onClick = {
                                        scope.launch { preferencesManager.setHorizontalPosition(key) }
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Offset X, Y
                        OutlinedTextField(
                            value = offsetX.toString(),
                            onValueChange = { new ->
                                new.toIntOrNull()?.let { scope.launch { preferencesManager.setOffsetX(it) } }
                            },
                            label = { Text("X Ekseni Kaydırma (px)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = offsetY.toString(),
                            onValueChange = { new ->
                                new.toIntOrNull()?.let { scope.launch { preferencesManager.setOffsetY(it) } }
                            },
                            label = { Text("Y Ekseni Kaydırma (px)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Süre Ayarları
                        Text("Zaman Ayarları", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        SliderWithLabel(
                            label = "Küçük Mod Süresi (sn)",
                            value = smallDur.toFloat(),
                            onValueChange = { scope.launch { preferencesManager.setSmallDuration(it.toInt()) } },
                            valueRange = 1f..15f
                        )
                        SliderWithLabel(
                            label = "Büyük Mod Süresi (sn)",
                            value = expandedDur.toFloat(),
                            onValueChange = { scope.launch { preferencesManager.setExpandedDuration(it.toInt()) } },
                            valueRange = 1f..30f
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Renk Ayarları
                        Text("Renk Paleti", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        ColorPickerWithDynamic(
                            label = "Yazı Rengi",
                            currentHex = textColorHex,
                            isDynamic = textDynamic,
                            onColorChange = { scope.launch { preferencesManager.setTextColor(it) } },
                            onDynamicToggle = { scope.launch { preferencesManager.setTextDynamic(it) } }
                        )
                        ColorPickerWithDynamic(
                            label = "Buton Rengi",
                            currentHex = buttonColorHex,
                            isDynamic = buttonDynamic,
                            onColorChange = { scope.launch { preferencesManager.setButtonColor(it) } },
                            onDynamicToggle = { scope.launch { preferencesManager.setButtonDynamic(it) } }
                        )
                        ColorPickerWithDynamic(
                            label = "Arka Plan Rengi",
                            currentHex = backgroundColorHex,
                            isDynamic = backgroundDynamic,
                            onColorChange = { scope.launch { preferencesManager.setBackgroundColor(it) } },
                            onDynamicToggle = { scope.launch { preferencesManager.setBackgroundDynamic(it) } }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Uygulama LSPosed modülü olarak çalışmaktadır. Değişiklikler anında uygulanır.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SliderWithLabel(label: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label: ${value.toInt()}")
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@Composable
fun ColorPickerWithDynamic(
    label: String,
    currentHex: String,
    isDynamic: Boolean,
    onColorChange: (String) -> Unit,
    onDynamicToggle: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentColor = try {
        Color(android.graphics.Color.parseColor(currentHex))
    } catch (e: Exception) {
        Color.White
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(currentColor, shape = MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { expanded = true }) {
                Text("Seç")
            }
            Switch(
                checked = isDynamic,
                onCheckedChange = onDynamicToggle
            )
            Text("Albüm Kapağından Al", fontSize = 10.sp)
        }
    }

    if (expanded) {
        // Basit bir renk girişi, ileride tam renk paleti eklenebilir
        var hexInput by remember { mutableStateOf(currentHex) }
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text("Renk Seç") },
            text = {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { hexInput = it },
                    label = { Text("HEX (#FFAABB)") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onColorChange(hexInput)
                    expanded = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { expanded = false }) { Text("İptal") }
            }
        )
    }
}