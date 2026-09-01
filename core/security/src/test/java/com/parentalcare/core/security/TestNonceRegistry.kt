package com.parentalcare.core.security.pairing

import android.content.SharedPreferences

/**
 * Test implementation of NonceRegistry using in-memory SharedPreferences.
 * Used for unit tests without Android context.
 */
class TestNonceRegistry : NonceRegistry(
    object : SharedPreferences by object : android.content.SharedPreferences {
        private val store = mutableMapOf<String, Any>()

        override fun contains(key: String): Boolean = store.containsKey(key)

        override fun edit() = TestEditor()

        override fun getAll(): Map<String, *> = store

        override fun getBoolean(key: String, defValue: Boolean): Boolean = store[key] as? Boolean ?: defValue

        override fun getFloat(key: String, defValue: Float): Float = store[key] as? Float ?: defValue

        override fun getInt(key: String, defValue: Int): Int = store[key] as? Int ?: defValue

        override fun getLong(key: String, defValue: Long): Long = store[key] as? Long ?: defValue

        override fun getString(key: String, defValue: String?): String? = store[key] as? String ?: defValue

        override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}

        override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}

        inner class TestEditor : android.content.SharedPreferences.Editor {
            private val edits = mutableMapOf<String, Any>()

            override fun putBoolean(key: String, value: Boolean) = this.apply { edits[key] = value }
            override fun putFloat(key: String, value: Float) = this.apply { edits[key] = value }
            override fun putInt(key: String, value: Int) = this.apply { edits[key] = value }
            override fun putLong(key: String, value: Long) = this.apply { edits[key] = value }
            override fun putString(key: String, value: String?) = this.apply { edits[key] = value }
            override fun putStringSet(key: String, value: Set<String>?) = this.apply { edits[key] = value }
            override fun remove(key: String) = this.apply { edits.remove(key) }
            override fun clear() = this.apply { edits.clear() }
            override fun commit() = true
            override fun apply() {
                edits.forEach { (k, v) -> store[k] = v }
                edits.clear()
            }
        }
    }
) {
    // Test implementation - uses in-memory storage
}