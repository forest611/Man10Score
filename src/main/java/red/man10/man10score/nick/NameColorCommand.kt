package red.man10.man10score.nick

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.man10score.Man10Score
import red.man10.man10score.ScoreDatabase

object NameColorCommand : CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (label!="namecolor")return false
        if (sender !is Player)return false

        if (!sender.hasPermission("man10score.namecolor")){
            Man10Score.sendMessage(sender,"§cあなたはカラーネーム機能を使うことができません！")
            return true
        }

        if (args.isEmpty()){
            NameColorMenu.openColorMenu(sender)
            return true
        }

        if (args[0]=="reload"){
            if (!sender.hasPermission("man10score.op"))return false
            NameColorData.loadColorList()
            return true
        }

        if (args[0]=="ticket"){
            if (!sender.hasPermission("man10score.op"))return false
            sender.inventory.addItem(NameColorData.ticketItem)
            return true
        }

        return false
    }
}