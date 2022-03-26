package red.man10.man10score.nick

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import red.man10.man10score.Man10Score.Companion.plugin
import red.man10.man10score.Man10Score.Companion.sendMessage
import red.man10.man10score.ScoreDatabase

object NameColorData {

    val colorList = mutableListOf<ColorData>()
    lateinit var boldData: Pair<Int,Int>//score:ticket
    lateinit var italicData: Pair<Int,Int>//score:ticket

    val ticketItem = ItemStack(Material.NETHERITE_INGOT)

    init {
        val meta = ticketItem.itemMeta
        meta.setCustomModelData(2)
        meta.setDisplayName("§a§lネームカラー§6§lチケット")
        meta.lore = mutableListOf("§fマインクラフトの表示名の色を変えたい時に","交換するチケット")
        meta.addEnchant(Enchantment.LUCK,0,false)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        ticketItem.itemMeta = meta
    }

    fun loadColorList(){

        plugin.reloadConfig()

        val list = plugin.config.getStringList("colorData")

        list.forEach {
            val data = ColorData()
            val value = it.split(";")//ColorName;Code;icon;score;ticket
            data.colorName=value[0]
            data.colorCode=value[1]
            data.icon=Material.valueOf(value[2])
            data.requireScore=value[3].toIntOrNull()?:0
            data.requireTicket=value[4].toIntOrNull()?:0
            colorList.add(data)
        }

        boldData = Pair(plugin.config.getInt("bold.score"),plugin.config.getInt("bold.ticket"))
        italicData = Pair(plugin.config.getInt("italic.score"),plugin.config.getInt("italic.ticket"))

    }

    fun setColor(p:Player,code:String,isBold:Boolean,isItalic:Boolean){
        val data = ColorData.getColorData(code)
        if (data == null){
            sendMessage(p,"エラー:${code} 運営に報告してください")
            return
        }

        val score = ScoreDatabase.getScore(p.uniqueId)

        if (data.requireScore>score){
            sendMessage(p,"§c${data.colorName}を使用するために必要なスコアが足りません！")
            return
        }
        if (isBold && boldData.first>score){
            sendMessage(p,"§c太字を使用するために必要なスコアが足りません！")
            return
        }
        if (isItalic && italicData.first>score){
            sendMessage(p,"§c斜体を使用するために必要なスコアが足りません！")
            return
        }

        var ticket = data.requireTicket

        if (isBold){ticket+= boldData.second}
        if (isItalic){ticket+= italicData.second}

        val handItem = p.inventory.itemInMainHand

        if (!handItem.isSimilar(ticketItem)){
            sendMessage(p,"利き手にチケットを持ってください！")
            return
        }

        if (handItem.amount<ticket){
            sendMessage(p,"チケットの枚数が足りません！(必要枚数:${ticket}枚)")
            return
        }

        handItem.amount = handItem.amount-ticket

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"/nick ${p.name} ${code}${if (isBold)"§l" else ""}${if (isItalic)"§o" else ""}${p.name}")

        sendMessage(p,"名前の表示名を変更しました！")

        Thread{ ScoreDatabase.giveScore(p.name,0,"表示名の変更",Bukkit.getConsoleSender()) }.start()
    }

    class ColorData{
        var colorName = ""
        var colorCode = ""
        var icon : Material = Material.INK_SAC
        var requireScore = 0
        var requireTicket = 0

        companion object{
            fun getColorData(code:String): ColorData? {
                val ret = colorList.filter { it.colorCode==code }
                if (ret.size>1)Bukkit.getLogger().warning("カラーコード\'${code}\'が${ret.size}個設定されています")
                if (ret.isEmpty())return null
                return ret[0]
            }
        }
    }

}