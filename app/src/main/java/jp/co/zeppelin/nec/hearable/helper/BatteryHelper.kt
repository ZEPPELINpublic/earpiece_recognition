package jp.co.zeppelin.nec.hearable.helper

import jp.co.zeppelin.nec.hearable.R

/**
 * Provide appropriate battery level drawables for percent charge levels
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
object BatteryHelper {

    /**
     * Get battery level indicator drawable for specified percentage charge level
     *
     * @param batteryPct  Battery percent in range [0,100]
     * @return  battery level drawable resource appropriate for input percent charge level
     */
    fun batteryDrawableForPct(batteryPctRaw: Int): Int {
        val batteryPct = maxOf(minOf(batteryPctRaw, 100), 0)
        val batteryPctFloor: Int = (batteryPct / 10) * 10
        return batteryLevelsMap[batteryPctFloor] ?: R.drawable.battery_min
    }

    val batteryLevelsMap = mapOf(
        0 to R.drawable.battery_min,
        10 to R.drawable.battery_min,
        20 to R.drawable.battery_20,
        30 to R.drawable.battery_30,
        40 to R.drawable.battery_40,
        50 to R.drawable.battery_50,
        60 to R.drawable.battery_60,
        70 to R.drawable.battery_70,
        80 to R.drawable.battery_80,
        90 to R.drawable.battery_90,
        100 to R.drawable.battery_100
    )
}
