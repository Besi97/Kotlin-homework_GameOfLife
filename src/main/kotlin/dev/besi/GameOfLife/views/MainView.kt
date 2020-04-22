package dev.besi.GameOfLife.views

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventTarget
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
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
				0.7 * Screen.getPrimary().visualBounds.width,
				0.8 * Screen.getPrimary().visualBounds.height
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
		recycleGridPane {
			alignment = Pos.CENTER
			rowConstraints.clear()
			val rowConstraint = RowConstraints(16.0)
			rowConstraint.valignment = VPos.CENTER
			for (i in 0 until 7) {
				rowConstraints.add(rowConstraint)
			}
			columnConstraints.clear()
			val columnConstraint = ColumnConstraints(16.0)
			columnConstraint.halignment = HPos.CENTER
			for (i in 0 until 7) {
				columnConstraints.add(columnConstraint)
			}
			for (i in 2 until 6) {
				for (j in 2 until 5) {
					add(Cell(SimpleBooleanProperty(i % 2 == j % 2), 16.0).root, i, j)
				}
			}
			isGridLinesVisible = true

			this@stackpane.setOnScroll { event ->
				event.consume()
				val zoomFactor = 1 + event.deltaY/100
				scaleX *= zoomFactor
				scaleY *= zoomFactor
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
		}
		layoutBoundsProperty().addListener { observable, oldValue, newValue ->
			clip = Rectangle(newValue.minX, newValue.minY, newValue.width, newValue.height)
		}
		hboxConstraints{
			hGrow = Priority.ALWAYS
		}
	}
	private fun EventTarget.recycleGridPane(op: RecycleGridPane.() -> Unit): RecycleGridPane = opcr(this, RecycleGridPane(), op)
}
