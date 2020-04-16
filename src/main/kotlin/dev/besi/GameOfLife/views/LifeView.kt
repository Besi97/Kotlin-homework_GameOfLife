package dev.besi.GameOfLife.views

import dev.besi.GameOfLife.LifeController
import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*
import kotlin.concurrent.fixedRateTimer

//TODO optimize drawing
class LifeView : View("Life View") {
	private val lifeController: LifeController by inject()

	companion object {
		const val DEFAULT_GRID_SIZE = 16
	}

	private var timer = fixedRateTimer(null, false, 1000, 20) {
		runLater { reDraw() }
	}

	private var zoom = 2.0
		set(value) {
			field = if(value > 5) 5.0 else value
		}
	/** X coord (on the map, not the view) of the cell in the center of the view*/
	private var xCenter = 0.0
		set(value) {
			val minXCenter = (getViewWidth() / 2) / getGridSize()
			val maxXCenter = lifeController.xSize - minXCenter

			field = when {
				value < minXCenter -> minXCenter
				value > maxXCenter -> maxXCenter
				else -> value
			}
		}
	/** Y coord (on the map, not the view) of the cell in the center of the view */
	private var yCenter = 0.0
		set(value) {
			val minYCenter = (getViewHeight() / 2) / getGridSize()
			val maxYCenter = lifeController.ySize - minYCenter

			field = when {
				value < minYCenter -> minYCenter
				value > maxYCenter -> maxYCenter
				else -> value
			}
		}

	private var mouseXDragAnchor: Double = 0.0
	private var mouseYDragAnchor: Double = 0.0

	override val root: StackPane = stackpane {
		this.setOnZoom { event -> onZoom(event.zoomFactor)}
		this.setOnScroll { event -> onZoom(event.deltaY)}
		this.setOnMousePressed { event -> if(event.isPrimaryButtonDown) {
			mouseXDragAnchor = event.x
			mouseYDragAnchor = event.y
		} }
		this.setOnMouseDragged { event -> if(event.isPrimaryButtonDown) {
			xCenter += (mouseXDragAnchor - event.x) / getGridSize()
			yCenter += (mouseYDragAnchor - event.y) / getGridSize()
			mouseXDragAnchor = event.x
			mouseYDragAnchor = event.y
		} }
		this.setOnMouseClicked { event -> if(event.isStillSincePress) {
			lifeController.invertCell(
					((event.x - (getMinXRender() + getViewWidth()/2)) / getGridSize() + xCenter).toInt(),
					((event.y - (getMinYRender() + getViewHeight()/2)) / getGridSize() + yCenter).toInt()
			)
		} }
	}

	init {
		xCenter = lifeController.xSize.toDouble() / 2
		yCenter = lifeController.ySize.toDouble() / 2
		primaryStage.setOnCloseRequest {
			timer.cancel()
			lifeController.stop()
			Platform.exit()
		}
	}

	private fun reDraw() {
		//revise center coordinates
		xCenter = xCenter
		yCenter = yCenter

		root.clear()
		drawMap()
		drawBorder()
	}

	private fun onZoom(zoomFactor: Double) {
		zoom *= (1 + zoomFactor/100)
	}

