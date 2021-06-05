package pt.isel.pdm.drag.game.view

import pt.isel.pdm.drag.game.model.GameState

interface GameStateDependent {

    /**
     * Sets the view mode to the specified game state
     * @param state the current game state
     */
    fun setMode(state: GameState)

}