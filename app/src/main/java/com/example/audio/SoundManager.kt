package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun playClick() {
        if (!isSoundEnabled) return
        audioScope.launch {
            playTone(880.0, 35, 0.25f)
        }
    }

    fun playMove() {
        if (!isSoundEnabled) return
        audioScope.launch {
            playTone(620.0, 40, 0.2f)
        }
    }

    fun playSuccess() {
        if (!isSoundEnabled) return
        audioScope.launch {
            playTone(523.25, 60, 0.3f)
            playTone(659.25, 70, 0.35f)
            playTone(783.99, 100, 0.4f)
        }
    }

    fun playError() {
        if (!isSoundEnabled) return
        audioScope.launch {
            playTone(220.0, 110, 0.35f)
            playTone(196.0, 140, 0.4f)
        }
    }

    fun playWin() {
        if (!isSoundEnabled) return
        audioScope.launch {
            val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0, 1108.73)
            for (note in notes) {
                playTone(note, 80, 0.35f)
            }
        }
    }

    fun triggerHaptic(context: Context, intensity: Int = 1) {
        if (!isHapticsEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val amplitude = when (intensity) {
                        1 -> VibrationEffect.DEFAULT_AMPLITUDE / 3
                        2 -> VibrationEffect.DEFAULT_AMPLITUDE / 2
                        else -> VibrationEffect.DEFAULT_AMPLITUDE
                    }
                    vibrator.vibrate(VibrationEffect.createOneShot(25L * intensity, amplitude))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25L * intensity)
                }
            }
        } catch (_: Exception) {}
    }

    private fun playTone(freqHz: Double, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            // Apply attack & decay envelope to eliminate click/pop
            val envelope = when {
                i < numSamples * 0.1 -> i / (numSamples * 0.1)
                i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                else -> 1.0
            }
            val angle = 2.0 * Math.PI * i / (sampleRate / freqHz)
            buffer[i] = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt().toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 10)
            audioTrack.release()
        } catch (_: Exception) {}
    }
}
