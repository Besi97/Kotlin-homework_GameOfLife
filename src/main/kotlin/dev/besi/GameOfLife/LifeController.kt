package dev.besi.GameOfLife

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

class LifeController : Controller() {
	var b: IntArray = intArrayOf(3)
	var s: IntArray = intArrayOf(2, 3)
	var xSizeProperty = SimpleIntegerProperty(150)
	var ySizeProperty = SimpleIntegerProperty(150)
	var gameSpeed = 1.0
		set(value) {
			field = value
			stop()
			start()
		}

	companion object {
		private const val DEFAULT_TIMER_PERIOD = 250
	}

	private var lifeTimer: Timer? = null

	var map = Array(xSizeProperty.value) { Array(ySizeProperty.value) { SimpleBooleanProperty(false) } }

	init {
		xSizeProperty.onChange {
			resizeMap()
		}
		ySizeProperty.onChange {
			resizeMap()
		}
	}

	private fun resizeMap() {
		val oldMap = map
		map = Array(xSizeProperty.value) { Array(ySizeProperty.value) { SimpleBooleanProperty(false) } }
		val xOffset = (oldMap.size - map.size) / 2
		val yOffset = (oldMap[0].size - map[0].size) / 2
		map.forEachIndexed { i, list ->
			list.forEachIndexed { j, property ->
				property.value = oldMap.getOrNull(xOffset + i)?.getOrNull(yOffset + j)?.value ?: false
			}
		}
	}

	fun tick() {
		//val neighbors = Array(map.size) { IntArray(map[0].size) { 0 } }
		val neighbors = mutableMapOf<Pair<Int, Int>, Int>()
		map.forEachIndexed { x, booleans ->
			booleans.forEachIndexed { y, bool ->
				if (bool.value) {
					for (i in -1 until 2) {
						for (j in -1 until 2) {
							if ((i == 0) and (j == 0))
								neighbors.merge(Pair(x, y), 0) { oldValue, value -> oldValue }
							else
								neighbors.merge(Pair(x + i, y + j), 1) { oldValue, value -> oldValue + value }
						}
					}
				}
			}
		}
		neighbors.map { entry ->
			val x = entry.key.first
			val y = entry.key.second
			if ((x < 0) or (y < 0) or (x >= map.size) or (y >= map[0].size)) return@map

			val bool = map[x][y]
			bool.value = when {
				bool.value and s.contains(entry.value) -> true
				!bool.value and b.contains(entry.value) -> true
				else -> false
			}
			//println("(${entry.key.first}, ${entry.key.second}): ${entry.value}")
		}
	}

	fun start(delay: Long = (DEFAULT_TIMER_PERIOD / gameSpeed / 2).toLong()) {
		lifeTimer = fixedRateTimer(null, true, delay, (DEFAULT_TIMER_PERIOD / gameSpeed).toLong()) { tick() }
	}

	fun stop() {
		lifeTimer?.cancel()
		lifeTimer = null
	}

}
