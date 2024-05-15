package br.com.qlog.model

class Report(
    val game: String,
    var totalKills: Int = 0,
    val players: MutableSet<String> = mutableSetOf(),
    val kills: MutableMap<String, Int> = hashMapOf(),
    val killsByMeans: MutableMap<String, Int> = hashMapOf()) {

    /**
     * Prints Report as string, it does not surround string by quotes as in JSON format.
     */
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(game).append(": {").append("\n")
            .append("\t").append("total_kills: ").append(totalKills).append(",\n")
            .append("\t").append("players: [").append(players.joinToString(", ")).append("],\n")

        if (kills.isNotEmpty()) {
            sb.append("\t").append("kills: {").append("\n")
            val sortedKills = kills
                .entries.sortedBy { it.value }
                .joinToString(",\n\t\t") { (k, v) -> "$k: $v" }
            sb.append("\t\t")
                .append(sortedKills)
                .append("\n")
                .append("\t").append("}")
                .append("\n")
        }

        if (killsByMeans.isNotEmpty()) {
            sb.append("\t").append("kills_by_means: {").append("\n")
                .append("\t\t").append(killsByMeans.entries.joinToString(",\n\t\t") { (k, v) -> "$k: $v" })
                .append("\n")
                .append("\t").append("}")
                .append("\n")
        }
        sb.append("}").append("\n")

        return sb.toString()
    }
}