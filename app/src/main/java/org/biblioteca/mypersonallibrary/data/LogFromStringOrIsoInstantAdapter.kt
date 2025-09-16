package org.biblioteca.mypersonallibrary.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant

/**
 * Accepta Longs que al JSON poden venir com a número, com a string numèric
 * o com a ISO-8601 (p.ex. "2025-09-13T01:00:13.8738901").
 */
class LongFromStringOrIsoInstantAdapter :
    JsonDeserializer<Long?>, JsonSerializer<Long?> {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun deserialize(json: JsonElement?, typeOfT: Type?, ctx: JsonDeserializationContext?): Long? {
        if (json == null || json.isJsonNull) return null

        val prim = json.asJsonPrimitive
        // 1) Número pur
        if (prim.isNumber) return prim.asLong
        // 2) String -> Long o ISO-8601
        if (prim.isString) {
            val s = prim.asString.trim()
            s.toLongOrNull()?.let { return it }
            return runCatching { Instant.parse(s).toEpochMilli() }.getOrNull()
        }
        // Altres casos: null
        return null
    }

    override fun serialize(src: Long?, typeOfSrc: Type?, ctx: JsonSerializationContext?): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src)
    }
}
