package jp.co.zeppelin.nec.hearable.navigation

/**
 * Track state of new vs returing user flow
 *
 * Typ. viewModel will store this value; eliminates need to pass screen-to-screen
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface INavigationContract {
    enum class LoginSignup {
        Signup,
        Login
    }

    val loginSignup: LoginSignup
}
