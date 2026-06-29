package com.example.ui.sound

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.sin

object SoundSynth {
    private const val SAMPLE_RATE = 22050

    /**
     * Synthesizes and plays a pure sine wave tone at the specified frequency and volume.
     */
    fun playTone(frequency: Double, durationMs: Int, volume: Float = 0.3f) {
        val numSamples = (durationMs * SAMPLE_RATE / 1000)
        val generatedSnd = ByteArray(2 * numSamples)
        
        var idx = 0
        for (i in 0 until numSamples) {
            val dVal = i.toDouble() / SAMPLE_RATE
            // Apply a slight envelope (fade out) to avoid clicking pops at the end
            val fadeOutFactor = if (i > numSamples * 0.8) {
                (numSamples - i).toDouble() / (numSamples * 0.2)
            } else {
                1.0
            }
            val valDouble = sin(2.0 * Math.PI * frequency * dVal) * fadeOutFactor
            val valShort = (valDouble * 32767 * volume).toInt().toShort()
            generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
        }
        
        try {
            @Suppress("DEPRECATION")
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                generatedSnd.size,
                AudioTrack.MODE_STATIC
            )
            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
            
            // Auto release after sound finishes
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Safe cleanup ignore
                }
            }, durationMs.toLong() + 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick() {
        playTone(600.0, 40, 0.15f)
    }

    fun playHover() {
        playTone(880.0, 20, 0.1f)
    }

    fun playSuccess() {
        // High arpeggio
        playTone(523.25, 100, 0.2f) // C5
        Handler(Looper.getMainLooper()).postDelayed({
            playTone(659.25, 100, 0.2f) // E5
        }, 100)
        Handler(Looper.getMainLooper()).postDelayed({
            playTone(783.99, 180, 0.2f) // G5
        }, 200)
    }

    fun playFailure() {
        // Sad low buzzer
        playTone(220.0, 120, 0.25f)
        Handler(Looper.getMainLooper()).postDelayed({
            playTone(180.0, 220, 0.25f)
        }, 120)
    }

    fun playMeld() {
        // Magnificent glowing psychic synth cascade
        val frequencies = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 880.0, 1046.50, 1318.51)
        frequencies.forEachIndexed { index, freq ->
            Handler(Looper.getMainLooper()).postDelayed({
                playTone(freq, 150, 0.2f)
            }, (index * 80).toLong())
        }
    }
}
