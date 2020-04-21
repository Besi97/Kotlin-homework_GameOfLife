package dev.besi.GameOfLife.views

import dev.besi.GameOfLife.LifeController
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import javafx.scene.shape.Rectangle
import tornadofx.*
import java.lang.Double.min

//TODO add recyclerview-like behaviour
class LifeView: View("Life view") {
	companion object {
		private const val DEFAULT_GRID_SIZE = 16.0
		const val VIEW_MARGIN_CLEARANCE = 100.0 //required for proper downsizing
		var xMouseAnchor = 0.0
		var yMouseAnchor = 0.0
	}

	private val lifeController: LifeController by inject()

	override val root = stackpane {
		gridpane {
			alignment = Pos.CENTER
			this@stackpane.layoutBoundsProperty().addListener { observable, oldValue, newValue ->
				this@stackpane.clip = Rectangle(newValue.minX, newValue.minY, newValue.width, newValue.height)
				draw()
			}
			this@stackpane.setOnScroll { event ->
				scale(1 + event.deltaY / 150)
			}
			this@stackpane.setOnMousePressed { event -> if(event.isPrimaryButtonDown) {
				xMouseAnchor = event.x
				yMouseAnchor = event.y
			} }
			this@stackpane.setOnMouseDragged { event -> if(event.isPrimaryButtonDown) {
				translateX -= xMouseAnchor - event.x
				translateY -= yMouseAnchor - event.y
				xMouseAnchor = event.x
				yMouseAnchor = event.y
			} }
			lifeController.xSizeProperty.onChange { draw() }
			lifeController.ySizeProperty.onChange { draw() }
		}
	}

	private fun GridPane.scale(factor: Double) {
		if(factor <= 0) return

		scaleX *= factor
		scaleY *= factor
		translateX *= factor
		translateY *= factor
	}

	private fun GridPane.draw() {
		clear()
		//println("default: $DEFAULT_GRID_SIZE\ngridSize: $gridSize\nfactor: ${DEFAULT_GRID_SIZE/(gridSize*scaleX)}")
		scale(DEFAULT_GRID_SIZE / (getGridSize() * scaleX))
		//println("scaleX: $scaleX\n")
		lifeController.map.forEachIndexed { i, list ->
			row {
				list.forEachIndexed { j, property ->
					val cell = Cell(property, getGridSize()) {
						setOnMouseClicked { event ->
							if(event.isStillSincePress) {
								lifeController.map[i][j].value = !lifeController.map[i][j].value
							}
						}
					}
					add(cell.root, i, j)
				}
			}
		}
		setupGridConstraints()
	}

	private fun GridPane.setupGridConstraints() {
		rowConstraints.clear()
		val rowConstraint = RowConstraints(getGridSize())
		rowConstraint.valignment = VPos.CENTER
		for (i in 0 until lifeController.ySizeProperty.value) {
			rowConstraints.add(rowConstraint)
		}

		columnConstraints.clear()
		val columnConstraint = ColumnConstraints(getGridSize())
		columnConstraint.halignment = HPos.CENTER
		for (i in 0 until lifeController.xSizeProperty.value) {
			columnConstraints.add(columnConstraint)
		}
	}

	private fun getGridSize(): Double = min(
			(root.width - VIEW_MARGIN_CLEARANCE) / lifeController.xSizeProperty.value,
			(root.height - VIEW_MARGIN_CLEARANCE) / lifeController.ySizeProperty.value)
/*
		init {
			root.add(group)
			root.layoutBoundsProperty().addListener { observable, oldValue, newValue ->
				draw()
				root.clip = Rectangle(newValue.minX, newValue.minY, newValue.width, newValue.height)
			}
			root.setOnScroll { event ->
				scale(1 + event.deltaY/200)
			}
			root.setOnMousePressed { event -> if(event.isPrimaryButtonDown) {
				xMouseAnchor = event.x
				yMouseAnchor = event.y
			} }
			root.setOnMouseDragged { event -> if(event.isPrimaryButtonDown) {
				group.apply {
					translateX -= xMouseAnchor - event.x
					translateY -= yMouseAnchor - event.y
				}
				xMouseAnchor = event.x
				yMouseAnchor = event.y
			} }
		}*/
}
