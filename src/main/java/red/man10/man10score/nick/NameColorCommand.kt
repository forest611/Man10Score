package red.man10.man10score.nick

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.man10score.Man10Score
import red.man10.man10score.ScoreDatabase

object NameColorCommand : CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (label!="nickcolor")return false
        if (sender !is Player)return false

        if (!sender.hasPermission("man10score.nickcolor")){
            Man10Score.sendMessage(sender,"")
            return true
        }

        val score = ScoreDatabase.getScore(sender.uniqueId)



        return false
    }
}