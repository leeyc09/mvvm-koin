package xlab.world.xlab.view.register

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.ViewFunction

class LocalRegisterActivity : AppCompatActivity() {
    private val registerViewModel: RegisterViewModel by viewModel()
    private val viewFunction: ViewFunction by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_register)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LocalRegisterActivity::class.java)
        }
    }
}
