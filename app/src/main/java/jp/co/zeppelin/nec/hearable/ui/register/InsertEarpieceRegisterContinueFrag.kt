package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.ui.register.base.InsertEarpieceBaseFrag
import kotlinx.android.synthetic.main.frag_insert_earpiece.*

/**
 * Screen prompting user to insert NEC Hearable earpiece for additional feature registration attempts
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class InsertEarpieceRegisterContinueFrag : InsertEarpieceBaseFrag() {
    private val TAG = InsertEarpieceRegisterContinueFrag::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenType = ScreenType.AdditionalFeaturesRequired
        super.onViewCreated(view, savedInstanceState)
        // Skip first of two screens for this case
        updateScreenOnStateChange()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonNextStateB.setOnClickListener {
            NavHostFragment.findNavController(this@InsertEarpieceRegisterContinueFrag)
                .navigate(InsertEarpieceRegisterContinueFragDirections.actionNavInsertEarpieceRegisterContinueToNavPlayRegisterSoundsNextThree())
        }
    }
}
