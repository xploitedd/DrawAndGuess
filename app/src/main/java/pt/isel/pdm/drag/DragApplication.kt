package pt.isel.pdm.drag

import android.app.Application
import androidx.room.Room
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isel.pdm.drag.database.HistoryDatabase
import pt.isel.pdm.drag.repo.GameRepository
import pt.isel.pdm.drag.repo.LiveGameRepository
import pt.isel.pdm.drag.repo.WordRepository
import pt.isel.pdm.drag.services.WordService

class DragApplication : Application() {

    val gameRepository: GameRepository by lazy {
        GameRepository(
            Room.databaseBuilder(
                this,
                HistoryDatabase::class.java,
                "HistoryDatabase"
            ).fallbackToDestructiveMigration()
                .build()
        )
    }

    val liveGameRepository: LiveGameRepository by lazy {
        LiveGameRepository(Firebase.firestore)
    }

    val wordRepository: WordRepository by lazy {
        WordRepository(WordService.getInstance())
    }

}