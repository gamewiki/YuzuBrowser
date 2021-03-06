/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.gesture.multiFinger.data

import android.content.Context
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.action.Action
import jp.hazuki.yuzubrowser.utils.ArrayUtils
import jp.hazuki.yuzubrowser.utils.JsonUtils
import java.io.IOException
import java.util.*

class MultiFingerGestureManager(context: Context) {
    private val jsonFile = context.getFileStreamPath(FILENAME)
    private val items: MutableList<MultiFingerGestureItem> = ArrayList()

    val gestureItems: List<MultiFingerGestureItem>
        get() = items

    init {
        load()
    }

    fun add(item: MultiFingerGestureItem) {
        items.add(item)
        save()
    }

    fun add(index: Int, item: MultiFingerGestureItem) {
        items.add(index, item)
        save()
    }

    operator fun set(index: Int, item: MultiFingerGestureItem) {
        items[index] = item
        save()
    }

    fun remove(index: Int): MultiFingerGestureItem {
        val item = items.removeAt(index)
        save()
        return item
    }

    fun remove(item: MultiFingerGestureItem) {
        if (items.remove(item))
            save()
    }

    fun move(from: Int, to: Int) {
        ArrayUtils.move(items, from, to)
        save()
    }

    fun indexOf(item: MultiFingerGestureItem): Int {
        return items.indexOf(item)
    }

    private fun load() {
        items.clear()
        if (jsonFile == null || !jsonFile.exists() || jsonFile.isDirectory) return
        try {
            JsonUtils.getFactory().createParser(jsonFile).use { parser ->
                if (parser.nextToken() == JsonToken.START_ARRAY) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        if (parser.currentToken == JsonToken.START_OBJECT) {
                            val item = MultiFingerGestureItem()
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                val name = parser.currentName
                                if (name != null) {
                                    when (name) {
                                        JSON_FINGERS -> {
                                            parser.nextToken()
                                            item.fingers = parser.intValue
                                        }
                                        JSON_TRACES -> if (parser.nextToken() == JsonToken.START_ARRAY) {
                                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                                item.addTrace(parser.intValue)
                                            }
                                        } else {
                                            parser.skipChildren()
                                        }
                                        JSON_ACTION -> {
                                            val action = Action()
                                            action.loadAction(parser)
                                            item.action = action
                                        }
                                        else -> parser.skipChildren()
                                    }
                                }
                            }
                            items.add(item)
                        } else {
                            parser.skipChildren()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun save() {
        try {
            JsonUtils.getFactory().createGenerator(jsonFile, JsonEncoding.UTF8).use { generator ->
                generator.writeStartArray()
                for (item in items) {
                    generator.writeStartObject()

                    generator.writeNumberField(JSON_FINGERS, item.fingers)

                    // finger actions
                    generator.writeFieldName(JSON_TRACES)
                    generator.writeStartArray()
                    for (i in item.traces) {
                        generator.writeNumber(i)
                    }
                    generator.writeEndArray()

                    // browser action
                    generator.writeFieldName(JSON_ACTION)
                    item.action.writeAction(generator)

                    generator.writeEndObject()
                }
                generator.writeEndArray()
                generator.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private const val FILENAME = "multiFingerGes_1.dat"

        private const val JSON_FINGERS = "0"
        private const val JSON_TRACES = "1"
        private const val JSON_ACTION = "2"
    }
}
