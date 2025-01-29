package com.pspgames.library.downloader


class DownloadSpeedChecker {
    fun test(){
    }
//    fun check(){
//        val BeforeTime = System.currentTimeMillis()
//        val TotalTxBeforeTest = TrafficStats.getTotalTxBytes()
//        val TotalRxBeforeTest = TrafficStats.getTotalRxBytes()
//
//        val TotalTxAfterTest = TrafficStats.getTotalTxBytes()
//        val TotalRxAfterTest = TrafficStats.getTotalRxBytes()
//        val AfterTime = System.currentTimeMillis()
//
//        val TimeDifference = (AfterTime - BeforeTime).toDouble()
//
//        val rxDiff = (TotalRxAfterTest - TotalRxBeforeTest).toDouble()
//        val txDiff = (TotalTxAfterTest - TotalTxBeforeTest).toDouble()
//        if ((rxDiff != 0.0) && (txDiff != 0.0)) {
//            val rxBPS = (rxDiff / (TimeDifference / 1000)) // total rx bytes per second.
//            val txBPS = (txDiff / (TimeDifference / 1000)) // total tx bytes per second.
//            testing.get(0) = rxBPS.toString() + "B/s. Total rx = " + rxDiff
//            testing.get(1) = txBPS.toString() + "B/s. Total tx = " + txDiff
//        } else {
//            testing.get(0) = "No uploaded or downloaded bytes."
//        }
//    }
}