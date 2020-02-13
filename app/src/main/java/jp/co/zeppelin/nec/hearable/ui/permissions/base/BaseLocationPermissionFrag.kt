package jp.co.zeppelin.nec.hearable.ui.permissions.base

import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R

/**
 * AAC "Navigation" component stubbornly remembers last destination and immediately restores the
 * user to that location in the nav graph on app relaunch.
 *
 * Unfortunately, this nullifies the whole idea of revoking permissions in system settings forcing
 * an app restart to essentially "start fresh;" without this sort of base activity constantly confirming
 * location permission granted on every screen, risk re-entering nav graph flow at late stage screen
 * and lacking crucial permissions.  Forcing all fragments to inherit from this isn't very pretty but
 * unless and until the framework is modified to handle permissions this is going to be a robust way
 * to deal with the problem
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class BaseLocationPermissionFrag : BaseBluetoothCheckFrag() {
    private val TAG = BaseLocationPermissionFrag::class.java.simpleName

    override fun onStart() {
        super.onStart()
        viewModel.checkAndRequestPermissionsBluetoothNLocation()
    }

    override fun onResume() {
        super.onResume()
        // Hack for corner case where user navigated this far in app, opened system settings without
        // closing app and revoked location permission, then returned to app
        if (!viewModel.isPermissionLocationGranted()) {
            NavHostFragment.findNavController(this@BaseLocationPermissionFrag)
                .navigate(R.id.nav_permission_location_explain)
        }
    }
}
