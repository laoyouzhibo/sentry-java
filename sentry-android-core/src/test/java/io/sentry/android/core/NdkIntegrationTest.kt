package io.sentry.android.core

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.sentry.IHub
import io.sentry.ILogger
import io.sentry.SentryLevel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NdkIntegrationTest {

    private class Fixture {
        val hub = mock<IHub>()
        val logger = mock<ILogger>()

        fun getSut(clazz: Class<*>? = SentryNdk::class.java): NdkIntegration {
            return NdkIntegration(clazz)
        }
    }

    private val fixture = Fixture()

    @Test
    fun `NdkIntegration calls init method`() {
        val integration = fixture.getSut()

        val options = getOptions()

        integration.register(fixture.hub, options)

        verify(fixture.logger, never()).log(eq(SentryLevel.ERROR), any<String>(), any())
        assertTrue(options.isEnableNdk)
        assertTrue(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration calls close method`() {
        val integration = fixture.getSut()

        val options = getOptions()

        integration.register(fixture.hub, options)

        assertTrue(options.isEnableNdk)
        assertTrue(options.isEnableScopeSync)

        integration.close()

        verify(fixture.logger, never()).log(eq(SentryLevel.ERROR), any<String>(), any())
        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration won't init if ndk integration is disabled`() {
        val integration = fixture.getSut()

        val options = getOptions(enableNdk = false)

        integration.register(fixture.hub, options)

        verify(fixture.logger, never()).log(eq(SentryLevel.ERROR), any<String>(), any())

        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration won't init if SentryNdk class is not available`() {
        val integration = fixture.getSut(null)

        val options = getOptions()

        integration.register(fixture.hub, options)

        verify(fixture.logger, never()).log(eq(SentryLevel.ERROR), any<String>(), any())

        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration won't init if init method is not available`() {
        val integration = fixture.getSut(SentryNdkNoInit::class.java)

        val options = getOptions()

        integration.register(fixture.hub, options)

        verify(fixture.logger).log(eq(SentryLevel.ERROR), any<String>(), any())

        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration won't close if close method is not available`() {
        val integration = fixture.getSut(SentryNdkNoClose::class.java)

        val options = getOptions()

        integration.register(fixture.hub, options)

        assertTrue(options.isEnableNdk)
        assertTrue(options.isEnableScopeSync)

        integration.close()

        verify(fixture.logger).log(eq(SentryLevel.ERROR), any<String>(), any())
        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration won't init if init throws`() {
        val integration = fixture.getSut(SentryNdkThrows::class.java)

        val options = getOptions()

        integration.register(fixture.hub, options)

        verify(fixture.logger).log(eq(SentryLevel.ERROR), any<String>(), any())

        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration won't init if cache dir is null`() {
        val integration = fixture.getSut()

        val options = getOptions(cacheDir = null)

        integration.register(fixture.hub, options)

        verify(fixture.logger).log(eq(SentryLevel.ERROR), any())

        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    @Test
    fun `NdkIntegration won't init if cache dir is empty`() {
        val integration = fixture.getSut()

        val options = getOptions(cacheDir = "")

        integration.register(fixture.hub, options)

        verify(fixture.logger).log(eq(SentryLevel.ERROR), any())

        assertFalse(options.isEnableNdk)
        assertFalse(options.isEnableScopeSync)
    }

    private fun getOptions(enableNdk: Boolean = true, cacheDir: String? = "abc"): SentryAndroidOptions {
        return SentryAndroidOptions().apply {
            setLogger(fixture.logger)
            isDebug = true
            isEnableNdk = enableNdk
            cacheDirPath = cacheDir
        }
    }

    private class SentryNdkNoInit

    private class SentryNdkThrows {
        companion object {
            @JvmStatic
            fun init(options: SentryAndroidOptions) {
                throw RuntimeException("damn")
            }
        }
    }

    private class SentryNdkNoClose {
        companion object {
            @JvmStatic
            fun init(options: SentryAndroidOptions) {
            }
        }
    }
}
