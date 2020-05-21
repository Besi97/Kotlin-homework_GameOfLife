package dev.besi.GameOfLife.views

import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.layout.Pane
import tornadofx.*

/**
 * This view is based on RecyclerView in Android. The aim here is to only have those cells loaded, which are displayed
 * (and some more, for smooth experience)
 */
class RecycleGridPane : Pane() {

	var getCell: (x: Int, y: Int) -> Cell? = { x, y -> null }

	var gridSize = 16.0
	var columnCount = 0
	var rowCount = 0

	private var xMouseAnchor = 0.0
	private var yMouseAnchor = 0.0

	private var xCenterPixel = 0.0
	private var yCenterPixel = 0.0
	private var xCenterCell = 0
	private var yCenterCell = 0

	private var xStartTheoretical: Int = 0
	private var xEndTheoretical: Int = 0
	private var yStartTheoretical: Int = 0
	private var yEndTheoretical: Int = 0

	private var xStartIndex: Int = 0
	private var xEndIndex: Int = 0
	private var yStartIndex: Int = 0
	private var yEndIndex: Int = 0

	private val cells = mutableListOf<CellHolder>()

	private fun add(node: Node, x: Int, y: Int) {
		node.layoutX = (x - xCenterCell) * gridSize + xCenterPixel
		node.layoutY = (y - yCenterCell) * gridSize + yCenterPixel
		add(node)
	}

	fun draw(bounds: Bounds) {
		cells.clear()
		xCenterCell = columnCount / 2
		yCenterCell = rowCount / 2
		xCenterPixel = bounds.width / 2
		yCenterPixel = bounds.height / 2
		calculateTheoreticalBonds()

		xStartIndex = xStartTheoretical.coerceAtLeast(0)
		xEndIndex = xEndTheoretical.coerceAtMost(columnCount)
		yStartIndex = yStartTheoretical.coerceAtLeast(0)
		yEndIndex = yEndTheoretical.coerceAtMost(rowCount)

		for (i in xStartIndex..xEndIndex) {
			for (j in yStartIndex..yEndIndex) {
				getCell(i, j)?.let {
					val holder = CellHolder(it, i, j)
					add(it.root, i, j)
					cells.add(holder)
				}
			}
		}
	}

	private fun calculateTheoreticalBonds() {
		val translateXCell = (translateX / scaleX) / gridSize
		val translateYCell = (translateY / scaleY) / gridSize
		val xCurrentCenterCellIndex = xCenterCell - translateXCell
		val yCurrentCenterCellIndex = yCenterCell - translateYCell
		val cellsToLeftFromCenter = xCenterPixel / (gridSize * scaleX)
		val cellsToTopFromCenter = yCenterPixel / (gridSize * scaleY)

		//+2 cells "clearance", to always see cells on the edge of the pane as well
		xStartTheoretical = (xCurrentCenterCellIndex - cellsToLeftFromCenter - 2).toInt()
		xEndTheoretical = (xCurrentCenterCellIndex + cellsToLeftFromCenter + 2).toInt()
		yStartTheoretical = (yCurrentCenterCellIndex - cellsToTopFromCenter - 2).toInt()
		yEndTheoretical = (yCurrentCenterCellIndex + cellsToTopFromCenter + 2).toInt()
	}

	private fun addCell(x: Int, y: Int) {
		getCell(x, y)?.let { cell ->
			CellHolder(cell, x, y).also { holder ->
				cells.add(holder)
			}
			add(cell.root, x, y)
		}
	}

	private fun removeCells(predicate: (CellHolder) -> Boolean) {
		cells.filter { predicate(it) }.forEach {
			children.remove(it.cell.root)
			cells.remove(it)
		}

	}

	private fun updateGrid() {
		val xStart = xStartIndex - xStartTheoretical
		val xEnd = xEndTheoretical - xEndIndex
		val yStart = yStartIndex - yStartTheoretical
		val yEnd = yEndTheoretical - yEndIndex
		if ((xStart == 0) and (xEnd == 0) and (yStart == 0) and (yEnd == 0)) return

		if (xStart > 0) {
			for (i in 0 until xStart) {
				for (j in yStartIndex..yEndIndex) {
					addCell(xStartIndex, j)
				}
				xStartIndex--
			}
		} else {
			for (i in 0 until -xStart) {
				removeCells { it.x == xStartIndex }
				xStartIndex++
			}
		}

		if (xEnd > 0) {
			for (i in 0 until xEnd) {
				for (j in yStartIndex..yEndIndex) {
					addCell(xEndIndex, j)
				}
				xEndIndex++
			}
		} else {
			for (i in 0 until -xEnd) {
				removeCells { it.x == xEndIndex }
				xEndIndex--
			}
		}

		if (yStart > 0) {
			for (i in 0 until yStart) {
				for (j in xStartIndex..xEndIndex) {
					addCell(j, yStartIndex)
				}
				yStartIndex--
			}
		} else {
			for (i in 0 until -yStart) {
				removeCells { it.y == yStartIndex }
				yStartIndex++
			}
		}

		if (yEnd > 0) {
			for (i in 0 until yEnd) {
				for (j in xStartIndex..xEndIndex) {
					addCell(j, yEndIndex)
				}
				yEndIndex++
			}
		} else {
			for (i in 0 until -yEnd) {
				removeCells { it.y == yEndIndex }
				yEndIndex--
			}
		}
	}

	fun translate(xNew: Double, yNew: Double) {
		translateX += xNew - xMouseAnchor
		translateY += yNew - yMouseAnchor

		calculateTheoreticalBonds()
		updateGrid()
	}

	fun setMouseAnchor(x: Double, y: Double) {
		xMouseAnchor = x
		yMouseAnchor = y
	}

	fun scale(factor: Double) {
		if (factor <= 0) return

		scaleX *= factor
		scaleY *= factor
		translateX *= factor
		translateY *= factor

		calculateTheoreticalBonds()
		updateGrid()
	}

	class CellHolder(
			var cell: Cell,
			var x: Int,
			var y: Int
	)

}

