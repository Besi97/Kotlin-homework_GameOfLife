package dev.besi.GameOfLife.views

import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.layout.Pane
import tornadofx.*
import kotlin.math.absoluteValue

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

	private var xRecentTranslate = 0.0
		set(value) {
			field = value
			if (field.absoluteValue >= gridSize) {
				translateCells((field / gridSize).toInt(), 0)
				field %= gridSize
			}
		}
	private var yRecentTranslate = 0.0
		set(value) {
			field = value
			if (field.absoluteValue >= gridSize) {
				translateCells(0, (field / gridSize).toInt())
				field %= gridSize
			}
		}

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
		val translateXCell = translateX / gridSize
		val translateYCell = translateY / gridSize
		val xCurrentCenterCellIndex = xCenterCell - translateXCell
		val yCurrentCenterCellIndex = yCenterCell - translateYCell
		xCenterPixel = bounds.width / 2
		yCenterPixel = bounds.height / 2
		val cellsToLeftFromCenter = xCenterPixel / (gridSize * scaleX)
		val cellsToTopFromCenter = yCenterPixel / (gridSize * scaleY)

		//+2 cells "clearance", to always see cells on the edge of the pane as well
		xStartTheoretical = (xCurrentCenterCellIndex - cellsToLeftFromCenter - 2).toInt()
		xEndTheoretical = (xCurrentCenterCellIndex + cellsToLeftFromCenter + 2).toInt()
		yStartTheoretical = (yCurrentCenterCellIndex - cellsToTopFromCenter - 2).toInt()
		yEndTheoretical = (yCurrentCenterCellIndex + cellsToTopFromCenter + 2).toInt()

		xStartIndex = xStartTheoretical.coerceAtLeast(0)
		xEndIndex = xEndTheoretical.coerceAtMost(columnCount)
		yStartIndex = yStartTheoretical.coerceAtLeast(0)
		yEndIndex = yEndTheoretical.coerceAtMost(rowCount)

		for (i in xStartIndex until xEndIndex + 1) {
			for (j in yStartIndex until yEndIndex + 1) {
				getCell(i, j)?.let {
					val holder = CellHolder(it, i, j) { isBound = true }
					add(it.root, i, j)
					cells.add(holder)
				}
			}
		}
	}

	private fun translateCells(x: Int, y: Int) {
		xStartTheoretical -= x
		xEndTheoretical -= x
		yStartTheoretical -= y
		yEndTheoretical -= y
		updateGrid()
	}

	private fun updateGrid() {
		val x = xStartIndex - xStartTheoretical
		val y = yStartIndex - yStartTheoretical
		fun removeCells(predicate: (CellHolder) -> Boolean) {
			cells.filter { predicate(it) }.forEach {
				it.isBound = false
				children.remove(it.cell.root)
			}
		}

		fun addCell(x: Int, y: Int) {
			getCell(x, y)?.let { cell ->
				val holder = cells.firstOrNull { !it.isBound }?.also { holder ->
					holder.cell = cell
					holder.x = x
					holder.y = y
				} ?: CellHolder(cell, x, y).also { holder ->
					cells.add(holder)
				}
				holder.isBound = true
				add(cell.root, x, y)
			}
		}

		if (x != 0) {
			if (x > 0) {
				if (xEndTheoretical <= xEndIndex) {
					for (i in 0 until x) {
						removeCells { it.x == xEndIndex }
						xEndIndex--
					}
				}
				if (xStartTheoretical <= xStartIndex) {
					for (i in 0 until x) {
						for (j in yStartIndex until yEndIndex + 1) {
							addCell(xStartIndex, j)
						}
						xStartIndex--
					}
				}
			} else {
				if (xStartTheoretical >= xStartIndex) {
					for (i in 0 until -x) {
						removeCells { it.x == xStartIndex }
						xStartIndex++
					}
				}
				if (xEndTheoretical >= xEndIndex) {
					for (i in 0 until -x) {
						for (j in yStartIndex until yEndIndex + 1) {
							addCell(xEndIndex, j)
						}
						xEndIndex++
					}
				}
			}
		}
		if (y != 0) {
			if (y > 0) {
				if (yEndTheoretical <= yEndIndex) {
					for (i in 0 until y) {
						removeCells { it.y == yEndIndex }
						yEndIndex--
					}
				}
				if (yStartTheoretical <= yStartIndex) {
					for (j in 0 until y) {
						for (i in xStartIndex until xEndIndex + 1) {
							addCell(i, yStartIndex)
						}
						yStartIndex--
					}
				}
			} else {
				if (yStartTheoretical >= yStartIndex) {
					for (i in 0 until -y) {
						removeCells { it.y == yStartIndex }
						yStartIndex++
					}
				}
				if (yEndTheoretical >= yEndIndex) {
					for (j in 0 until -y) {
						for (i in xStartIndex until xEndIndex + 1) {
							addCell(i, yEndIndex)
						}
						yEndIndex++
					}
				}
			}
		}
	}

	fun translate(xNew: Double, yNew: Double) {
		val xDiff = xNew - xMouseAnchor
		val yDiff = yNew - yMouseAnchor
		translateX += xDiff
		translateY += yDiff
		xRecentTranslate += xDiff
		yRecentTranslate += yDiff
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
	}

	class CellHolder(
			var cell: Cell,
			var x: Int,
			var y: Int,
			op: CellHolder.() -> Unit = {}
	) {
		var isBound = false

		init {
			op()
		}
	}

}

