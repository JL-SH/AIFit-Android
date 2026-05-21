package com.jlsh.aifit.core.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

private val APP_LOCALE: Locale = Locale.forLanguageTag("es-ES")

fun Context.wrapWithAppLocale(): Context {
    Locale.setDefault(APP_LOCALE)
    val config = resources.configuration
    config.setLocale(APP_LOCALE)
    return createConfigurationContext(config)
}

fun applyAppLocale() {
    Locale.setDefault(APP_LOCALE)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es-ES"))
}
