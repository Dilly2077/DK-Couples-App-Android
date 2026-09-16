package com.dk.together.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.dk.together.data.EveluneSocialStore
import com.dk.together.data.MemoryMoment
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val memoryDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
private fun memoryDate(epoch: Long): String = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(memoryDateFormatter)

@Composable
internal fun MemoryTimelineScreen(
    social: EveluneSocialStore,
    modifier: Modifier = Modifier,
) {
    var refresh by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    val memories = remember(refresh) { social.memories() }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Timeline", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = EveluneRoseDeep)
                    Text("The dates and moments you want to keep.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
                }
            }

            if (memories.isEmpty()) {
                item {
                    Surface(color = EveluneCard, shape = RoundedCornerShape(26.dp)) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("No memories yet", style = MaterialTheme.typography.titleLarge, color = EveluneInk)
                            Text(
                                "Tap + to add photos or videos from a date and save what made it special.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = EveluneMuted,
                            )
                        }
                    }
                }
            }

            items(memories, key = { it.id }) { memory ->
                MemoryMomentCard(memory)
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 22.dp),
            containerColor = EveluneRoseDeep,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add memory")
        }
    }

    if (showAdd) {
        AddMemoryDialog(
            onDismiss = { showAdd = false },
            onSave = { date, title, description, whySpecial, favourite, media ->
                social.addMemory(
                    dateMillis = date,
                    title = title,
                    description = description,
                    whySpecial = whySpecial,
                    favouritePart = favourite,
                    mediaUris = media,
                )
                showAdd = false
                refresh++
            },
        )
    }
}

@Composable
private fun MemoryMomentCard(memory: MemoryMoment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFB),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 1.dp,
    ) {
        Column {
            if (memory.mediaUris.isNotEmpty()) {
                MemoryCollage(memory)
            }
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    memoryDate(memory.dateMillis).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = EveluneRoseDeep,
                    letterSpacing = .5.sp,
                )
                Text(
                    memory.title.ifBlank { "Our date" },
                    fontFamily = FontFamily.Serif,
                    fontSize = 25.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Bold,
                    color = EveluneInk,
                )
                if (memory.description.isNotBlank()) {
                    Text(memory.description, style = MaterialTheme.typography.bodyLarge, color = EveluneInk)
                }
                if (memory.whySpecial.isNotBlank()) {
                    Text("Why it was special", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                    Text(memory.whySpecial, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
                }
                if (memory.favouritePart.isNotBlank()) {
                    Text("Favourite part", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                    Text(memory.favouritePart, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
                }
            }
        }
    }
}

@Composable
private fun MemoryCollage(memory: MemoryMoment) {
    val ordered = remember(memory.id, memory.mediaUris) {
        memory.mediaUris.sortedBy { it.hashCode() xor memory.id.hashCode() }
    }
    val shown = ordered.take(4)
    val hidden = (ordered.size - 4).coerceAtLeast(0)
    val gap = 3.dp

    Box(
        Modifier.fillMaxWidth().height(236.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
    ) {
        when (shown.size) {
            1 -> MemoryMediaTile(shown[0], Modifier.fillMaxSize())
            2 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                MemoryMediaTile(shown[0], Modifier.weight(1f).fillMaxHeight())
                MemoryMediaTile(shown[1], Modifier.weight(1f).fillMaxHeight())
            }
            3 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                MemoryMediaTile(shown[0], Modifier.weight(1.18f).fillMaxHeight())
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(gap)) {
                    MemoryMediaTile(shown[1], Modifier.weight(1f).fillMaxWidth())
                    MemoryMediaTile(shown[2], Modifier.weight(1f).fillMaxWidth())
                }
            }
            else -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    MemoryMediaTile(shown[0], Modifier.weight(1f).fillMaxHeight())
                    MemoryMediaTile(shown[1], Modifier.weight(1f).fillMaxHeight())
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    MemoryMediaTile(shown[2], Modifier.weight(1f).fillMaxHeight())
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        MemoryMediaTile(shown[3], Modifier.fillMaxSize())
                        if (hidden > 0) {
                            Box(
                                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .34f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("+$hidden", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryMediaTile(uriString: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uri = remember(uriString) { uriString.toUri() }
    val mime = remember(uriString) { context.contentResolver.getType(uri).orEmpty() }
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uriString) {
        value = withContext(Dispatchers.IO) { loadMemoryThumbnail(context, uri, mime) }
    }

    Box(modifier.background(EveluneRosePale), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(Icons.Filled.Image, contentDescription = null, tint = EveluneMuted, modifier = Modifier.size(28.dp))
        }
        if (mime.startsWith("video/")) {
            Surface(shape = CircleShape, color = Color.Black.copy(alpha = .44f)) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Video", tint = Color.White)
                }
            }
        }
    }
}

private fun loadMemoryThumbnail(context: android.content.Context, uri: Uri, mime: String): ImageBitmap? = try {
    val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= 29) {
        context.contentResolver.loadThumbnail(uri, Size(700, 700), null)
    } else if (mime.startsWith("video/")) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(0)
        } finally {
            retriever.release()
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
    bitmap?.asImageBitmap()
} catch (_: Exception) {
    null
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onSave: (Long, String, String, String, String, List<String>) -> Unit,
) {
    val context = LocalContext.current
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var whySpecial by remember { mutableStateOf("") }
    var favourite by remember { mutableStateOf("") }
    val media = remember { mutableStateListOf<String>() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
            val value = uri.toString()
            if (value !in media) media += value
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(Modifier.fillMaxSize(), color = EveluneBackground) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Close", tint = EveluneRoseDeep)
                    }
                    Text(
                        "Add a memory",
                        fontFamily = FontFamily.Serif,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = EveluneInk,
                    )
                }

                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    Text("Date", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                    Surface(
                        onClick = {
                            val current = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    dateMillis = LocalDate.of(year, month + 1, day)
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli()
                                },
                                current.year,
                                current.monthValue - 1,
                                current.dayOfMonth,
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFFCFB),
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 1.dp,
                    ) {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = EveluneRoseDeep)
                            Spacer(Modifier.width(10.dp))
                            Text(memoryDate(dateMillis), style = MaterialTheme.typography.bodyLarge, color = EveluneInk)
                        }
                    }

                    Button(
                        onClick = { picker.launch(arrayOf("image/*", "video/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                        shape = RoundedCornerShape(19.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text("Add photos or videos")
                    }

                    if (media.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(media, key = { it }) { uri ->
                                Box(Modifier.size(82.dp).clip(RoundedCornerShape(17.dp))) {
                                    MemoryMediaTile(uri, Modifier.fillMaxSize())
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        label = { Text("Title") },
                        placeholder = { Text("Dinner by the river") },
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        label = { Text("What did you do?") },
                    )
                    OutlinedTextField(
                        value = whySpecial,
                        onValueChange = { whySpecial = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(20.dp),
                        label = { Text("Why was it special?") },
                    )
                    OutlinedTextField(
                        value = favourite,
                        onValueChange = { favourite = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(20.dp),
                        label = { Text("Favourite part") },
                    )
                }

                Button(
                    onClick = { onSave(dateMillis, title, description, whySpecial, favourite, media.toList()) },
                    enabled = description.isNotBlank() || media.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                    shape = RoundedCornerShape(21.dp),
                ) {
                    Text("Save memory")
                }
            }
        }
    }
}
