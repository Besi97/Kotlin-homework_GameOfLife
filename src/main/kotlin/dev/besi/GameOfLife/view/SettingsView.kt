package dev.besi.GameOfLife.view

import dev.besi.GameOfLife.LifeController
import tornadofx.*

class SettingsView : View("Settings View") {
	val lifeController: LifeController by inject()

	override val root = vbox {
		label("Settings view")
	}
}
