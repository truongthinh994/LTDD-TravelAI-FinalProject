package com.travelai.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_prefs"
)

private const val DEFAULT_FONT_SCALE = 1.0f
private const val MIN_FONT_SCALE = 0.85f
private const val MAX_FONT_SCALE = 1.30f

enum class ThemeMode {
    SystemDefault,
    Light,
    Dark
}

enum class AvatarStyle {
    Aurora,
    Ocean,
    Sunset,
    Mint
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SystemDefault,
    val displayName: String = "",
    val fontScale: Float = DEFAULT_FONT_SCALE,
    val avatarStyle: AvatarStyle = AvatarStyle.Aurora
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.userPreferencesDataStore

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            UserPreferences(
                themeMode = runCatching {
                    ThemeMode.valueOf(preferences[KEY_THEME] ?: ThemeMode.SystemDefault.name)
                }.getOrDefault(ThemeMode.SystemDefault),
                displayName = preferences[KEY_NAME].orEmpty(),
                fontScale = (preferences[KEY_FONT_SCALE] ?: DEFAULT_FONT_SCALE)
                    .coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE),
                avatarStyle = runCatching {
                    AvatarStyle.valueOf(preferences[KEY_AVATAR_STYLE] ?: AvatarStyle.Aurora.name)
                }.getOrDefault(AvatarStyle.Aurora)
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = mode.name
        }
    }

    suspend fun setDisplayName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_NAME] = name.trim()
        }
    }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_FONT_SCALE] = scale.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)
        }
    }

    suspend fun setAvatarStyle(style: AvatarStyle) {
        dataStore.edit { preferences ->
            preferences[KEY_AVATAR_STYLE] = style.name
        }
    }

    suspend fun setProfile(displayName: String, avatarStyle: AvatarStyle) {
        dataStore.edit { preferences ->
            preferences[KEY_NAME] = displayName.trim()
            preferences[KEY_AVATAR_STYLE] = avatarStyle.name
        }
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_NAME = stringPreferencesKey("display_name")
        val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        val KEY_AVATAR_STYLE = stringPreferencesKey("avatar_style")
    }
}
