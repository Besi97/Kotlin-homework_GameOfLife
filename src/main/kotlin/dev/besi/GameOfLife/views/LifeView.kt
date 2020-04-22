package dev.besi.GameOfLife.views

import dev.besi.GameOfLife.LifeController
import javafx.event.EventTarget
import javafx.geometry.Bounds
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import tornadofx.*

class LifeView : View("Life view") {
	companion object {
		private const val DEFAULT_GRID_SIZE = 16.0
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
				//border = Border(BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii(0.0), BorderWidths(2.0)))
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
		gridSize = getGridSize()
		rowCount = lifeController.ySizeProperty.value
		columnCount = lifeController.xSizeProperty.value
		draw(bounds)
	}

	private fun getGridSize(): Double = DEFAULT_GRID_SIZE

}
