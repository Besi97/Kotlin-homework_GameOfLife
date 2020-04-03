package dev.besi.GameOfLife

/**
 * @param b neighbours requires for a cell to be born
 * @param s neighbours required for a cell to survive
 * @param xSize x size of the map
 * @param ySize y size of the map
 */
class LifeConfig (
		val b: IntArray = intArrayOf(3),
		val s: IntArray = intArrayOf(2, 3),
		val xSize: Int = 100,
		val ySize: Int = 100
)
