package red.man10.man10score

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.Executors

class Man10Score : JavaPlugin() , Listener{

    private val es = Executors.newCachedThreadPool()
    private val prefix = "§b[§dMan10Score§b]"


    companion object{
        lateinit var plugin : Man10Score
    }

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        server.pluginManager.registerEvents(this,this)
        plugin = this

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        when(label){

            "mscore" ->{

                if (!sender.hasPermission("man10score.op")){
                    return true
                }

                if (args.isNullOrEmpty()){

                    sendMessage(sender,"§a/mscore give <player> <score> <理由> : 指定ユーザーにスコアを与えます")
                    sendMessage(sender,"§a/mscore take <player> <score> <理由> : 指定ユーザーのスコアを減らします")
                    sendMessage(sender,"§a/mscore set <player> <score> <理由>  : 指定ユーザーのスコアを指定値にします")

                    return true
                }

                val receiverName = args[1]

                es.execute {
                    when(args[0]){

                        "give" ->{

                            if (args.size!=4)return@execute

                            if (ScoreDatabase.giveScore(receiverName,args[2].toInt(),args[3],sender)){
                                sendMessage(sender,"§a${receiverName}に${args[2]}ポイント与えました")

                                Bukkit.getScheduler().runTask(this, Runnable {
                                    broadcast("§a${receiverName}は「${args[3]}」により、${args[2]}ポイント受け取りました。")
                                })
                                return@execute
                            }

                            sendMessage(sender,"§cユーザーが見つかりませんでした")

                        }

                        "take" ->{

                            if (args.size!=4)return@execute

                            if (ScoreDatabase.giveScore(receiverName,-args[2].toInt(),args[3],sender)){
                                sendMessage(sender,"§a${receiverName}から${args[2]}ポイント引きました")
                                Bukkit.getScheduler().runTask(this, Runnable {
                                    broadcast("§c${receiverName}は「${args[3]}」により、${args[2]}ポイント引かれました！。")
                                })

                                return@execute
                            }

                            sendMessage(sender,"§cユーザーが見つかりませんでした")

                        }

                        "set"  ->{

                            if (args.size!=4)return@execute

                            if (ScoreDatabase.setScore(receiverName,args[2].toInt(),args[3],sender)){
                                sendMessage(sender,"§a${receiverName}のスコアを${args[2]}ポイントに設定しました")
                                return@execute
                            }

                            sendMessage(sender,"§cユーザーが見つかりませんでした")

                        }

                        else ->{

                            sendMessage(sender,"${args[0]}のスコア:${ScoreDatabase.getScore(args[0])}")
                        }
                    }
                }

            }

            "score" ->{

                if (!sender.hasPermission("man10score.user")){
                    return true
                }

                if (sender !is Player)return false

                es.execute {
                    showScore(sender)
                }
            }

            "scoreranking"->{
                if (sender is Player){
                    if (!sender.hasPermission("man10score.user")){
                        return true
                    }
                }

                val page = if (args.isEmpty()) 1 else args[0].toIntOrNull()?:1

                var i = (page*10)-9

                sender.sendMessage("§6§k§lXX§a§lスコアトップ${page*10}§6§k§lXX")

                es.execute {
                    for (data in ScoreDatabase.getScoreRanking(page)){
                        sender.sendMessage("§7§l${i}.§b§l${data.first} : §a§l${data.second}ポイント")
                        i++
                    }
                }


            }

            "thank" ->{

                if (!sender.hasPermission("man10score.user")){
                    return true
                }

                if (sender !is Player)return false

                if (args.isEmpty())return false

                val receiver = Bukkit.getPlayer(args[0])

                if (!ScoreDatabase.canThank(sender.uniqueId)){
                    sendMessage(sender,"§cクールダウンによりThankは使えません")
                    return true
                }

                if (receiver == null){
                    sendMessage(sender,"§c現在オフラインのプレイヤーです！")
                    return true
                }

                if (sender == receiver){
                    sendMessage(sender,"§c自分にThankはできません！")
                    return true
                }

                broadcast("§a${sender.name}は${receiver.name}に§d感謝しました")
                sendMessage(receiver,"§aあなたは${sender.name}から§d感謝されました")

                es.execute {
                    ScoreDatabase.giveScore(receiver.name,5,"Thankされた",sender)
                    ScoreDatabase.giveScore(sender.name,0,"Thankした",sender)
                    showScore(receiver)
                }

            }

            "fuck" ->{

                if (!sender.hasPermission("man10score.user")){
                    return true
                }

                if (sender !is Player)return false

                if (args.isEmpty())return false

                val receiver = Bukkit.getPlayer(args[0])

                if (receiver == null){
                    sendMessage(sender,"§c相手がオフラインです！")
                    return true
                }

                broadcast("§c§l${sender.name}は${receiver.name}に「ファック！」といったことにより、20ポイント引かれました！")

                es.execute {
                    ScoreDatabase.giveScore(receiver.name,0,"FUCKされた",sender)
                    ScoreDatabase.giveScore(sender.name,-20,"FUCKした",sender)
//                    showScore(receiver)
                }

            }

        }

        return false
    }

    @EventHandler
    fun loginEvent(e:PlayerLoginEvent){

        val p = e.player

        es.execute {
            Thread.sleep(500)
            showScore(p)
        }
    }

    private fun showScore(p:Player){
        sendMessage(p,"§a${p.name}のスコア：${ScoreDatabase.getScore(p.uniqueId)}ポイント")
    }

    private fun sendMessage(p:CommandSender,text:String){
        p.sendMessage(prefix+text)
    }

    private fun broadcast(text: String){
        Bukkit.broadcastMessage(prefix+text)
    }
}