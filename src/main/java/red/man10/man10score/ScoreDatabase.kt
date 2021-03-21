package red.man10.man10score

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


    fun giveScore(mcid:String,uuid: UUID,amount:Int,reason:String,issuer:Player){

        mysql.execute("update player_data set score=score+$amount where uuid='$uuid';")

        mysql.execute("INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " +
                "VALUES ('$mcid', '$uuid', $amount, '[give]:$reason',${getScore(uuid)}, '$issuer', now())")

    }

    fun setScore(mcid:String,uuid: UUID,amount:Int,reason:String,issuer:Player){

        mysql.execute("update player_data set score=$amount where uuid='$uuid';")

        mysql.execute("INSERT INTO score_log (mcid, uuid, score, note, issuer,now_score, date) " +
                "VALUES ('$mcid', '$uuid', $amount, '[set]:$reason',${getScore(uuid)}, '$issuer', now())")

    }
}