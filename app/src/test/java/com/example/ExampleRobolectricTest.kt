package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.VoiceBiometricsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS AI Assistant", appName)
  }

  @Test
  fun `voice biometric engine acoustic feature extraction and enrollment`() {
    val engine = VoiceBiometricsEngine()
    val features = engine.extractAcousticFeatures("Jarvis, authorize protocol Mark 85")
    assertTrue(features.fundamentalFrequencyHz > 100f)
    assertTrue(features.harmonicsToNoiseRatio > 0.8f)

    val profile = engine.enrollNewVoiceprint(
      userName = "Sir",
      samples = listOf("Jarvis, authorize protocol Mark 85")
    )
    assertNotNull(profile.voiceprintHash)
    assertTrue(profile.isEnrolled)

    val verification = engine.verifyVoiceprint(profile, "Jarvis, authorize protocol Mark 85")
    assertTrue(verification.success)
    assertTrue(verification.confidence >= 0.85f)
  }
}
