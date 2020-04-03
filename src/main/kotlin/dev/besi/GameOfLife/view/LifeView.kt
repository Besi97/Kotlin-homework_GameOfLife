package dev.besi.GameOfLife.view

import dev.besi.GameOfLife.LifeController
import javafx.beans.value.ObservableValue
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class LifeView : View("Life View") {
	private val lifeController: LifeController by inject()

	companion object {
		const val DEFAULT_GRID_SIZE = 16

		var zoom = 1.0
		var xCenter = 0
		var yCenter = 0
	}

	init {
		xCenter = lifeController.lifeConfig.xSize/2
		yCenter = lifeController.lifeConfig.ySize/2
	}

	private val resizeListener = fun(observable: ObservableValue<out Number>?, oldValue: Number?, newValue: Number?) {
		println(root.width)
		root.clear()
		drawGrid()
		drawBorder()
	}

	override val root: StackPane = stackpane {
		this@stackpane.widthProperty().addListener(resizeListener)
		this@stackpane.heightProperty().addListener(resizeListener)
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

	private fun drawGrid() {
		val viewXCenter = getViewWidth() / 2
		val viewYCenter = getViewHeight() / 2
		root.add(root.circle(viewXCenter, viewYCenter, 5) {fill = Color.RED})

		for (i in 1 until getXGridCountDouble().toInt() + 1) {
		    root.add(root.line {
				startX = i * getGridSize() + (root.width - getViewWidth()) / 2
				startY = 0.0 + (root.height - getViewHeight()) / 2
				endX = startX
				endY = getViewHeight() + (root.height - getViewHeight()) / 2
				isManaged = false
				stroke = Color.LIGHTGRAY
			})
		}
		for (i in 1 until getYGridCountDouble().toInt() + 1) {
		    root.add(root.line {
				startX = 0.0 + (root.width - getViewWidth()) / 2
				startY = i * getGridSize() + (root.height - getViewHeight()) / 2
				endX = getViewWidth() + (root.width - getViewWidth()) / 2
				endY = startY
				isManaged = false
				stroke = Color.LIGHTGRAY
			})
		}
	}

	private fun getGridSize(): Double {
		return DEFAULT_GRID_SIZE * zoom
	}

	private fun getXGridCountDouble(): Double {
		val maxXCount = root.width / getGridSize() - 1	// the -1 allows it to resize to smaller correctly
		return if(maxXCount > lifeController.lifeConfig.xSize) lifeController.lifeConfig.xSize.toDouble() else maxXCount
	}

	private fun getYGridCountDouble(): Double {
		val maxYCount = root.height / getGridSize() - 1	// the -1 allows it to resize to smaller correctly
		return if(maxYCount > lifeController.lifeConfig.ySize) lifeController.lifeConfig.ySize.toDouble() else maxYCount
	}

	private fun getViewWidth(): Double {
	    return getXGridCountDouble() * getGridSize()
	}

	private fun getViewHeight(): Double {
	    return getYGridCountDouble() * getGridSize()
	}
}
