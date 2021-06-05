package pt.isel.pdm.drag.game.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pt.isel.pdm.drag.repo.WordLanguage
import java.time.Instant

@Parcelize
data class LiveGameBlockState(
    val curPlayer: Int,
    val block: BlockState,
    val completed: Boolean = false
): Parcelable, Model<LiveGameBlockStateDto> {

    override fun mapToDto() = LiveGameBlockStateDto(
        curPlayer,
        block.word,
        block.board.boardString,
        completed
    )

}

data class LiveGameBlockStateDto(
    val curPlayer: Int,
    val word: String,
    val board: String,
    val completed: Boolean
): Dto<LiveGameBlockState> {

    constructor(): this(-1, "", "", false)

    override fun mapToModel() = LiveGameBlockState(
        curPlayer,
        BlockState(word, DrawingBoard.boardFromString(board)),
        completed
    )

}

@Parcelize
data class LiveGameState(
    val id: String,
    val roundTime: Long,
    val maxPlayers: Int,
    val blockStates: MutableMap<String, LiveGameBlockState>,
    val joinedPlayers: Map<String, RemotePlayerState?>,
    val gameState: GameState = GameState.WAITING,
    val lang: WordLanguage = WordLanguage.FALLBACK_LANG,
    val error: String? = null,
): Parcelable, Model<LiveGameStateDto> {

    override fun mapToDto() = LiveGameStateDto(
        id,
        roundTime,
        maxPlayers,
        blockStates.mapValues { (_, v) -> v.mapToDto() },
        joinedPlayers.mapValues { (_, v) -> v?.mapToDto() },
        gameState,
        lang,
        error
    )

}

data class LiveGameStateDto(
    val id: String,
    val roundTime: Long,
    val maxPlayers: Int,
    val blockStates: Map<String, LiveGameBlockStateDto>,
    val joinedPlayers: Map<String, RemotePlayerStateDto?>,
    val gameState: GameState,
    val lang: WordLanguage,
    val error: String? = null
): Dto<LiveGameState> {

    constructor(): this("", -1, -1, mapOf(), mapOf(), GameState.WAITING, WordLanguage.FALLBACK_LANG)

    override fun mapToModel() = LiveGameState(
        id,
        roundTime,
        maxPlayers,
        blockStates.mapValues { (_, v) -> v.mapToModel() }.toMutableMap(),
        joinedPlayers.mapValues { (_, v) -> v?.mapToModel() },
        gameState,
        lang,
        error
    )

}

@Parcelize
data class LocalPlayerState(
    val playerName: String,
    val playerId: Int,
    val blockId: Int,
    val roundStart: Instant = Instant.now()
): Parcelable

@Parcelize
data class RemotePlayerState(
    val playerName: String,
    val blockId: Int
): Parcelable, Model<RemotePlayerStateDto> {

    override fun mapToDto() = RemotePlayerStateDto(
        playerName,
        blockId
    )

}

@Parcelize
data class RemotePlayerStateDto(
    val playerName: String,
    val blockId: Int
): Parcelable, Dto<RemotePlayerState> {

    constructor(): this("", -1)

    override fun mapToModel() = RemotePlayerState(
        playerName,
        blockId
    )

}

@Parcelize
data class PlayerConfiguration(
    val playerName: String,
    val gameId: String? = null
): Parcelable