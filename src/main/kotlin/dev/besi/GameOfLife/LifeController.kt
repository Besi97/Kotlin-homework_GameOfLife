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

	private fun tick() {
		val neighbors = Array(map.size) { IntArray(map[0].size) { 0 } }
		map.forEachIndexed { x, booleans ->
			booleans.forEachIndexed { y, bool ->
				if (bool.value) {
					for (i in -1 until 2) {
						for (j in -1 until 2) {
							if ((i == 0) and (j == 0)) continue
							if ((x + i < 0) or (y + j < 0) or (x + i >= neighbors.size) or (y + j >= neighbors[0].size)) continue
							neighbors[x + i][y + j]++
						}
					}
				}
			}
		}
		map.forEachIndexed { i, booleans ->
			booleans.forEachIndexed { j, bool ->
				bool.value = when {
					bool.value and s.contains(neighbors[i][j]) -> true
					!bool.value and b.contains(neighbors[i][j]) -> true
					else -> false
				}
			}
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
