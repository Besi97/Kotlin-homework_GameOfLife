package dev.besi.GameOfLife.views

import dev.besi.GameOfLife.LifeController
import javafx.event.EventTarget
import javafx.geometry.Bounds
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.ScrollPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.RowConstraints
import tornadofx.*

class LifeView : View("Life view") {
	companion object {
		private const val DEFAULT_GRID_SIZE = 16.0
		const val VIEW_MARGIN_CLEARANCE = 100.0 //required for proper downsizing
	}

	private val lifeController: LifeController by inject()

	override val root = scrollpane {
		hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
		vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
		stackpane {
			minWidthProperty().bind(this@scrollpane.widthProperty())
			minHeightProperty().bind(this@scrollpane.heightProperty())
			recycleGridPane {
				alignment = Pos.CENTER
				this@scrollpane.layoutBoundsProperty().addListener { observable, oldValue, newValue ->
					this@stackpane.prefWidth = newValue.width
					this@stackpane.prefHeight = newValue.height
					init(newValue)
				}
				/*this@stackpane.setOnScroll { event ->
					scale(1 + event.deltaY / 150)
				}*/
				this@stackpane.setOnMousePressed { event ->
					if (event.isPrimaryButtonDown) {
						setMouseAnchor(event.x, event.y)
					}
				}
				this@stackpane.setOnMouseDragged { event ->
					if (event.isPrimaryButtonDown) {
						translate(event.x, event.y)
						setMouseAnchor(event.x, event.y)
					}
				}
				lifeController.xSizeProperty.onChange { init(this@scrollpane.layoutBoundsProperty().value) }
				lifeController.ySizeProperty.onChange { init(this@scrollpane.layoutBoundsProperty().value) }
				getCell = { x, y ->
					if (lifeController.map.getOrNull(x)?.getOrNull(y) == null) {
						null
					} else {
						Cell(lifeController.map[x][y], getGridSize()) {
							setOnMouseClicked { event ->
								if (event.isStillSincePress) {
									lifeController.map[x][y].value = !lifeController.map[x][y].value
								}
							}
						}
					}
				}
			}
		}
	}

	private fun EventTarget.recycleGridPane(op: RecycleGridPane.() -> Unit): RecycleGridPane = opcr(this, RecycleGridPane(), op)

	private fun RecycleGridPane.init(bounds: Bounds) {
		clear()
		setupGridConstraints()
		draw(bounds)
	}

	private fun RecycleGridPane.setupGridConstraints() {
		val rowConstraint = RowConstraints(getGridSize())
		rowConstraint.valignment = VPos.CENTER
		setupRows(lifeController.ySizeProperty.value, rowConstraint)

		val columnConstraint = ColumnConstraints(getGridSize())
		columnConstraint.halignment = HPos.CENTER
		setupColumns(lifeController.xSizeProperty.value, columnConstraint)
	}

	private fun getGridSize(): Double = DEFAULT_GRID_SIZE

}
