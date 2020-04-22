package dev.besi.GameOfLife.views

import javafx.beans.property.BooleanProperty
import javafx.scene.Group
import javafx.scene.paint.Color
import tornadofx.*

class Cell(
		private val isAliveProperty: BooleanProperty,
		private val size: Double,
		op: Group.() -> Unit = {}
): View("Cell instance") {
	override val root = group {
		rectangle {
			width = size
			height = size

			stroke = Color.BLACK
			strokeWidth = size / 25
			fill = if(isAliveProperty.value)
				Color.BLACK
			else
				Color.WHITESMOKE

			isAliveProperty.addListener { observable, oldValue, newValue ->
				fill = if(newValue)
					Color.BLACK
				else
					Color.WHITESMOKE
			}
		}
		op()
	}
}
