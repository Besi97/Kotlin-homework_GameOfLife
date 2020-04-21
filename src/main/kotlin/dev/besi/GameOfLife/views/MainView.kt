package dev.besi.GameOfLife.views

import javafx.scene.layout.Priority
import javafx.scene.shape.Rectangle
import javafx.stage.Screen
import tornadofx.*

class MainView : View("Game of Life") {
	private val lifeView: LifeView by inject()
	private val settingsView: SettingsView by inject()

	companion object {
		private const val PADDING = 16.0
		private val PADDING_UNIT = Dimension.LinearUnits.px
	}

	override val root = hbox {
		add(lifeView)
		//add(TestView::class)
		add(settingsView)
		style {
			padding = box(
					Dimension(PADDING, PADDING_UNIT),
					Dimension(PADDING, PADDING_UNIT),
					Dimension(PADDING, PADDING_UNIT),
					Dimension(PADDING, PADDING_UNIT))
		}
	}

	init {
		this.setWindowMinSize(
				0.75 * Screen.getPrimary().visualBounds.width,
				0.85 * Screen.getPrimary().visualBounds.height
		)

		lifeView.root.hboxConstraints{
			hGrow = Priority.ALWAYS
		}
		settingsView.root.hboxConstraints {
			marginLeft = PADDING
		}
	}
}

class TestView: View("test") {
	private var xMouseAnchor = 0.0
	private var yMouseAnchor = 0.0

	override val root = stackpane {
		val group = group {
			rectangle(200, 200, 300, 100) { isManaged = false }
			rectangle(100, 100, 100, 100) { isManaged = false }
		}
		layoutBoundsProperty().addListener { observable, oldValue, newValue ->
			clip = Rectangle(newValue.minX, newValue.minY, newValue.width, newValue.height)
		}
		setOnScroll { event ->
			event.consume()
			val zoomFactor = 1 + event.deltaY/100
			group.scaleX *= zoomFactor
			group.scaleY *= zoomFactor
		}
		hboxConstraints{
			hGrow = Priority.ALWAYS
			marginRight = 16.0
		}
		setOnMousePressed { event -> if(event.isPrimaryButtonDown) {
			xMouseAnchor = event.x
			yMouseAnchor = event.y
		} }
		setOnMouseDragged { event -> if(event.isPrimaryButtonDown) {
			group.apply {
				translateX -= xMouseAnchor - event.x
				translateY -= yMouseAnchor - event.y
			}
			xMouseAnchor = event.x
			yMouseAnchor = event.y
		} }
	}
}
