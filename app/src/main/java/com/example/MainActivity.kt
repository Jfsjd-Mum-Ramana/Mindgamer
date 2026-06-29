package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CosmicBackground
import com.example.ui.sound.SoundSynth
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameScreen
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.SynapseCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = bottomPadding)
                    ) {
                        // Global interactive floating cosmic background
                        CosmicBackground(modifier = Modifier.fillMaxSize())

                        // Screen Router
                        when (viewModel.currentScreen) {
                            GameScreen.HUB -> GameHubScreen(viewModel)
                            GameScreen.AI_MELD -> AiMeldScreen(viewModel)
                            GameScreen.DUO_MELD -> DuoMeldScreen(viewModel)
                            GameScreen.SOLITAIRE -> SolitaireScreen(viewModel)
                            GameScreen.PSYCHIC -> PsychicZenScreen(viewModel)
                            GameScreen.HISTORY -> HistoryScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// GAME HUB SCREEN
// ==========================================
@Composable
fun GameHubScreen(viewModel: GameViewModel) {
    val records by viewModel.allRecords.collectAsState()
    val totalGames = records.size
    val totalMends = records.count { it.success }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_title")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Spacer(modifier = Modifier.height(30.dp))

            // Gorgeous Cosmic Title Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(titleScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFD0BCFF).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MIND MELD",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                        color = Color(0xFFD0BCFF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.scale(titleScale)
                    )
                    Text(
                        text = "📡 COGNITIVE HARMONICS ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0xFF00E5FF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle instructions
            Text(
                text = "Harmonize your mental frequencies with advanced AI or pass the device to a friend. Link matching thoughts in the Synapse matrix, or test your psychic projection.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Archives", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Text("$totalGames", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Successful Melds", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Text("$totalMends", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SELECT AN OPERATIONAL WEB",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )
        }

        // MODE LIST
        item {
            HubCard(
                title = "AI Telepathy",
                tagline = "PLAY COOP WITH GEMINI",
                description = "Enter words connecting random starting terms. Gemini guesses concurrently. Merge your thoughts into the exact same word!",
                icon = "🤖",
                borderColor = Color(0xFFD0BCFF),
                onClick = {
                    viewModel.startNewAiGame()
                    viewModel.currentScreen = GameScreen.AI_MELD
                },
                testTag = "ai_mode_card"
            )
        }

        item {
            HubCard(
                title = "Local Duo Pass & Play",
                tagline = "DIVERGENT WORD CONVERGENCE",
                description = "Play offline with a friend next to you. Type guesses secretly. If your answers differ, they become the starting words of the next round!",
                icon = "👥",
                borderColor = Color(0xFF00E5FF),
                onClick = {
                    viewModel.startNewDuoGame()
                    viewModel.currentScreen = GameScreen.DUO_MELD
                },
                testTag = "duo_mode_card"
            )
        }

        item {
            HubCard(
                title = "Synapse Connect",
                tagline = "CONCEPT ASSOCIATION MATRIX",
                description = "Link conceptual word pairs inside a beautiful 3x3 grid. Features handcrafted levels or dynamic on-demand AI puzzle generation.",
                icon = "🧠",
                borderColor = Color(0xFFEFB8C8),
                onClick = {
                    viewModel.startSolitaireLevel(1)
                    viewModel.currentScreen = GameScreen.SOLITAIRE
                },
                testTag = "solitaire_mode_card"
            )
        }

        item {
            HubCard(
                title = "Psychic Zen Sensor",
                tagline = "ELEMENTAL ROTATION RADAR",
                description = "Project your mind forward to predict which colored crystal or symbol the rotating alignment pointer will charge up next.",
                icon = "🔮",
                borderColor = Color(0xFFFF9E00),
                onClick = {
                    viewModel.resetPsychicStats()
                    viewModel.currentScreen = GameScreen.PSYCHIC
                },
                testTag = "psychic_mode_card"
            )
        }

        item {
            HubCard(
                title = "Mental Archives",
                tagline = "COGNITIVE SYNCHRONICITY LOG",
                description = "View complete historical stats, successfully matched words, and turn-by-turn convergence timelines stored securely in local Room db.",
                icon = "📜",
                borderColor = Color.White.copy(alpha = 0.4f),
                onClick = {
                    viewModel.currentScreen = GameScreen.HISTORY
                },
                testTag = "history_mode_card"
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun HubCard(
    title: String,
    tagline: String,
    description: String,
    icon: String,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clickable {
                SoundSynth.playClick()
                onClick()
            }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Icon Holder
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.12f))
                    .border(1.dp, borderColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tagline,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = borderColor
                )
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open Mode",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(20.dp)
            )
        }
    }
}


