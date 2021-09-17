package red.man10.man10score

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class Man10Score : JavaPlugin() , Listener{

    private val es = Executors.newCachedThreadPool()
    private val prefix = "§b[§dMan10Score§b]"


    companion object{
        lateinit var plugin : Man10Score
        val freezePlayerList = mutableSetOf<String>()
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
                    sendMessage(sender,"§a/mscore show <player> : 指定ユーザーのスコアを確認します")
                    sendMessage(sender,"§a/mscore log <player> : 指定ユーザーのスコアのログを確認します")

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

                        "show" ->{

                            if (args.size!=2)return@execute
                            sendMessage(sender,"§a${receiverName}のスコアは${ScoreDatabase.getScore(receiverName)}です")

                        }

                        "log" ->{

                            val page = if (args.size >= 2) args[2].toIntOrNull()?:0 else 0

                            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                                val list = ScoreDatabase.getScoreLog(receiverName,page)

                                sendMessage(sender,"§d§l===========スコアの履歴==========")
                                for (data in list){
                                    sendMessage(sender,"§e${data.dateFormat} §e§l${data.note} §e${data.score}")
                                }

                                val previous = if (page!=0) {
                                    text("${prefix}§b§l<<==前のページ ").clickEvent(ClickEvent.runCommand("/mscore log ${page-1}"))
                                }else text(prefix)

                                val next = if (list.size == 10){
                                    text("§b§l次のページ==>>").clickEvent(ClickEvent.runCommand("/mscore log ${page+1}"))
                                }else text("")

                                sender.sendMessage(previous.append(next))

                            })

                        }

                        else ->{
                            sendMessage(sender,"§a/mscore give <player> <score> <理由> : 指定ユーザーにスコアを与えます")
                            sendMessage(sender,"§a/mscore take <player> <score> <理由> : 指定ユーザーのスコアを減らします")
                            sendMessage(sender,"§a/mscore set <player> <score> <理由>  : 指定ユーザーのスコアを指定値にします")
                            sendMessage(sender,"§a/mscore show <player> : 指定ユーザーのスコアを確認します")
                            sendMessage(sender,"§a/mscore log <player> : 指定ユーザーのスコアのログを確認します")
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

            "mfreeze"->{

                if (!sender.hasPermission("man10score.op")){
                    return true
                }

                val calendar = Calendar.getInstance()

                val freezeuntil = calendar.time

                when(args[1]){

                    "d" ->addDate(freezeuntil,0,0,args[2].toInt())
                    "h" ->addDate(freezeuntil,0,args[2].toInt(),0)
                    "m" ->addDate(freezeuntil,args[2].toInt(),0,0)
                    "k" ->addDate(freezeuntil,0,0,383512)

                    else -> {
                        sendMessage(sender,"§c§l時間の指定方法が不適切です")
                        return true
                    }

                }

                ScoreDatabase.setFreeze(args[0],freezeuntil)

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

        if(ScoreDatabase.isFrozen(e.player.name)){
            freezePlayerList.add(e.player.name)
        }

    }

    @EventHandler
    fun logoutEvent(e:PlayerQuitEvent){

        if(ScoreDatabase.isFrozen(e.player.name)){
            freezePlayerList.remove(e.player.name)
        }

    }

    @EventHandler
    fun onPlayerMove(e:PlayerMoveEvent){

        if(freezePlayerList.contains(e.player.name)){
            e.isCancelled = true
        }

    }

    @EventHandler
    fun onPlayerInteract(e:PlayerInteractEvent){

        if(freezePlayerList.contains(e.player.name)){
            e.isCancelled = true
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

    private fun addDate(date: Date?, min:Int, hour:Int, day:Int): Date? {

        val calender = Calendar.getInstance()

        calender.time = date?: Date()
        calender.add(Calendar.MINUTE,min)
        calender.add(Calendar.HOUR,hour)
        calender.add(Calendar.DATE,day)

        val time = calender.time

        if (time.time< Date().time){
            return null
        }

        return time
    }

}