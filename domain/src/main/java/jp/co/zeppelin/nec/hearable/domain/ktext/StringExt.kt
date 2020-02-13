package jp.co.zeppelin.nec.hearable.domain.ktext

/**
 * Print "nice" hex/decimal forms of Long
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
fun Long.hexAndDec(): String = "0x${this.toString(16)}/$this"
