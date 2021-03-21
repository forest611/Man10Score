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
    private val prefix = "§b[§aMan10Score§b]"

    override fun onEnable() {
        // Plugin startup logic

        server.pluginManager.registerEvents(this,this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        when(label){

            "mscore" ->{

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

                            if (ScoreDatabase.giveScore(receiverName,-args[2].toInt(),args[3],sender)){
                                sendMessage(sender,"§a${receiverName}に${args[2]}ポイント与えました")

                                Bukkit.getScheduler().runTask(this, Runnable {
                                    broadcast("§a${receiverName}は「${args[3]}」により、${args[2]}ポイント受け取りました。")
                                })
                                return@execute
                            }

                            sendMessage(sender,"§cユーザーが見つかりませんでした")

                        }

                        "take" ->{

                            if (ScoreDatabase.giveScore(receiverName,args[2].toInt(),args[3],sender)){
                                sendMessage(sender,"§a${receiverName}から${args[2]}ポイント引きました")
                                return@execute
                            }

                            sendMessage(sender,"§cユーザーが見つかりませんでした")

                        }

                        "set"  ->{

                            if (ScoreDatabase.setScore(receiverName,args[2].toInt(),args[3],sender)){
                                sendMessage(sender,"§a${receiverName}のスコアを${args[2]}ポイントに設定しました")
                                return@execute
                            }

                            sendMessage(sender,"§cユーザーが見つかりませんでした")

                        }
                    }
                }

            }

            "score" ->{

                if (sender !is Player)return false

                es.execute {
                    showScore(sender)
                }
            }

            "thank" ->{

                if (sender !is Player)return false

                if (args.isEmpty())return false

                val receiver = Bukkit.getPlayer(args[0])?:return false

                if (!ScoreDatabase.canThank(receiver.uniqueId)){
                    sendMessage(sender,"§cクールダウンによりThankはできません")
                    return true
                }

                sendMessage(sender,"§aあなたは${receiver.name}に§d感謝しました")
                sendMessage(receiver,"§aあなたは${sender.name}から§d感謝されました")

                es.execute {
                    ScoreDatabase.giveScore(receiver.name,5,"Thankされた",sender)
                    ScoreDatabase.giveScore(sender.name,0,"Thankした",sender)
                    showScore(receiver)
                }

            }

            "fuck" ->{

                if (sender !is Player)return false

                if (args.isEmpty())return false

                val receiver = Bukkit.getPlayer(args[0])?:return false

                broadcast("§c§l${sender.name}は${receiver.name}に「Fuck！」といったことにより、20ポイント引かれました！")

                es.execute {
                    ScoreDatabase.giveScore(receiver.name,0,"FUCKされた",sender)
                    ScoreDatabase.giveScore(sender.name,-20,"FUCKした",sender)
                    showScore(receiver)
                }

            }

        }

        return false
    }

    @EventHandler
    fun loginEvent(e:PlayerLoginEvent){

        val p = e.player

        es.execute {
            showScore(p)
        }
    }

    private fun showScore(p:Player){
        sendMessage(p,"§a現在の${p.name}のスコア：${ScoreDatabase.getScore(p.uniqueId)}ポイント")
    }

    private fun sendMessage(p:CommandSender,text:String){
        p.sendMessage(prefix+text)
    }

    private fun broadcast(text: String){
        Bukkit.broadcastMessage(prefix+text)
    }
}