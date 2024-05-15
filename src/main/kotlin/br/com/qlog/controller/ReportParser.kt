package br.com.qlog.controller

import br.com.qlog.model.Report
import java.util.regex.Pattern

fun evaluateNewGame(command: String, reports: List<Report>) {
    if (command == "InitGame") {
        reports.addLast(Report(game = "game_" + (reports.count() + 1)))
    }
}

fun evaluatePlayer(command: String, log: String, reports: List<Report>) {
    if (command == "ClientUserinfoChanged" && reports.isNotEmpty()) {
        val r = reports.last()
        val player = log.split("n\\")[1].split("\\")[0]
        r.players.add(player)
    }
}

fun evaluateKill(command: String, log: String, reports: List<Report>) {
    if (command == "Kill" && reports.isNotEmpty()) {
        val r = reports.last()
        r.totalKills++
        // TODO better error handling for malformed log
        val killInfo = log.split(":\\s+".toRegex())[2].split("\\s+killed\\s+|\\s+by\\s+".toRegex())
        val killer = killInfo[0]
        val dead = killInfo[1]

        if (killer == "<world>") {
            r.kills[dead] = (r.kills[dead] ?: 0) - 1
        } else {
            r.kills[killer] = (r.kills[killer] ?: 0) + 1
        }
        if (killInfo.count() == 3) {
            val weapon = killInfo[2]
            r.killsByMeans[weapon] = (r.killsByMeans[weapon] ?: 0) + 1
        }
    }
}

val commandPattern: Pattern = Pattern.compile("[A-Za-z]\\w+")

val logParser: (String, List<Report>) -> Unit = { log, reports ->
    val commandMatcher = commandPattern.matcher(log)
    if (commandMatcher.find()) {
        val command = commandMatcher.group()
        evaluateNewGame(command, reports)
        evaluatePlayer(command, log, reports)
        evaluateKill(command, log, reports)
    }
}

fun report(reports: List<Report>) = reports.map { print(it) }