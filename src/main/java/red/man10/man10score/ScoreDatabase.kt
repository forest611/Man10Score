package red.man10.man10score

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object ScoreDatabase {

    private val mysql = MySQLManager(Man10Score(),"Score")

    fun getUUID(name:String):UUID?{

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


    fun giveScore(mcid:String,amount:Int,reason:String,issuer:CommandSender): Boolean {

        val uuid = getUUID(mcid)?:return false

        mysql.execute("update player_data set score=score+$amount where uuid='$uuid';")

        mysql.execute("INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " +
                "VALUES ('$mcid', '$uuid', $amount, '[give]:$reason',${getScore(uuid)}, '$issuer', now())")

        return true
    }

    fun setScore(mcid:String,amount:Int,reason:String,issuer:CommandSender): Boolean {

        val uuid = getUUID(mcid)?:return false

        mysql.execute("update player_data set score=$amount where uuid='$uuid';")

        mysql.execute("INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " +
                "VALUES ('$mcid', '$uuid', $amount, '[set]:$reason',${getScore(uuid)}, '${issuer.name}', now())")

        return true
    }

    fun canThank(uuid: UUID):Boolean{
        val rs = mysql.query("select * from score_log where uuid=$uuid and reason=[give]:Thankした;")?:return true

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
}