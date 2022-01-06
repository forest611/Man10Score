package red.man10.man10score

import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat
import java.util.*

object ScoreDatabase {

    private val mysql = MySQLManager(Man10Score.plugin,"Score")

    private fun getUUID(name:String):UUID?{

        val rs = mysql.query("select uuid from player_data where mcid='$name';")?:return null

        var uuid:UUID? = null

        if (rs.next()){
            uuid = UUID.fromString(rs.getString("uuid"))
        }

        rs.close()
        mysql.close()

        return uuid

    }

    fun getScore(uuid: UUID):Int{

        val rs = mysql.query("select score from player_data where uuid='$uuid';")?:return 0

        var score = 0

        if (rs.next()){
            score = rs.getInt("score")
        }

        rs.close()
        mysql.close()

        return score
    }

    fun getScore(mcid:String):Int{

        val uuid = getUUID(mcid)

        val rs = mysql.query("select score from player_data where uuid='$uuid';")?:return 0

        var score = 0

        if (rs.next()){
            score = rs.getInt("score")
        }

        rs.close()
        mysql.close()

        return score
    }

    fun getScoreRanking(page : Int): MutableList<Pair<String, Int>> {

        val list = mutableListOf<Pair<String,Int>>()

        val rs = mysql.query("select score from player_data order by score desc limit 10 offset ${(page*10)-10}")?:return mutableListOf(Pair("",0))

        while (rs.next()){
            list.add(Pair(rs.getString("mcid"),rs.getInt("score")))
        }

        return list
    }


    fun giveScore(mcid:String,amount:Int,reason:String,issuer:CommandSender): Boolean {

        val uuid = getUUID(mcid)?:return false

        mysql.execute("update player_data set score=score+${amount} where uuid='$uuid';")

        mysql.execute("INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " + "VALUES ('$mcid', '$uuid', $amount, '[give]:$reason','${issuer.name}',${getScore(uuid)}, now())")

        return true
    }

    fun setScore(mcid:String,amount:Int,reason:String,issuer:CommandSender): Boolean {

        val uuid = getUUID(mcid)?:return false

        mysql.execute("update player_data set score=$amount where uuid='$uuid';")

        mysql.execute("INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " + "VALUES ('$mcid', '$uuid', $amount, '[set]:$reason', '${issuer.name}',${getScore(uuid)}, now())")

        return true
    }

    fun canThank(uuid: UUID):Boolean{
        val rs = mysql.query("select date from score_log where uuid='$uuid' and note='[give]:Thankした' ORDER BY date DESC LIMIT 1;")?:return true

        var ret = false

        if (rs.next()){

            val data = Calendar.getInstance()
            data.time = rs.getDate("date")
            data.add(Calendar.HOUR_OF_DAY,24)

            if (Date().after(data.time)){
                ret= true
            }
        } else {
            ret = true
        }

        rs.close()
        mysql.close()

        return ret
    }

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

    fun getScoreLog(mcid:String,page:Int): MutableList<ScoreLog>{

        val rs = mysql.query("select * from score_log where uuid='${getUUID(mcid)}' order by id desc Limit 10 offset ${(page)*10};")?:return Collections.emptyList()

        val list = mutableListOf<ScoreLog>()

        while (rs.next()){

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

    class ScoreLog{

        var score = 0.0
        var note = ""
        var dateFormat = ""

    }

}