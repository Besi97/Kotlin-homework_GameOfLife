package dev.besi.GameOfLife

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

class LifeController: Controller() {
	var b: IntArray = intArrayOf(3)
	var s: IntArray = intArrayOf(2, 3)
	var xSizeProperty = SimpleIntegerProperty(5)
	var ySizeProperty = SimpleIntegerProperty(5)
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

	var map = MutableList(xSizeProperty.value) { MutableList(ySizeProperty.value) { SimpleBooleanProperty(false) } }

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
		map = MutableList(xSizeProperty.value) { MutableList(ySizeProperty.value) { SimpleBooleanProperty(false) } }
		val xOffset = (oldMap.size - map.size) / 2
		val yOffset = (oldMap[0].size - map[0].size) / 2
		map.forEachIndexed { i, list ->
			list.forEachIndexed { j, property ->
				property.value = oldMap.getOrNull(xOffset + i)?.getOrNull(yOffset + j)?.value ?: false
				println("(${xOffset + i}, ${yOffset + j}): ${property.value}")
			}
		}
	}

	private fun tick() {
		val oldMap = map.copy()
		oldMap.forEachIndexed { x, booleans -> run{
			booleans.forEachIndexed { y, bool -> run{
				var neighbors = 0
				for (i in -1 until 2){
					for (j in -1 until 2){
						if((i==0) and (j==0)) continue
						if(oldMap.getOrNull(x-i)?.getOrNull(y-j)?.value == true)
							neighbors++
					}
				}
				map[x][y].value = when{
					bool.value and s.contains(neighbors) -> true
					!bool.value and b.contains(neighbors) -> true
					else -> false
				}
			} }
		} }
	}

	private fun List<List<BooleanProperty>>.copy(): List<List<BooleanProperty>> {
		return this.map { list ->
			list.map { property ->
				SimpleBooleanProperty(property.value)
			}
		}
	}

	fun start(delay: Long = (DEFAULT_TIMER_PERIOD/gameSpeed/2).toLong()) {
		lifeTimer = fixedRateTimer(null, true, delay, (DEFAULT_TIMER_PERIOD/gameSpeed).toLong()) { tick() }
	}

	fun stop() {
		lifeTimer?.cancel()
		lifeTimer = null
	}

}
