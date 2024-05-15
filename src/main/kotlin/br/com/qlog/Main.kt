package br.com.qlog

import br.com.qlog.controller.logParser
import br.com.qlog.controller.report
import br.com.qlog.model.Report

fun main() {
    val reports = mutableListOf<Report>()
    Any::class::class.java.getResourceAsStream("/qgame.log")?.bufferedReader(Charsets.UTF_8)?.forEachLine {
        logParser(it, reports)
    }
    report(reports)
}
