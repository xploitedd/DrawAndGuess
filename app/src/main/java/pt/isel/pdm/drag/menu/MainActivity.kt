package pt.isel.pdm.drag.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.isel.pdm.drag.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private val binding: MainActivityBinding by lazy { MainActivityBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

}