	/*
	As a first step, it calculates the coordinates of the upper left corner of the center cell and draws it.
	After that, it draws all other cells around that cell, from inside out.
	 */
	private fun drawMap() {
		//get the coordinates of the center of the view
		val xViewCenter = getViewWidth() / 2 + getMinXRender()
		val yViewCenter = getViewHeight() / 2 +  getMinYRender()
		//get the coords of the upper left corner of the first (central) cell
		val xCenterCell = xViewCenter - ((xCenter - xCenter.toInt()) * getGridSize())
		val yCenterCell = yViewCenter - ((yCenter - yCenter.toInt()) * getGridSize())
		drawCell(xCenterCell, yCenterCell, lifeController.map[xCenter.toInt()][yCenter.toInt()])

		var iterations = 1
		var x = xCenterCell - getGridSize()
		var y = yCenterCell - getGridSize()
		do {
			//draw the upper row
			if(y <= getMinYRender() - getGridSize()){
				//skip to the end of row, if we are out of bounds
				//-getGridSize() to include partially seen cells
				x += iterations*2*getGridSize()
			}else for (i in 0 until iterations * 2) {
				x += getGridSize()
				drawCell(x, y,
						lifeController.map
								.getOrNull(xCenter.toInt() - iterations + i + 1)
								?.getOrNull(yCenter.toInt() - iterations)
								?: false)
			}

			//draw the right column
			if(x >= getMaxXRender()){
				//skip to the bottom if we are out of bounds
				y += iterations*2*getGridSize()
			}else for (i in 0 until iterations * 2) {
				y += getGridSize()
				if(yCenter.toInt() - iterations + i + 1 < lifeController.ySize)
					drawCell(x, y,
							lifeController.map
									.getOrNull(xCenter.toInt() + iterations)
									?.getOrNull(yCenter.toInt() - iterations + i + 1)
									?: false)
			}

			//draw the lower row
			if(y >= getMaxYRender()){
				//skip to the start if we are out of bounds
				x -= iterations*2*getGridSize()
			}else for (i in 0 until iterations * 2) {
				x -= getGridSize()
				if(xCenter.toInt() + iterations - i - 1 >= 0)
					drawCell(x, y,
							lifeController.map
									.getOrNull(xCenter.toInt() + iterations - i - 1)
									?.getOrNull(yCenter.toInt() + iterations)
									?: false)
			}

			//draw the left column
			if(x <= getMinXRender() - getGridSize()){
				//skip to the top if we are out of bounds
				//-getGridSize() to include partially seen cells
				y -= iterations*2*getGridSize()
			}else for (i in 0 until iterations * 2) {
				y -= getGridSize()
				if(yCenter.toInt() + iterations - i - 1 >= 0)
					drawCell(x, y,
							lifeController.map
									.getOrNull(xCenter.toInt() - iterations)
									?.getOrNull(yCenter.toInt() + iterations - i - 1)
									?: false)
			}

			//set coordinates for the next iteration
			iterations++
			x = xCenterCell - iterations * getGridSize()
			y = yCenterCell - iterations * getGridSize()
			//break loop if both coords are clearly out of bounds
			// - getGridSize() is added to include partially seen cells
			// - 2*getGridSize() for x, without the 2 multiplier, the rightmost column may fail to draw on drag
		}while (x > getMinXRender() - 2*getGridSize() || y > getMinYRender() - getGridSize())
	}

	private fun drawCell(x: Double, y: Double, isAlive: Boolean) {
		if(!isCellVisible(x, y)) return

	    root.add(root.rectangle {
			val correctedX = if(x < getMinXRender()) getMinXRender() else x
			val correctedY = if(y < getMinYRender()) getMinYRender() else y
			val correctedWidth = if(correctedX + getGridSize() - (correctedX - x) > getMaxXRender())
				getMaxXRender() - correctedX else getGridSize() - (correctedX - x)
			val correctedHeight = if(correctedY + getGridSize() - (correctedY - y) > getMaxYRender())
				getMaxYRender() - correctedY else getGridSize() - (correctedY - y)

			this.x = correctedX
			this.y = correctedY
			width = correctedWidth
			height = correctedHeight
			fill = if(isAlive) Color.BLACK else null
			stroke = if(zoom > 0.5) Color.LIGHTGRAY else null
			isManaged = false
		})
	}

	private fun isCellVisible(x: Double, y: Double): Boolean{
		if(
				x < getMinXRender()-getGridSize() ||
				y < getMinYRender()-getGridSize() ||
				x > getMaxXRender() ||
				y > getMaxYRender())
			return false
		return true
	}

	private fun drawBorder() {
		root.add(root.polygon(
				0, 				0,
				0, 				getViewHeight(),
				getViewWidth(), getViewHeight(),
				getViewWidth(), 0) {
			stroke = Color.DIMGRAY
			strokeWidth = 2.5
			fill = null
		})
	}

	private fun getGridSize(): Double = DEFAULT_GRID_SIZE * zoom

	private fun getXGridCountDouble(): Double {
		val maxXCount = root.width / getGridSize() - 1	// the -1 allows it to resize to smaller correctly
		return if(maxXCount > lifeController.xSize) lifeController.xSize.toDouble() else maxXCount
	}
	private fun getYGridCountDouble(): Double {
		val maxYCount = root.height / getGridSize() - 1	// the -1 allows it to resize to smaller correctly
		return if(maxYCount > lifeController.ySize) lifeController.ySize.toDouble() else maxYCount
	}

	private fun getViewWidth(): Double = getXGridCountDouble() * getGridSize()
	private fun getViewHeight(): Double = getYGridCountDouble() * getGridSize()

	private fun getMinXRender(): Double = (root.width - getViewWidth()) / 2
	private fun getMinYRender(): Double = (root.height - getViewHeight()) / 2
	private fun getMaxXRender(): Double = root.width - getMinXRender()
	private fun getMaxYRender(): Double = root.height - getMinYRender()
}
