package net.bloople.audiobooks

import android.database.Cursor

inline operator fun <reified T: Any> Cursor.get(columnName: String): T {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return when (T::class) {
        ByteArray::class -> getBlob(columnIndex) as T
        Double::class -> getDouble(columnIndex) as T
        Float::class -> getFloat(columnIndex) as T
        Int::class -> getInt(columnIndex) as T
        Long::class -> getLong(columnIndex) as T
        Short::class -> getShort(columnIndex) as T
        String::class -> getString(columnIndex) as T
        Boolean::class -> (getInt(columnIndex) == 1) as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}
