package red.man10.man10score

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.man10score.Man10Score.Companion.plugin
import java.text.SimpleDateFormat
import java.util.*

object ScoreDatabase {

    private val mysql = MySQLManager(Man10Score.plugin, "Score")

    private fun getUUID(name: String): UUID? {

        val rs = mysql.query("select uuid from player_data where mcid='$name';") ?: return null

        var uuid: UUID? = null

        if (rs.next()) {
            uuid = UUID.fromString(rs.getString("uuid"))
        }

        rs.close()
        mysql.close()

        return uuid

    }

    fun getScore(uuid: UUID): Int {

        val rs = mysql.query("select score from player_data where uuid='$uuid';") ?: return 0

        var score = 0

        if (rs.next()) {
            score = rs.getInt("score")
        }

        rs.close()
        mysql.close()

        return score
    }

    fun getScore(mcid: String): Int {

        val uuid = getUUID(mcid)

        val rs = mysql.query("select score from player_data where uuid='$uuid';") ?: return 0

        var score = 0

        if (rs.next()) {
            score = rs.getInt("score")
        }

        rs.close()
        mysql.close()

        return score
    }

    fun getScoreRanking(page: Int): MutableList<Pair<String, Int>> {

        val list = mutableListOf<Pair<String, Int>>()

        val rs =
            mysql.query("select mcid,score from player_data order by score desc limit 10 offset ${(page * 10) - 10}")
                ?: return mutableListOf(Pair("", 0))

        while (rs.next()) {
            list.add(Pair(rs.getString("mcid"), rs.getInt("score")))
        }

        return list
    }


    fun giveScore(mcid: String, amount: Int, reason: String, issuer: CommandSender): Boolean {

        val uuid = getUUID(mcid) ?: return false

        mysql.execute("update player_data set score=score+${amount} where uuid='$uuid';")

        mysql.execute(
            "INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " + "VALUES ('$mcid', '$uuid', $amount, '[give]:$reason','${issuer.name}',${
                getScore(
                    uuid
                )
            }, now())"
        )

        return true
    }

    fun setScore(mcid: String, amount: Int, reason: String, issuer: CommandSender): Boolean {

        val uuid = getUUID(mcid) ?: return false

        mysql.execute("update player_data set score=$amount where uuid='$uuid';")

        mysql.execute(
            "INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " + "VALUES ('$mcid', '$uuid', $amount, '[set]:$reason', '${issuer.name}',${
                getScore(
                    uuid
                )
            }, now())"
        )

        return true
    }

    fun canThank(uuid: UUID): Boolean {
        val rs =
            mysql.query("select date from score_log where uuid='$uuid' and note='[give]:Thankした' ORDER BY date DESC LIMIT 1;")
                ?: return true

        var ret = false

        if (rs.next()) {

            val data = Calendar.getInstance()
            data.time = rs.getDate("date")
            data.add(Calendar.HOUR_OF_DAY, 24)

            if (Date().after(data.time)) {
                ret = true
            }
        } else {
            ret = true
        }

        rs.close()
        mysql.close()

        return ret
    }

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

    fun getScoreLog(mcid: String, page: Int): MutableList<ScoreLog> {

        val rs =
            mysql.query("select * from score_log where uuid='${getUUID(mcid)}' order by id desc Limit 10 offset ${(page) * 10};")
                ?: return Collections.emptyList()

        val list = mutableListOf<ScoreLog>()

        while (rs.next()) {

            val data = ScoreLog()

            data.score = rs.getDouble("score")
            data.note = rs.getString("note")!!
            data.dateFormat = simpleDateFormat.format(rs.getTimestamp("date"))

            list.add(data)
        }

        mysql.close()
        rs.close()

        return list

    }

    //thank fuckの回数
    fun getActionCount(mcid: String): ActionData {

        val actionData = ActionData()

        val uuid = getUUID(mcid) ?: return actionData

        val mysql = MySQLManager(Man10Score.plugin, "Man10Score")
        val rs = mysql.query(
            "select " +
                    "count(note like '%Thankされた%' or null)," +
                    "count(note like '%Thankした%' or null)," +
                    "count(note like '%Fuckされた%' or null)," +
                    "count(note like '%FUCKした%' or null) from score_log where uuid='${uuid}';"
        ) ?: return actionData

        if (!rs.next()){
            actionData.getData = false
            return actionData
        }

        actionData.givenThank = rs.getInt(1)
        actionData.doThank = rs.getInt(2)
        actionData.givenFuck = rs.getInt(3)
        actionData.doFuck = rs.getInt(4)

        rs.close()
        mysql.close()

        return actionData
    }

    fun getSubAccount(p:Player):List<UUID> {
        return getSubAccount(p.uniqueId)
    }

    fun getSubAccount(uuid: UUID):List<UUID> {

        val db = MySQLManager(plugin,"AltCheck")

        val rs = db.query("select uuid from connection_log where ip in (select ip from connection_log " +
                "where uuid = '${uuid}' group by mcid, ip order by ip) group by uuid;")?:return Collections.emptyList()

        val accountList = mutableListOf<UUID>()

        while (rs.next()){accountList.add(UUID.fromString(rs.getString("uuid")))}

        rs.close()
        db.close()

        return accountList.toList()
    }

    class ScoreLog {

        var score = 0.0
        var note = ""
        var dateFormat = ""

    }

    class ActionData {
        var getData = true
        var givenThank = 0
        var doThank = 0
        var givenFuck = 0
        var doFuck = 0
    }
}