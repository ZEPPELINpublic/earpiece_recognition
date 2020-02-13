package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.ui.register.base.InsertEarpieceBaseFrag
import kotlinx.android.synthetic.main.frag_insert_earpiece.*

/**
 * Screen prompting user to insert NEC Hearable earpiece for (new) user registration
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class InsertEarpieceTutorialFrag : InsertEarpieceBaseFrag() {
    private val TAG = InsertEarpieceTutorialFrag::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenType = ScreenType.Tutorial
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonNextStateB.setOnClickListener {
            NavHostFragment.findNavController(this@InsertEarpieceTutorialFrag)
                .navigate(InsertEarpieceTutorialFragDirections.actionNavInsertEarpieceRegisterToRegisterEarFeatTutorialFrag())
        }
    }
}
