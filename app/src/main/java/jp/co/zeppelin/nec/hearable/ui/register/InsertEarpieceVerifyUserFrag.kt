package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.ui.register.base.InsertEarpieceBaseFrag
import kotlinx.android.synthetic.main.frag_insert_earpiece.*

/**
 * Screen prompting user to insert NEC Hearable earpiece for authentication
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class InsertEarpieceVerifyUserFrag : InsertEarpieceBaseFrag() {
    private val TAG = InsertEarpieceVerifyUserFrag::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenType = ScreenType.VerifyUser
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {
            // Reset system top status bar color from previous screen using themed color override
            this.window?.statusBarColor = 0
        }

        buttonNextStateB.setOnClickListener {
            NavHostFragment.findNavController(this@InsertEarpieceVerifyUserFrag)
                .navigate(InsertEarpieceVerifyUserFragDirections.actionNavInsertEarpieceVerifyUserToNavVerifyUser())
        }
    }
}
