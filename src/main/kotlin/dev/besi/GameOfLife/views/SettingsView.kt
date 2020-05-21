package dev.besi.GameOfLife.views

import dev.besi.GameOfLife.LifeController
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.ToggleButton
import tornadofx.*
import kotlin.math.abs

class SettingsView : View("Settings View") {
	private val lifeController: LifeController by inject()

	private val xSizeProperty = SimpleIntegerProperty(lifeController.xSizeProperty.value)
	private val ySizeProperty = SimpleIntegerProperty(lifeController.ySizeProperty.value)

	private val sProperties = Array(9) { i ->
		SimpleBooleanProperty(lifeController.s.contains(i))
	}
	private val bProperties = Array(9) { i ->
		SimpleBooleanProperty(lifeController.b.contains(i))
	}

	private var startStopButton: ToggleButton? = null

	override val root = vbox {
		label("Map size")
		hbox {
			textfield(xSizeProperty) { filterInput { it.controlNewText.isInt() } }
			label("x")
			textfield(ySizeProperty) { filterInput { it.controlNewText.isInt() } }
		}
		label("Survival")
		hbox {
			sProperties.forEachIndexed { i, property -> checkbox(i.toString(), property) }
		}
		label("Birth")
		hbox {
			bProperties.forEachIndexed { i, property -> checkbox(i.toString(), property) }
		}
		button("Refresh") {
			action {
				lifeController.xSizeProperty.value = xSizeProperty.value
				lifeController.ySizeProperty.value = ySizeProperty.value
				lifeController.s = sProperties
						.mapIndexed { i, property -> if (property.value) i else -1 }
						.filter { it >= 0 }
						.toIntArray()
				lifeController.b = bProperties
						.mapIndexed { i, property -> if (property.value) i else -1 }
						.filter { it >= 0 }
						.toIntArray()
			}
		}
		label("Game speed")
		slider(0.25, 10, lifeController.gameSpeed) {
			this.valueProperty().onChange {
				if (abs(lifeController.gameSpeed - this.value) > 0.15) {
					lifeController.gameSpeed = this.value
				}
			}
		}
		button("Clear") {
			action {
				lifeController.clear()
				startStopButton?.isSelected = false
				startStopButton?.text = "Start"
			}
		}
		startStopButton = togglebutton("Start") {
			isSelected = false
			action {
				text = if (isSelected) {
					lifeController.start()
					"Stop"
				} else {
					lifeController.stop()
					"Start"
				}
			}
		}
	}
}
