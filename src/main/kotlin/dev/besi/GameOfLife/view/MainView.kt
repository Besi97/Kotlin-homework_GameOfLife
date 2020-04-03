package dev.besi.GameOfLife.view

import dev.besi.GameOfLife.LifeConfig
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
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
				0.85 * Screen.getPrimary().visualBounds.height)


		lifeView.root.hboxConstraints{
			hGrow = Priority.ALWAYS
			marginRight = PADDING
		}
	}
}