// ==========================================
// 1. AI MIND MELD SCREEN
// ==========================================
@Composable
fun AiMeldScreen(viewModel: GameViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_nodes")
    val ringSizeMultiplier by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nodes"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    SoundSynth.playClick()
                    viewModel.currentScreen = GameScreen.HUB
                },
                modifier = Modifier.testTag("ai_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "AI Mind Meld",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { viewModel.startNewAiGame() },
                modifier = Modifier.testTag("ai_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Game",
                    tint = Color(0xFFD0BCFF)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "ROUND ${viewModel.aiRound}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFD0BCFF),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Words Nodes Neural Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom neural synapse draw line linking nodes
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val midY = size.height / 2f
                        val paddingSide = 90f
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFFD0BCFF))
                            ),
                            start = Offset(paddingSide, midY),
                            end = Offset(size.width - paddingSide, midY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        // Central brain bridge spark
                        drawCircle(
                            color = Color(0xFFD0BCFF).copy(alpha = 0.3f),
                            radius = 20f * ringSizeMultiplier,
                            center = Offset(size.width / 2f, midY)
                        )
                    }

                    // Word A node (left side)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NodeContainer(word = viewModel.aiWordA, title = "Word A", color = Color(0xFF00E5FF))
                        NodeContainer(word = viewModel.aiWordB, title = "Word B", color = Color(0xFFD0BCFF))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Commentary Text Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👽", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = viewModel.aiCommentary,
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Similarity progress meter
                if (viewModel.aiRound > 1 || viewModel.aiMeldSuccess) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Thought Alignment Frequency",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                "${viewModel.aiSimilarity}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = getSimilarityColor(viewModel.aiSimilarity)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.aiSimilarity / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = getSimilarityColor(viewModel.aiSimilarity),
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                    }
                }

                // GUESS SUBMISSION BAR
                if (!viewModel.aiMeldSuccess) {
                    OutlinedTextField(
                        value = viewModel.aiUserGuess,
                        onValueChange = { viewModel.aiUserGuess = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_word_input"),
                        placeholder = { Text("Enter a connecting word...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                viewModel.submitAiGuess()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.04f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                            focusedIndicatorColor = Color(0xFFD0BCFF),
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.submitAiGuess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("ai_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                        shape = RoundedCornerShape(14.dp),
                        enabled = viewModel.aiUserGuess.trim().isNotEmpty() && !viewModel.aiIsLoading
                    ) {
                        if (viewModel.aiIsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF0F0B1E))
                        } else {
                            Text(
                                "SYNC FREQUENCY",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F0B1E),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                } else {
                    // Win state action button
                    Button(
                        onClick = { viewModel.startNewAiGame() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("ai_play_again_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "INITIATE NEW HARMONIC",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F0B1E),
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // History Trace logs
                Text(
                    text = "TRANSMISSION CHRONOLOGY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            if (viewModel.aiHistoryList.value.isEmpty()) {
                item {
                    Text(
                        "No past frequencies inside this cycle. Submit a word to initiate sync.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(viewModel.aiHistoryList.value.reversed()) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = log,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun NodeContainer(word: String, title: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .widthIn(min = 120.dp, max = 150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(2.dp, color, RoundedCornerShape(16.dp))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = word,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}


// ==========================================
// 2. DUO MELD SCREEN (PASS & PLAY)
// ==========================================
@Composable
fun DuoMeldScreen(viewModel: GameViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var inputWord by rememberSaveable { mutableStateOf("") }
    var obscureInput by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    SoundSynth.playClick()
                    viewModel.currentScreen = GameScreen.HUB
                },
                modifier = Modifier.testTag("duo_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Duo Pass & Play",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { viewModel.startNewDuoGame() },
                modifier = Modifier.testTag("duo_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Game",
                    tint = Color(0xFF00E5FF)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "ROUND ${viewModel.duoRound}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Words display box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NodeContainer(word = viewModel.duoWordA, title = "Word A", color = Color(0xFF00E5FF))
                    NodeContainer(word = viewModel.duoWordB, title = "Word B", color = Color(0xFFEFB8C8))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!viewModel.duoShowRevealScreen) {
                // TURN ENTRY FLOW
                item {
                    val pName = if (viewModel.duoCurrentTurnPlayer == 1) "PLAYER 1" else "PLAYER 2"
                    val accentColor = if (viewModel.duoCurrentTurnPlayer == 1) Color(0xFF00E5FF) else Color(0xFFEFB8C8)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$pName'S TURN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Enter one connecting word secretly. Tap eye to hide typing.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = inputWord,
                                onValueChange = { inputWord = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("duo_player_input"),
                                placeholder = { Text("Connection guess...") },
                                singleLine = true,
                                visualTransformation = if (obscureInput) PasswordVisualTransformation() else VisualTransformation.None,
                                trailingIcon = {
                                    IconButton(onClick = { obscureInput = !obscureInput }) {
                                        Text(if (obscureInput) "👁️" else "🕶️", fontSize = 18.sp)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.01f),
                                    focusedIndicatorColor = accentColor,
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.15f)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    if (viewModel.duoCurrentTurnPlayer == 1) {
                                        viewModel.submitPlayer1Guess(inputWord)
                                    } else {
                                        viewModel.submitPlayer2Guess(inputWord)
                                    }
                                    inputWord = ""
                                    obscureInput = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("duo_lock_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(12.dp),
                                enabled = inputWord.trim().isNotEmpty()
                            ) {
                                Text(
                                    "LOCK IN FREQUENCY",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F0B1E)
                                )
                            }
                        }
                    }
                }
            } else {
                // REVEAL SCREEN
                item {
                    val matchSuccess = viewModel.duoMeldSuccess
                    val resultText = if (matchSuccess) "MIND MELD SUCCESSFUL!" else "FREQUENCY MISALIGNMENT"
                    val statusColor = if (matchSuccess) Color(0xFF00E5FF) else Color(0xFFFF4B4B)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = resultText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = statusColor,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Venn Diagram overlap visualization
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(240.dp, 100.dp)) {
                                    val midY = size.height / 2f
                                    val offsetDistance = if (matchSuccess) 30f else 60f
                                    
                                    // P1 circle
                                    drawCircle(
                                        color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                                        radius = 45.dp.toPx(),
                                        center = Offset(size.width / 2f - offsetDistance, midY)
                                    )
                                    // P2 circle
                                    drawCircle(
                                        color = Color(0xFFEFB8C8).copy(alpha = 0.25f),
                                        radius = 45.dp.toPx(),
                                        center = Offset(size.width / 2f + offsetDistance, midY)
                                    )
                                    
                                    if (matchSuccess) {
                                        // Overlapping meld sparks
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.6f),
                                            radius = 12.dp.toPx(),
                                            center = Offset(size.width / 2f, midY)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Player 1 said", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                                        Text(
                                            viewModel.duoPlayer1Guess.uppercase(Locale.ROOT),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                    if (matchSuccess) {
                                        Text("🧬", fontSize = 24.sp)
                                    } else {
                                        Text("⚡", fontSize = 24.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Player 2 said", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                                        Text(
                                            viewModel.duoPlayer2Guess.uppercase(Locale.ROOT),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEFB8C8)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (matchSuccess) {
                                Text(
                                    "Stellar cognitive telepathy! You locked onto '${viewModel.duoPlayer1Guess}' in ${viewModel.duoRound} rounds.",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { viewModel.startNewDuoGame() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("duo_reveal_reset"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("INITIATE NEW MATCH", fontWeight = FontWeight.Bold, color = Color(0xFF0F0B1E))
                                }
                            } else {
                                Text(
                                    "The frequencies were divergent. Your words will now become the target Word A & B for Round ${viewModel.duoRound + 1}!",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { viewModel.proceedToNextDuoRound() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("duo_reveal_next"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("PROCEED TO ROUND ${viewModel.duoRound + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF0F0B1E))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Duo round trace history
                Text(
                    text = "ALIGNMENT PROGRESS HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            if (viewModel.duoHistoryList.value.isEmpty()) {
                item {
                    Text(
                        "No attempts made yet. Tap buttons to lock values.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(viewModel.duoHistoryList.value.reversed()) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = log,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}


// ==========================================
// 3. SYNAPSE SOLITAIRE GRID SCREEN
// ==========================================
@Composable
fun SolitaireScreen(viewModel: GameViewModel) {
    val cards = viewModel.solitaireCards.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    SoundSynth.playClick()
                    viewModel.currentScreen = GameScreen.HUB
                },
                modifier = Modifier.testTag("solitaire_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Synapse Solitaire",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Score: ${viewModel.solitaireScore}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEFB8C8),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Subtitle Level selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                LevelChip(level = 1, current = viewModel.solitaireLevel) { viewModel.startSolitaireLevel(1) }
                Spacer(modifier = Modifier.width(6.dp))
                LevelChip(level = 2, current = viewModel.solitaireLevel) { viewModel.startSolitaireLevel(2) }
                Spacer(modifier = Modifier.width(6.dp))
                LevelChip(level = 3, current = viewModel.solitaireLevel) { viewModel.startSolitaireLevel(3) }
            }

            Button(
                onClick = { viewModel.generateAiSolitaireGrid() },
                modifier = Modifier
                    .height(32.dp)
                    .testTag("solitaire_ai_generate"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                contentPadding = BoxDefaults.smallButtonPadding()
            ) {
                Text("🧠 GEN WITH AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F0B1E))
            }
        }

        // Status banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
        ) {
            Text(
                text = viewModel.solitaireStatus,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.solitaireIsLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFEFB8C8))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Forging multidimensional pathways...",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // 3x3 Card Grid
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // We chunks the list into groups of 3
                val rows = cards.chunked(3)
                items(rows) { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (card in rowCards) {
                            Box(modifier = Modifier.weight(1f)) {
                                SolitaireCardItem(card = card, onClick = { viewModel.selectSolitaireCard(card.id) })
                            }
                        }
                        // Fill empty cells if last row has < 3 elements
                        if (rowCards.size < 3) {
                            for (i in 0 until (3 - rowCards.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Action instructions footer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Hint",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Note: In offline levels, there is one wildcard distractor term that pairs with nothing. Filter your logic carefully!",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun LevelChip(level: Int, current: Int, onClick: () -> Unit) {
    val selected = level == current
    val activeColor = Color(0xFFEFB8C8)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) activeColor else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (selected) activeColor else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LV $level",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color(0xFF0F0B1E) else Color.White
        )
    }
}

@Composable
fun SolitaireCardItem(card: SynapseCard, onClick: () -> Unit) {
    val borderBrush = when {
        card.isMatched -> Brush.linearGradient(colors = listOf(Color(0xFF00FF87), Color(0xFF60EFFF)))
        card.isSelected -> Brush.linearGradient(colors = listOf(Color(0xFFD0BCFF), Color(0xFF00E5FF)))
        else -> Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f)))
    }

    val cardBg = when {
        card.isMatched -> Color(0xFF003816).copy(alpha = 0.5f)
        card.isSelected -> Color(0xFF231C3D).copy(alpha = 0.7f)
        else -> Color.White.copy(alpha = 0.03f)
    }

    val textColor = when {
        card.isMatched -> Color(0xFF00FF87)
        card.isSelected -> Color(0xFF00E5FF)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(if (card.isSelected || card.isMatched) 2.dp else 1.dp, borderBrush, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("solitaire_card_${card.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = card.word,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            if (card.isMatched) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Matched",
                    tint = Color(0xFF00FF87),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}


// ==========================================
// 4. PSYCHIC ZEN SCREEN (CRYSTAL SPINNER)
// ==========================================
@Composable
fun PsychicZenScreen(viewModel: GameViewModel) {
    val winRate = if (viewModel.psychicTotalGuesses > 0) {
        (viewModel.psychicHits.toFloat() / viewModel.psychicTotalGuesses.toFloat() * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    SoundSynth.playClick()
                    viewModel.currentScreen = GameScreen.HUB
                },
                modifier = Modifier.testTag("psychic_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Psychic Zen Predictor",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { viewModel.resetPsychicStats() },
                modifier = Modifier.testTag("psychic_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = Color(0xFFFF9E00)
                )
            }
        }

        // Stats scoreboard panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Attempts", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                Text("${viewModel.psychicTotalGuesses}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Success Rate", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                Text("$winRate%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Current Streak", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                Text("${viewModel.psychicStreak} 🔥", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9E00))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center wheel indicator display
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Draw rotating radar alignment pointer on custom canvas
            val wheelAngle = remember { Animatable(0f) }

            LaunchedEffect(viewModel.psychicWheelIsSpinning) {
                if (viewModel.psychicWheelIsSpinning) {
                    wheelAngle.animateTo(
                        targetValue = wheelAngle.value + 1440f, // 4 full loops
                        animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Draw 4 physical sector elements
                val colors = listOf(Color(0xFFFF4B4B), Color(0xFF9E26FF), Color(0xFF00E5FF), Color(0xFFFF9E00))
                for (i in 0..3) {
                    drawArc(
                        color = colors[i].copy(alpha = 0.08f),
                        startAngle = i * 90f,
                        sweepAngle = 90f,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                }

                // Outer neon boundary
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius,
                    style = Stroke(width = 4f)
                )

                // Active glowing segment indicator
                if (viewModel.psychicTargetIndex in 0..3) {
                    drawArc(
                        color = colors[viewModel.psychicTargetIndex].copy(alpha = 0.4f),
                        startAngle = viewModel.psychicTargetIndex * 90f,
                        sweepAngle = 90f,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                }

                // Draw rotating laser compass arm
                val radians = Math.toRadians((wheelAngle.value + (viewModel.psychicTargetIndex.coerceAtLeast(0) * 90) + 45).toDouble())
                val tipX = center.x + radius * 0.85f * cos(radians).toFloat()
                val tipY = center.y + radius * 0.85f * sin(radians).toFloat()

                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(tipX, tipY),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // Central node point
                drawCircle(
                    color = Color.White,
                    radius = 12f
                )
            }

            // Central icon text depending on state
            if (viewModel.psychicWheelIsSpinning) {
                Text("🌀", fontSize = 28.sp)
            } else if (viewModel.psychicTargetIndex >= 0) {
                val emotes = listOf("🔴", "🟣", "🔵", "🟡")
                Text(emotes[viewModel.psychicTargetIndex], fontSize = 28.sp)
            } else {
                Text("👁️", fontSize = 28.sp)
            }
        }

        // Status description
        Text(
            text = viewModel.psychicStatus,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Grid list of 4 crystals selectors
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val colors = listOf(Color(0xFFFF4B4B), Color(0xFF9E26FF), Color(0xFF00E5FF), Color(0xFFFF9E00))
            val symbols = listOf("🔴 PYRAMID", "🟣 ORB", "🔵 PRISM", "🟡 GEODE")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CrystalButton(
                    symbol = symbols[0],
                    color = colors[0],
                    isSelected = viewModel.psychicSelectedUserIndex == 0,
                    enabled = !viewModel.psychicWheelIsSpinning,
                    onClick = { viewModel.guessPsychicCrystal(0) },
                    modifier = Modifier.weight(1f).testTag("crystal_button_0")
                )
                CrystalButton(
                    symbol = symbols[1],
                    color = colors[1],
                    isSelected = viewModel.psychicSelectedUserIndex == 1,
                    enabled = !viewModel.psychicWheelIsSpinning,
                    onClick = { viewModel.guessPsychicCrystal(1) },
                    modifier = Modifier.weight(1f).testTag("crystal_button_1")
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CrystalButton(
                    symbol = symbols[2],
                    color = colors[2],
                    isSelected = viewModel.psychicSelectedUserIndex == 2,
                    enabled = !viewModel.psychicWheelIsSpinning,
                    onClick = { viewModel.guessPsychicCrystal(2) },
                    modifier = Modifier.weight(1f).testTag("crystal_button_2")
                )
                CrystalButton(
                    symbol = symbols[3],
                    color = colors[3],
                    isSelected = viewModel.psychicSelectedUserIndex == 3,
                    enabled = !viewModel.psychicWheelIsSpinning,
                    onClick = { viewModel.guessPsychicCrystal(3) },
                    modifier = Modifier.weight(1f).testTag("crystal_button_3")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun CrystalButton(
    symbol: String,
    color: Color,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.03f))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) color else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) color else Color.White
        )
    }
}


// ==========================================
// 5. ARCHIVES HISTORY SCREEN
// ==========================================
@Composable
fun HistoryScreen(viewModel: GameViewModel) {
    val records by viewModel.allRecords.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    SoundSynth.playClick()
                    viewModel.currentScreen = GameScreen.HUB
                },
                modifier = Modifier.testTag("history_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Mental Archives",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (records.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearGameHistory() },
                    modifier = Modifier.testTag("history_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = Color(0xFFFF4B4B)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (records.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📜", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No matches found in the synapses.",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(records) { record ->
                    val color = when (record.mode) {
                        "AI_MELD" -> Color(0xFFD0BCFF)
                        "DUO_MELD" -> Color(0xFF00E5FF)
                        "SOLITAIRE" -> Color(0xFFEFB8C8)
                        else -> Color(0xFFFF9E00)
                    }

                    val dateFormatted = remember(record.timestamp) {
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            sdf.format(Date(record.timestamp))
                        } catch (e: Exception) {
                            "Unknown date"
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getModeLabel(record.mode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = dateFormatted,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Target Result: ${record.finalWord.uppercase(Locale.ROOT)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Completed in ${record.rounds} intervals. Alignment: ${if (record.success) "PERFECT SYNC" else "DIVERGENT"}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )

                            if (record.wordsTimeline.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.02f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = record.wordsTimeline,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White.copy(alpha = 0.5f),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

fun getModeLabel(mode: String): String = when (mode) {
    "AI_MELD" -> "🤖 AI TELEPATHY"
    "DUO_MELD" -> "👥 LOCAL DUO"
    "SOLITAIRE" -> "🧠 SYNAPSE CONNECT"
    "PSYCHIC" -> "🔮 PSYCHIC ZEN"
    else -> "🕹️ GENERAL MODE"
}

fun getSimilarityColor(pct: Int): Color = when {
    pct >= 90 -> Color(0xFF00FF87) // Bright lime
    pct >= 70 -> Color(0xFF00E5FF) // Electric cyan
    pct >= 40 -> Color(0xFFFF9E00) // Amber
    else -> Color(0xFFFF4B4B)      // Red
}

// Custom simple box padding defaults for compact rendering
object BoxDefaults {
    fun smallButtonPadding() = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0B1E)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome to Mind Meld, $name!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD0BCFF)
        )
    }
}
