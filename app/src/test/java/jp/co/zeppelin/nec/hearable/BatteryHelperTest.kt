package jp.co.zeppelin.nec.hearable

import jp.co.zeppelin.nec.hearable.helper.BatteryHelper
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Sanity check battery level drawables returned for percent charge levels
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class BatteryHelperTest {
    @Test
    fun batteryLevelDrawables() {
        // Out of range
        var batteryPct = 110
        assertEquals(R.drawable.battery_100, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = -10
        assertEquals(R.drawable.battery_min, BatteryHelper.batteryDrawableForPct(batteryPct))

        // Edges
        batteryPct = 100
        assertEquals(R.drawable.battery_100, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 0
        assertEquals(R.drawable.battery_min, BatteryHelper.batteryDrawableForPct(batteryPct))

        // Inflection points
        batteryPct = 99
        assertEquals(R.drawable.battery_90, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 90
        assertEquals(R.drawable.battery_90, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 89
        assertEquals(R.drawable.battery_80, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 80
        assertEquals(R.drawable.battery_80, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 79
        assertEquals(R.drawable.battery_70, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 70
        assertEquals(R.drawable.battery_70, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 69
        assertEquals(R.drawable.battery_60, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 60
        assertEquals(R.drawable.battery_60, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 59
        assertEquals(R.drawable.battery_50, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 50
        assertEquals(R.drawable.battery_50, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 49
        assertEquals(R.drawable.battery_40, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 40
        assertEquals(R.drawable.battery_40, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 39
        assertEquals(R.drawable.battery_30, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 30
        assertEquals(R.drawable.battery_30, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 29
        assertEquals(R.drawable.battery_20, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 20
        assertEquals(R.drawable.battery_20, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 19
        assertEquals(R.drawable.battery_min, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 9
        assertEquals(R.drawable.battery_min, BatteryHelper.batteryDrawableForPct(batteryPct))
        batteryPct = 1
        assertEquals(R.drawable.battery_min, BatteryHelper.batteryDrawableForPct(batteryPct))
    }
}
