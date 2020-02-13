package jp.co.zeppelin.nec.hearable

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommon
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommonFactory
import pub.devrel.easypermissions.EasyPermissions

/**
 * Top level activity
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: ViewModelCommon

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        ZepLog.d(TAG, "onRequestPermissionsResult(): code $requestCode")
        ZepLog.d(TAG, "onRequestPermissionsResult(): perms:")
        permissions.forEach { ZepLog.d(TAG, "    $it") }
        ZepLog.d(TAG, "onRequestPermissionsResult(): results:")
        grantResults.forEach { ZepLog.d(TAG, "    $it") }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            viewModel.permissionsHelper
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModelFactory =
            ViewModelCommonFactory(this)
        viewModel =
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)

        viewModel.startService(applicationContext)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.visibility = View.GONE

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.getHeaderView(0)?.let { navHeaderView ->
            val textViewBuildVer: TextView = navHeaderView.findViewById(R.id.textViewBuildVersion)
            textViewBuildVer.text = BuildConfig.VERSION_NAME
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return when (item.itemId) {
            R.id.action_global_nav_settings -> {
                navController.navigate(item.itemId)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
