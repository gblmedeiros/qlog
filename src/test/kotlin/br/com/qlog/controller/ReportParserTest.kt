package br.com.qlog.controller

import br.com.qlog.model.Report
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportParserTest {

    @Test
    fun `when identifies new game log, create a new Report`() {
        val reports = mutableListOf<Report>()
        logParser("InitGame", reports)
        assertEquals(1, reports.count())
        assertEquals("game_1", reports.last().game)

        logParser("InitGame", reports)
        assertEquals(2, reports.count())
        assertEquals("game_2", reports.last().game)
    }

    @Test
    fun `when it is not new game log, does not create a new Report`() {
        val reports = mutableListOf<Report>()
        logParser("Abacaxi", reports)
        assertEquals(0, reports.count())

        reports.add(Report("game_1"))
        logParser("Abacaxi", reports)
        assertEquals(1, reports.count())
        assertEquals("game_1", reports.last().game)
    }

    @Test
    fun `when it is a player log, adds new player`() {
        val reports = listOf(Report("game_1"))
        logParser("ClientUserinfoChanged: 2 n\\Isgalamido\\t\\0\\model\\uriel/zael\\hmodel\\uriel/zael\\g_redteam\\\\g_blueteam\\\\c1\\5\\c2\\5\\hc\\100\\w\\0\\l\\0\\tt\\0\\tl\\0", reports)
        assertEquals(1, reports.last().players.count())
        assertTrue(reports.last().players.contains("Isgalamido"))

        // then do not duplicate user if same log
        logParser("ClientUserinfoChanged: 2 n\\Isgalamido\\t\\0\\model\\uriel/zael\\hmodel\\uriel/zael\\g_redteam\\\\g_blueteam\\\\c1\\5\\c2\\5\\hc\\100\\w\\0\\l\\0\\tt\\0\\tl\\0", reports)
        assertEquals(1, reports.last().players.count())
        assertTrue(reports.last().players.contains("Isgalamido"))

        // then adds new player
        logParser("21:51 ClientUserinfoChanged: 3 n\\Dono da Bola\\t\\0\\model\\sarge/krusade\\hmodel\\sarge/krusade\\g_redteam\\\\g_blueteam\\\\c1\\5\\c2\\5\\hc\\95\\w\\0\\l\\0\\tt\\0\\tl\\0", reports)
        assertEquals(2, reports.last().players.count())
        assertTrue(reports.last().players.contains("Isgalamido"))
        assertTrue(reports.last().players.contains("Dono da Bola"))
    }

    @Test
    fun `when it is a player log, and has no game init, do nothing`() {
        val reports = listOf<Report>()
        logParser("ClientUserinfoChanged: 2 n\\Isgalamido\\t\\0\\model\\uriel/zael\\hmodel\\uriel/zael\\g_redteam\\\\g_blueteam\\\\c1\\5\\c2\\5\\hc\\100\\w\\0\\l\\0\\tt\\0\\tl\\0", reports)
        assertTrue(reports.isEmpty())
    }

    @Test
    fun `when it is not a player log, do nothing`() {
        val reports = listOf(Report("game_1"))
        logParser("Abacaxi", reports)
        assertTrue(reports.last().players.isEmpty())
    }

    @Test
    fun `when it is a kill log, then adds kills to the user and count by weapons`() {
        val reports = listOf(Report("game_1"))
        logParser("22:06 Kill: 2 3 7: Isgalamido killed Mocinha by MOD_ROCKET_SPLASH", reports)
        assertEquals(1, reports.last().totalKills)
        assertEquals(1, reports.last().kills["Isgalamido"])
        assertEquals(1, reports.last().killsByMeans["MOD_ROCKET_SPLASH"])

        logParser("22:06 Kill: 2 3 7: Dono da Bola killed Mocinha by MOD_ROCKET_SPLASH", reports)
        assertEquals(2, reports.last().totalKills)
        assertEquals(1, reports.last().kills["Dono da Bola"])
        assertEquals(2, reports.last().killsByMeans["MOD_ROCKET_SPLASH"])

    }

    @Test
    fun `when it is a world kill, then remove user kills`() {
        val reports = listOf(Report("game_1"))
        logParser("20:54 Kill: 1022 2 22: <world> killed Isgalamido by MOD_TRIGGER_HURT", reports)
        assertEquals(1, reports.last().totalKills)
        assertEquals(-1, reports.last().kills["Isgalamido"])
        assertEquals(1, reports.last().killsByMeans["MOD_TRIGGER_HURT"])

        logParser("22:06 Kill: 2 3 7: Isgalamido killed Mocinha by MOD_ROCKET_SPLASH", reports)
        assertEquals(2, reports.last().totalKills)
        assertEquals(0, reports.last().kills["Isgalamido"])
        assertEquals(1, reports.last().killsByMeans["MOD_ROCKET_SPLASH"])

        logParser("20:54 Kill: 1022 2 22: <world> killed Isgalamido by MOD_TRIGGER_HURT", reports)
        logParser("20:54 Kill: 1022 2 22: <world> killed Isgalamido by MOD_TRIGGER_HURT", reports)
        assertEquals(4, reports.last().totalKills)
        assertEquals(-2, reports.last().kills["Isgalamido"])
        assertEquals(3, reports.last().killsByMeans["MOD_TRIGGER_HURT"])
    }

    @Test
    fun `when user kills himself, counts as their kill`() {
        val reports = listOf(Report("game_1"))
        logParser("20:54 Kill: 1022 2 22: Isgalamido killed Isgalamido by MOD_TRIGGER_HURT", reports)
        assertEquals(1, reports.last().totalKills)
        assertEquals(1, reports.last().kills["Isgalamido"])
        assertEquals(1, reports.last().killsByMeans["MOD_TRIGGER_HURT"])
    }

    @Test
    fun `when it is a kill log, but no game was init, do nothing`() {
        val reports = listOf<Report>()
        logParser("20:54 Kill: 1022 2 22: Isgalamido killed Isgalamido by MOD_TRIGGER_HURT", reports)
        assertTrue(reports.isEmpty())
    }

    @Test
    fun `when it is not a kill log, do nothing`() {
        val reports = listOf<Report>(Report("game_1"))
        logParser("20:54 Abacaxi: 1022 2 22: Isgalamido killed Isgalamido by MOD_TRIGGER_HURT", reports)
        assertEquals(0, reports.last().totalKills)
    }

    @Test
    fun `read a full log file and assert the result`() {
        val reports = mutableListOf<Report>()
        this::class.java.getResourceAsStream("/qgame.log")?.bufferedReader(Charsets.UTF_8)?.forEachLine {
            logParser(it, reports)
        }
        val expected = this::class.java.getResourceAsStream("/fixtures/expectedReports.txt")?.bufferedReader(Charsets.UTF_8)?.readText()
        assertEquals(expected, reports.joinToString("") { it.toString() })
    }
}