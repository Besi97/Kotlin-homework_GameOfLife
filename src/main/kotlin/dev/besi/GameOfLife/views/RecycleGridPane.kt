package dev.besi.GameOfLife.views

import javafx.geometry.Bounds
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

class RecycleGridPane : GridPane() {

	var getCell: (x: Int, y: Int) -> Cell? = { x, y -> null }

	private var xMouseAnchor = 0.0
	private var yMouseAnchor = 0.0

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
			val prefWidth = columnConstraints[0].prefWidth
			if (field.absoluteValue >= prefWidth) {
				translateCells((field / prefWidth).toInt(), 0)
				field %= prefWidth
			}
		}
	private var yRecentTranslate = 0.0
		set(value) {
			field = value
			val prefHeight = rowConstraints[0].prefHeight
			if (field.absoluteValue >= prefHeight) {
				translateCells(0, (field / prefHeight).toInt())
				field %= prefHeight
			}
		}

	private val cells = mutableListOf<CellHolder>()

	fun draw(bounds: Bounds) {
		cells.clear()
		val xOriginalCenterCellIndex = columnConstraints.size / 2
		val yOriginalCenterCellIndex = rowConstraints.size / 2
		val translateXCell = translateX / columnConstraints[0].prefWidth
		val translateYCell = translateY / rowConstraints[0].prefHeight
		val xCurrentCenterCellIndex = xOriginalCenterCellIndex - translateXCell
		val yCurrentCenterCellIndex = yOriginalCenterCellIndex - translateYCell
		val cellsToLeftFromCenter = bounds.width / 2 / (columnConstraints[0].prefWidth * scaleX)
		val cellsToTopFromCenter = bounds.height / 2 / (rowConstraints[0].prefHeight * scaleY)

		//+2 cells "clearance", to always see cells on the edge of the pane as well
		xStartTheoretical = (xCurrentCenterCellIndex - cellsToLeftFromCenter - 2).toInt()
		xEndTheoretical = (xCurrentCenterCellIndex + cellsToLeftFromCenter + 2).toInt()
		yStartTheoretical = (yCurrentCenterCellIndex - cellsToTopFromCenter - 2).toInt()
		yEndTheoretical = (yCurrentCenterCellIndex + cellsToTopFromCenter + 2).toInt()

		xStartIndex = xStartTheoretical.coerceAtLeast(0)
		xEndIndex = xEndTheoretical.coerceAtMost(columnConstraints.size)
		yStartIndex = yStartTheoretical.coerceAtLeast(0)
		yEndIndex = yEndTheoretical.coerceAtMost(rowConstraints.size)

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
		val time = measureTimeMillis {
			updateGrid()
		}
		println("gridupdate: $time ms")
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
				add(cell.root, x, y)
				holder.isBound = true
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

	fun setupRows(count: Int, constraints: RowConstraints) {
		rowConstraints.clear()
		for (i in 0 until count) {
			rowConstraints.add(constraints)
		}
	}

	fun setupColumns(count: Int, constraints: ColumnConstraints) {
		columnConstraints.clear()
		for (i in 0 until count) {
			columnConstraints.add(constraints)
		}
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

