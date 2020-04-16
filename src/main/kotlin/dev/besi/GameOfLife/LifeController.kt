package dev.besi.GameOfLife

import tornadofx.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

class LifeController: Controller() {
	var b: IntArray = intArrayOf(3)
	var s: IntArray = intArrayOf(2, 3)
	var xSize: Int = 20
		set(value) {
			field = value
			initMap()
		}
	var ySize: Int = 20
		set(value) {
			field = value
			initMap()
		}
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

	var map: Array<BooleanArray> = Array(xSize) {BooleanArray(ySize) {false} }

	init {
		initMap()
	}

	private fun initMap() {
		map = Array(xSize) {BooleanArray(ySize) {false} }
	}

	private fun tick() {
		val newMap = Array(xSize) {BooleanArray(ySize) {false} }
		map.forEachIndexed { x, booleans -> run{
			booleans.forEachIndexed { y, bool -> run{
				var neighbors = 0
				for (i in -1 until 2){
					for (j in -1 until 2){
						if((i==0) and (j==0)) continue
						if(map.getOrNull(x-i)?.getOrNull(y-j)?:false) neighbors++
					}
				}
				newMap[x][y] = when{
					bool and s.contains(neighbors) -> true
					!bool and b.contains(neighbors) -> true
					else -> false
				}
			} }
		} }
		map = newMap
	}

	fun invertCell(x: Int, y: Int) {
		if((x<0) or (y<0) or (x>=map.size) or (y>=map[0].size)) return
		map[x][y] = !map[x][y]
	}

	fun start() {
		lifeTimer = fixedRateTimer(null, false, 0, (DEFAULT_TIMER_PERIOD/gameSpeed).toLong()) { tick() }
	}

	fun stop() {
		lifeTimer?.cancel()
		lifeTimer = null
	}

}
