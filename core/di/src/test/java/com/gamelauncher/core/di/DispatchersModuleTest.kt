package com.gamelauncher.core.di

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class DispatchersModuleTest {

    @Test
    fun testIoDispatcherProvider() {
        val dispatcher = DispatchersModule.provideIoDispatcher()
        assertEquals(Dispatchers.IO, dispatcher)
    }

    @Test
    fun testDefaultDispatcherProvider() {
        val dispatcher = DispatchersModule.provideDefaultDispatcher()
        assertEquals(Dispatchers.Default, dispatcher)
    }

    @Test
    fun testMainDispatcherProvider() {
        val dispatcher = DispatchersModule.provideMainDispatcher()
        assertEquals(Dispatchers.Main, dispatcher)
    }
}
