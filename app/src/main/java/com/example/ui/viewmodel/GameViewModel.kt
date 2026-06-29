package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.GameRecord
import com.example.data.GameRepository
import com.example.ui.sound.SoundSynth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// --- Game UI States ---

enum class GameScreen {
    HUB,
    AI_MELD,
    DUO_MELD,
    SOLITAIRE,
    PSYCHIC,
    HISTORY
}

// Model for a Word Association pair
data class WordPair(val word1: String, val word2: String)

// Model for Synapse Card
data class SynapseCard(
    val id: Int,
    val word: String,
    val matchId: Int, // Cards with the same positive matchId are associated. Distractors have -1
    val isMatched: Boolean = false,
    val isSelected: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val allRecords: StateFlow<List<GameRecord>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.gameRecordDao())
        allRecords = repository.allRecords
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // Navigation State
    var currentScreen by mutableStateOf(GameScreen.HUB)

    // Predefined starting pairs for Mind Meld
    private val startingPairs = listOf(
        WordPair("Cat", "Box"),
        WordPair("Rain", "Electricity"),
        WordPair("Fire", "Water"),
        WordPair("Astronaut", "Ocean"),
        WordPair("Apple", "Gravity"),
        WordPair("Sun", "Snow"),
        WordPair("Phone", "Paper"),
        WordPair("Coffee", "Cold"),
        WordPair("Music", "Math"),
        WordPair("Volcano", "Ice"),
        WordPair("Clock", "Sand"),
        WordPair("Bird", "Metal"),
        WordPair("Tree", "Sky"),
        WordPair("Diamond", "Coal"),
        WordPair("Key", "Brain")
    )

    // ==========================================
    // 1. AI MIND MELD STATE & ACTIONS
    // ==========================================
    var aiWordA by mutableStateOf("")
    var aiWordB by mutableStateOf("")
    var aiRound by mutableStateOf(1)
    var aiUserGuess by mutableStateOf("")
    var aiLastUserGuess by mutableStateOf("")
    var aiLastAiGuess by mutableStateOf("")
    var aiCommentary by mutableStateOf("Ready to sync our brainwaves? Enter your guess.")
    var aiSimilarity by mutableStateOf(0)
    var aiIsLoading by mutableStateOf(false)
    var aiMeldSuccess by mutableStateOf(false)
    var aiHistoryList = mutableStateOf<List<String>>(emptyList())
    var aiTimeline = mutableListOf<String>()

    fun startNewAiGame() {
        val pair = startingPairs.random()
        aiWordA = pair.word1
        aiWordB = pair.word2
        aiRound = 1
        aiUserGuess = ""
        aiLastUserGuess = ""
        aiLastAiGuess = ""
        aiCommentary = "Starting words established. What one word connects '${aiWordA}' and '${aiWordB}'?"
        aiSimilarity = 0
        aiMeldSuccess = false
        aiHistoryList.value = emptyList()
        aiTimeline.clear()
        SoundSynth.playClick()
    }

    fun submitAiGuess() {
        if (aiUserGuess.trim().isEmpty() || aiIsLoading || aiMeldSuccess) return

        val cleanUserGuess = aiUserGuess.trim().lowercase(Locale.ROOT)
        aiIsLoading = true
        aiLastUserGuess = aiUserGuess.trim()
        aiTimeline.add("Round $aiRound: You guessed '$aiLastUserGuess'")
        SoundSynth.playClick()

        viewModelScope.launch {
            // First check if user guess literally melds with the current Word A/B
            if (cleanUserGuess == aiWordA.lowercase(Locale.ROOT) || cleanUserGuess == aiWordB.lowercase(Locale.ROOT)) {
                // Cannot guess starting words
                aiCommentary = "That's one of the current words! Try to find a merging concept instead."
                aiIsLoading = false
                SoundSynth.playFailure()
                return@launch
            }

            // Call Gemini
            val systemInstruction = """
                You are a psychic word telepathy assistant in a game called 'Mind Meld'.
                Your human partner has selected a word to connect: '$aiWordA' and '$aiWordB'.
                The partner's connecting guess is: '$cleanUserGuess'.
                You MUST select a single connecting word of your own that links '$aiWordA' and '$aiWordB' without repeating their guess exactly, but trying to guess the EXACT same word. 
                Your response must follow this strict format:
                GUESS: [Your single connection word, lowercase, alphabetic only]
                SIMILARITY: [Your estimated similarity score between 0 and 100 based on how closely aligned your word and their word are, e.g., 85]
                COMMENT: [A very short, playful, psychic comment of maximum 12 words commenting on their guess and yours]
            """.trimIndent()

            val prompt = """
                Current target words: '$aiWordA' and '$aiWordB'
                My connecting guess: '$cleanUserGuess'
                Please think of your guess and respond with the required format.
            """.trimIndent()

            val aiResponse = withContext(Dispatchers.IO) {
                GeminiClient.getAiResponse(prompt, systemInstruction)
            }

            // Parse response
            var parsedGuess = ""
            var parsedSimilarity = 30
            var parsedComment = "The cosmos is shifting... Try again!"

            if (aiResponse == "ERROR_API_KEY_MISSING") {
                // OFFLINE / NO API KEY FALLBACK
                // Smart local fallback to make the game fully playable immediately
                parsedGuess = generateOfflineAiGuess(aiWordA, aiWordB, cleanUserGuess)
                parsedSimilarity = calculateOfflineSimilarity(cleanUserGuess, parsedGuess)
                parsedComment = "📡 [Local Mode] Telepathy simulation active. Synchronizing waves..."
            } else {
                try {
                    val lines = aiResponse.split("\n")
                    for (line in lines) {
                        if (line.startsWith("GUESS:", ignoreCase = true)) {
                            parsedGuess = line.substringAfter("GUESS:").trim().replace("[^a-zA-Z]".toRegex(), "").lowercase(Locale.ROOT)
                        } else if (line.startsWith("SIMILARITY:", ignoreCase = true)) {
                            parsedSimilarity = line.substringAfter("SIMILARITY:").trim().filter { it.isDigit() }.toIntOrNull() ?: 50
                        } else if (line.startsWith("COMMENT:", ignoreCase = true)) {
                            parsedComment = line.substringAfter("COMMENT:").trim()
                        }
                    }
                } catch (e: Exception) {
                    parsedGuess = "connection"
                    parsedSimilarity = 40
                    parsedComment = "Our brainwaves tangled in cosmic noise! Try to merge again."
                }
            }

            if (parsedGuess.isEmpty()) {
                parsedGuess = "synergy"
            }

            aiLastAiGuess = parsedGuess
            aiSimilarity = parsedSimilarity
            aiCommentary = parsedComment
            aiTimeline.add("Round $aiRound: AI guessed '$parsedGuess'")

            // Did we meld?
            if (cleanUserGuess == parsedGuess.lowercase(Locale.ROOT) || parsedSimilarity >= 98) {
                aiMeldSuccess = true
                aiCommentary = "🎉 MIND MELD COMPLETED! We both converged on '$parsedGuess'!"
                aiTimeline.add("MELD COMPLETED on '$parsedGuess' in $aiRound rounds!")
                SoundSynth.playMeld()
                saveGameRecord(
                    mode = "AI_MELD",
                    finalWord = parsedGuess,
                    rounds = aiRound,
                    success = true,
                    timeline = aiTimeline.joinToString("\n")
                )
            } else {
                // Shift target words to the new guesses for next round
                aiWordA = cleanUserGuess.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                aiWordB = parsedGuess.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                aiRound++
                aiHistoryList.value = aiHistoryList.value + "Round ${aiRound - 1}: You said '$cleanUserGuess', AI said '$parsedGuess' (Similarity $parsedSimilarity%)"
                aiUserGuess = ""
                SoundSynth.playSuccess() // Encouragement chirp
            }

            aiIsLoading = false
        }
    }

    private fun generateOfflineAiGuess(w1: String, w2: String, userGuess: String): String {
        // Fun offline answers based on common connections
        val connections = mapOf(
            "cat" to mapOf("box" to "cardboard", "dog" to "pet", "mouse" to "chase"),
            "rain" to mapOf("electricity" to "lightning", "sun" to "rainbow", "water" to "flood"),
            "fire" to mapOf("water" to "steam", "wood" to "smoke", "ice" to "melt"),
            "astronaut" to mapOf("ocean" to "sub", "sky" to "space", "stars" to "galaxy"),
            "apple" to mapOf("gravity" to "newton", "pie" to "baking", "orange" to "fruit")
        )
        val k1 = w1.lowercase(Locale.ROOT)
        val k2 = w2.lowercase(Locale.ROOT)
        
        val guess = connections[k1]?.get(k2) ?: connections[k2]?.get(k1)
        if (guess != null && guess != userGuess) return guess

        // Simple default algorithm for random combinations
        val list = listOf("energy", "fusion", "link", "nucleus", "network", "synapse", "bridge", "portal", "spark", "pulse")
        return list.filter { it != userGuess }.random()
    }

    private fun calculateOfflineSimilarity(g1: String, g2: String): Int {
        if (g1 == g2) return 100
        // Levenshtein-like basic metric or random realistic score
        val matchCount = g1.toSet().intersect(g2.toSet()).size
        return (30 + (matchCount * 12)).coerceIn(15, 95)
    }


    // ==========================================
    // 2. DUO MELD STATE & ACTIONS (Pass & Play)
    // ==========================================
    var duoWordA by mutableStateOf("")
    var duoWordB by mutableStateOf("")
    var duoRound by mutableStateOf(1)
    
    // Player turn flow
    var duoPlayer1Guess by mutableStateOf("")
    var duoPlayer2Guess by mutableStateOf("")
    var duoCurrentTurnPlayer by mutableStateOf(1) // 1 or 2
    var duoShowRevealScreen by mutableStateOf(false)
    var duoMeldSuccess by mutableStateOf(false)
    var duoHistoryList = mutableStateOf<List<String>>(emptyList())
    var duoTimeline = mutableListOf<String>()

    fun startNewDuoGame() {
        val pair = startingPairs.random()
        duoWordA = pair.word1
        duoWordB = pair.word2
        duoRound = 1
        duoPlayer1Guess = ""
        duoPlayer2Guess = ""
        duoCurrentTurnPlayer = 1
        duoShowRevealScreen = false
        duoMeldSuccess = false
        duoHistoryList.value = emptyList()
        duoTimeline.clear()
        SoundSynth.playClick()
    }

    fun submitPlayer1Guess(guess: String) {
        if (guess.trim().isEmpty()) return
        duoPlayer1Guess = guess.trim()
        duoCurrentTurnPlayer = 2
        SoundSynth.playClick()
    }

    fun submitPlayer2Guess(guess: String) {
        if (guess.trim().isEmpty()) return
        duoPlayer2Guess = guess.trim()
        duoShowRevealScreen = true
        duoTimeline.add("Round $duoRound: P1 said '$duoPlayer1Guess', P2 said '$duoPlayer2Guess'")
        
        // Check match immediately
        if (duoPlayer1Guess.lowercase(Locale.ROOT) == duoPlayer2Guess.lowercase(Locale.ROOT)) {
            duoMeldSuccess = true
            SoundSynth.playMeld()
            saveGameRecord(
                mode = "DUO_MELD",
                finalWord = duoPlayer1Guess,
                rounds = duoRound,
                success = true,
                timeline = duoTimeline.joinToString("\n")
            )
        } else {
            SoundSynth.playFailure()
        }
    }

    fun proceedToNextDuoRound() {
        if (duoMeldSuccess) return
        
        // Guesses become the new starting words!
        duoWordA = duoPlayer1Guess.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        duoWordB = duoPlayer2Guess.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        
        duoHistoryList.value = duoHistoryList.value + "Round $duoRound: P1: '$duoPlayer1Guess' | P2: '$duoPlayer2Guess'"
        duoRound++
        duoPlayer1Guess = ""
        duoPlayer2Guess = ""
        duoCurrentTurnPlayer = 1
        duoShowRevealScreen = false
        SoundSynth.playClick()
    }


    // ==========================================
    // 3. SYNAPSE CONNECT SOLITAIRE STATE & ACTIONS
    // ==========================================
    var solitaireScore by mutableStateOf(0)
    var solitaireLevel by mutableStateOf(1)
    var solitaireCards = mutableStateOf<List<SynapseCard>>(emptyList())
    var solitaireStatus by mutableStateOf("Link the matching conceptual pairs! Tap a card to select.")
    var solitaireIsLoading by mutableStateOf(false)

    // Handcrafted grid levels
    private val levelData = mapOf(
        1 to listOf(
            "Spaceship" to 1, "Astronaut" to 1,
            "Spaghetti" to 2, "Fork" to 2,
            "Novel" to 3, "Library" to 3,
            "Volcano" to 4, "Lava" to 4,
            "Pineapple" to -1 // Distractor
        ),
        2 to listOf(
            "Keyboard" to 1, "Computer" to 1,
            "Anchor" to 2, "Captain" to 2,
            "Brush" to 3, "Painting" to 3,
            "Garlic" to 4, "Vampire" to 4,
            "Bicycle" to -1 // Distractor
        ),
        3 to listOf(
            "Newton" to 1, "Apple" to 1,
            "Caffeine" to 2, "Coffee" to 2,
            "Rhythm" to 3, "Drums" to 3,
            "Doctor" to 4, "Stethoscope" to 4,
            "Paperclip" to -1
        )
    )

    fun startSolitaireLevel(level: Int) {
        solitaireLevel = level
        solitaireIsLoading = false
        val words = levelData[level] ?: levelData[1]!!
        
        // Construct and shuffle
        solitaireCards.value = words.mapIndexed { idx, pair ->
            SynapseCard(id = idx, word = pair.first, matchId = pair.second)
        }.shuffled()
        
        solitaireStatus = "Level $level: Find all 4 conceptual pairs! Avoid the distractor."
        SoundSynth.playClick()
    }

    fun selectSolitaireCard(cardId: Int) {
        val currentList = solitaireCards.value
        val selectedCard = currentList.find { it.id == cardId } ?: return
        if (selectedCard.isMatched) return

        SoundSynth.playClick()

        // Toggle selection
        val updatedList = currentList.map { card ->
            if (card.id == cardId) {
                card.copy(isSelected = !card.isSelected)
            } else card
        }

        solitaireCards.value = updatedList

        // Check if two cards are selected
        val activeSelection = updatedList.filter { it.isSelected }
        if (activeSelection.size == 2) {
            val first = activeSelection[0]
            val second = activeSelection[1]

            viewModelScope.launch {
                withContext(Dispatchers.Default) {
                    kotlinx.coroutines.delay(400) // Small delay for visual feedback
                }
                
                if (first.matchId == second.matchId && first.matchId != -1) {
                    // MATCH SUCCESS!
                    solitaireCards.value = solitaireCards.value.map { card ->
                        if (card.id == first.id || card.id == second.id) {
                            card.copy(isMatched = true, isSelected = false)
                        } else card
                    }
                    solitaireStatus = "Match found! '${first.word}' & '${second.word}' are linked!"
                    SoundSynth.playSuccess()

                    // Check win condition
                    val totalMatchesNeeded = 4
                    val matchedCount = solitaireCards.value.count { it.isMatched } / 2
                    if (matchedCount == totalMatchesNeeded) {
                        solitaireStatus = "🎉 Level Complete! Fantastic conceptual telepathy."
                        solitaireScore += 100
                        SoundSynth.playMeld()
                        saveGameRecord(
                            mode = "SOLITAIRE",
                            finalWord = "Level $solitaireLevel Cleared",
                            rounds = 1,
                            success = true,
                            timeline = "Cleared Level $solitaireLevel"
                        )
                    }
                } else {
                    // MATCH FAIL
                    solitaireCards.value = solitaireCards.value.map { card ->
                        if (card.id == first.id || card.id == second.id) {
                            card.copy(isSelected = false)
                        } else card
                    }
                    solitaireStatus = "Divergent thoughts. Those concepts don't align!"
                    SoundSynth.playFailure()
                }
            }
        }
    }

    /**
     * Dynamically generates a brand new solitaire puzzle grid using Gemini!
     */
    fun generateAiSolitaireGrid() {
        if (solitaireIsLoading) return
        solitaireIsLoading = true
        solitaireStatus = "AI is fabricating a custom multi-dimensional word matrix..."
        SoundSynth.playClick()

        viewModelScope.launch {
            val systemInstruction = """
                You are a puzzle master generator for 'Synapse Connect'.
                Generate exactly 4 pairs of highly associated words, plus 1 distractor word that does not pair with any of them.
                Output your response EXACTLY in this JSON array structure, and nothing else:
                [
                  {"word": "WordA1", "matchId": 1},
                  {"word": "WordA2", "matchId": 1},
                  {"word": "WordB1", "matchId": 2},
                  {"word": "WordB2", "matchId": 2},
                  {"word": "WordC1", "matchId": 3},
                  {"word": "WordC2", "matchId": 3},
                  {"word": "WordD1", "matchId": 4},
                  {"word": "WordD2", "matchId": 4},
                  {"word": "DistractorWord", "matchId": -1}
                ]
                Keep words short (1 word, alphanumeric, uppercase first letter). Be highly creative with connections!
            """.trimIndent()

            val aiResponse = withContext(Dispatchers.IO) {
                GeminiClient.getAiResponse("Generate a unique association matrix.", systemInstruction)
            }

            if (aiResponse == "ERROR_API_KEY_MISSING") {
                solitaireStatus = "Offline Mode: AI generation requires a Gemini API key. Loading Level 1 instead."
                solitaireIsLoading = false
                startSolitaireLevel(1)
                SoundSynth.playFailure()
                return@launch
            }

            try {
                // Parse manually or with Moshi (regex parse is highly resilient to any text surrounding JSON)
                val regex = """\{\s*"word"\s*:\s*"([^"]+)"\s*,\s*"matchId"\s*:\s*(-?\d+)\s*\}""".toRegex()
                val matches = regex.findAll(aiResponse)
                val cards = mutableListOf<SynapseCard>()
                
                var idCounter = 0
                for (match in matches) {
                    val word = match.groupValues[1]
                    val matchId = match.groupValues[2].toInt()
                    cards.add(SynapseCard(id = idCounter++, word = word, matchId = matchId))
                }

                if (cards.size == 9) {
                    solitaireCards.value = cards.shuffled()
                    solitaireStatus = "🌌 Custom AI Matrix Loaded! Discover the connected synapses."
                    SoundSynth.playMeld()
                } else {
                    // Fallback if parsing failed
                    solitaireStatus = "Web noise interfered! Loading localized Level 2 matrix instead."
                    startSolitaireLevel(2)
                }
            } catch (e: Exception) {
                solitaireStatus = "Error crafting matrix. Loading standard matrix instead."
                startSolitaireLevel(1)
            }
            solitaireIsLoading = false
        }
    }


    // ==========================================
    // 4. PSYCHIC ZEN STATE & ACTIONS
    // ==========================================
    var psychicStreak by mutableStateOf(0)
    var psychicTotalGuesses by mutableStateOf(0)
    var psychicHits by mutableStateOf(0)
    var psychicTargetIndex by mutableStateOf(-1) // Index of active crystal (0 to 3)
    var psychicSelectedUserIndex by mutableStateOf(-1)
    var psychicWheelIsSpinning by mutableStateOf(false)
    var psychicStatus by mutableStateOf("Focus your mental energy... Select a symbol below.")

    val crystals = listOf(
        Pair("Red Pyramidal", 0xFFFF4B4B),
        Pair("Violet Orb", 0xFF9E26FF),
        Pair("Cyan Prism", 0xFF00E5FF),
        Pair("Amber Geode", 0xFFFF9E00)
    )

    fun guessPsychicCrystal(selectedIndex: Int) {
        if (psychicWheelIsSpinning) return
        psychicSelectedUserIndex = selectedIndex
        psychicWheelIsSpinning = true
        psychicStatus = "Tuning your third eye... Wheel is rotating..."
        SoundSynth.playClick()

        viewModelScope.launch {
            // Spin wheel simulation
            val cycles = 15
            for (i in 0 until cycles) {
                psychicTargetIndex = (psychicTargetIndex + 1) % 4
                SoundSynth.playTone(400.0 + (psychicTargetIndex * 150), 40, 0.1f)
                withContext(Dispatchers.Default) {
                    kotlinx.coroutines.delay((100 + (i * 15)).toLong()) // Slowing down
                }
            }

            // Real target (25% pure random probability, perfect prediction simulation)
            val realTarget = (0..3).random()
            psychicTargetIndex = realTarget
            psychicWheelIsSpinning = false
            psychicTotalGuesses++

            if (selectedIndex == realTarget) {
                psychicHits++
                psychicStreak++
                psychicStatus = "✨ TELEPATHIC RESONANCE! You predicted the ${crystals[realTarget].first} successfully!"
                SoundSynth.playMeld()
                
                if (psychicStreak >= 3) {
                    saveGameRecord(
                        mode = "PSYCHIC",
                        finalWord = "Streak of $psychicStreak",
                        rounds = psychicTotalGuesses,
                        success = true,
                        timeline = "Achieved 3+ Prediction Streak!"
                    )
                }
            } else {
                psychicStreak = 0
                psychicStatus = "Divergent energy. The machine manifested the ${crystals[realTarget].first}."
                SoundSynth.playFailure()
            }
        }
    }

    fun resetPsychicStats() {
        psychicStreak = 0
        psychicTotalGuesses = 0
        psychicHits = 0
        psychicTargetIndex = -1
        psychicSelectedUserIndex = -1
        psychicStatus = "Focus your mind. Select a symbol below."
        SoundSynth.playClick()
    }


    // ==========================================
    // 5. DATABASE UTILS
    // ==========================================
    private fun saveGameRecord(
        mode: String,
        finalWord: String,
        rounds: Int,
        success: Boolean,
        timeline: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveRecord(
                GameRecord(
                    mode = mode,
                    finalWord = finalWord,
                    rounds = rounds,
                    success = success,
                    wordsTimeline = timeline
                )
            )
        }
    }

    fun clearGameHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearRecords()
        }
        SoundSynth.playFailure()
    }
}
