package dev.besi.GameOfLife

import org.junit.Test
import kotlin.system.measureTimeMillis

class LifeControllerTest {
	@Test
	fun tickSpeedTest() {
		val controller = LifeController()
		//setMapSize(controller, 1000)
		addAcornToMap(controller)
		val times = mutableListOf<Long>()
		for (j in 0 until 10) {
			val time = measureTimeMillis {
				for (i in 0 until 500) {
					controller.tick()
				}
			}
			times.add(time)
			println("500 ticks took $time ms")
		}
		println("\n500 ticks on average took ${times.average()} ms")
	}

	private fun setMapSize(controller: LifeController, size: Int) {
		controller.xSizeProperty.value = size
		controller.ySizeProperty.value = size
	}

	private fun addAcornToMap(controller: LifeController){
		setMapSize(controller, 1000)
		controller.map[500][500].value = true
		controller.map[498][499].value = true
		controller.map[498][501].value = true
		controller.map[497][501].value = true
		controller.map[501][501].value = true
		controller.map[502][501].value = true
		controller.map[503][501].value = true
	}
}
