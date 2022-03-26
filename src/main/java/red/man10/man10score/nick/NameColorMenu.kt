package red.man10.man10score.nick

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import red.man10.man10score.Man10Score.Companion.plugin
import red.man10.man10score.nick.NameColorData.boldData
import red.man10.man10score.nick.NameColorData.colorList
import red.man10.man10score.nick.NameColorData.italicData

object NameColorMenu : Listener{

    private val userCache = HashMap<Player,Cache>()

    //1
    fun openColorMenu(p:Player){

        val inv = Bukkit.createInventory(null,27, Component.text("§a§lMCIDの色を変える"))

        colorList.forEach {

            val icon = ItemStack(it.icon)
            val meta = icon.itemMeta
            meta.displayName(Component.text(it.colorCode+it.colorName))
            meta.lore= mutableListOf("§f表示例:${it.colorCode}${p.name}","§f必要チケット:${it.requireTicket}枚","§f解放条件:スコア${it.requireScore}以上")
            setID(meta,it.colorCode)
            icon.itemMeta = meta
            inv.addItem(icon)
        }

        val reset = ItemStack(Material.WHITE_DYE)
        val meta = reset.itemMeta
        meta.displayName(Component.text("§f§lカラーコードをリセットする"))
        meta.lore= mutableListOf("§c§l[注意]リセットしてもチケットは返って来ません！")
        setID(meta,"reset")
        reset.itemMeta = meta
        inv.addItem(reset)

        p.openInventory(inv)

        val c = userCache[p]?:Cache()
        c.openingMenuID = 1
        userCache[p] = c

    }

    //2
    fun openSubMenu(p:Player){

        val inv = Bukkit.createInventory(null,27,"§a§lサブオプション")

        val c = userCache[p]?:Cache()

        val bold = ItemStack(Material.QUARTZ)
        val bMeta = bold.itemMeta
        bMeta.setCustomModelData(66)
        bMeta.displayName(Component.text("§f§l太字"))
        bMeta.lore = mutableListOf(
            "§f表示例:${c.code}${if (c.isBold)"§l" else ""}${if (c.isItalic)"§o" else ""}${p.name}"
            ,"§f必要チケット:${boldData.second}枚","§f解放条件:スコア${boldData.first}以上")
        setID(bMeta,"bold")
        bold.itemMeta = bMeta

        val italic = ItemStack(Material.QUARTZ)
        val iMeta = italic.itemMeta
        iMeta.setCustomModelData(73)
        iMeta.displayName(Component.text("§f§o斜体"))
        iMeta.lore = mutableListOf(
            "§f表示例:${c.code}${if (c.isBold)"§l" else ""}${if (c.isItalic)"§o" else ""}${p.name}"
            ,"§f必要チケット:${italicData.second}枚","§f解放条件:スコア${italicData.first}以上")
        setID(iMeta,"italic")
        italic.itemMeta = iMeta

        val decide = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val dMeta = decide.itemMeta
        dMeta.displayName(Component.text("§a§l確定"))
        dMeta.lore= mutableListOf(
            "§f表示例:${c.code}${if (c.isBold)"§l" else ""}${if (c.isItalic)"§o" else ""}${p.name}")
        setID(dMeta,"decide")
        decide.itemMeta = dMeta

        inv.setItem(12,bold)
        inv.setItem(14,italic)
        inv.setItem(22,decide)

        p.openInventory(inv)

        c.openingMenuID = 2
        userCache[p] = c

    }

    //3
    fun openCheckMenu(p:Player){

        val c = userCache[p]?:Cache()
        c.openingMenuID = 3
        userCache[p] = c

        val data = NameColorData.ColorData.getColorData(c.code)!!

        var ticket = data.requireTicket

        if (c.isBold){ticket+= boldData.second}
        if (c.isItalic){ticket+= italicData.second}

        val inv = Bukkit.createInventory(null,27, Component.text("§a§l最終確認"))

        val decide = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val dMeta = decide.itemMeta
        dMeta.displayName(Component.text("§a§l確定"))
        dMeta.lore= mutableListOf(
            "§f表示例:${c.code}${if (c.isBold)"§l" else ""}${if (c.isItalic)"§o" else ""}${p.name}",
            "§f消費チケット:${ticket}",
            "§a§l消費したチケットは戻って来ません！"
        )
        setID(dMeta,"decide")
        decide.itemMeta = dMeta


        val cancel = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val cMeta = cancel.itemMeta
        cMeta.displayName(Component.text("§c§lキャンセル"))
        setID(cMeta,"cancel")
        cancel.itemMeta = cMeta

        //decide
        intArrayOf(0,1,2,9,10,11,18,19,20).forEach {
            inv.setItem(it,decide)
        }

        //cancel
        intArrayOf(6,7,8,15,16,17,24,25,26).forEach {
            inv.setItem(it,cancel)
        }

        p.openInventory(inv)
    }

    //4
    fun openResetMenu(p:Player){

        val inv = Bukkit.createInventory(null,27, Component.text("§a§l最終確認"))

        val decide = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val dMeta = decide.itemMeta
        dMeta.displayName(Component.text("§a§l確定"))
        dMeta.lore= mutableListOf(
            "§a§l表示名のリセット",
            "§a§l消費したチケットは戻って来ません！"
        )
        setID(dMeta,"decide")
        decide.itemMeta = dMeta


        val cancel = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val cMeta = cancel.itemMeta
        cMeta.displayName(Component.text("§c§lキャンセル"))
        setID(cMeta,"cancel")
        cancel.itemMeta = cMeta

        //decide
        intArrayOf(0,1,2,9,10,11,18,19,20).forEach {
            inv.setItem(it,decide)
        }

        //cancel
        intArrayOf(6,7,8,15,16,17,24,25,26).forEach {
            inv.setItem(it,cancel)
        }

        p.openInventory(inv)

        val c = userCache[p]?:Cache()
        c.openingMenuID = 4
        userCache[p] = c

    }


    ///////////////////////////EVENT


    @EventHandler
    fun clickEvent(e:InventoryClickEvent){

        val p = e.whoClicked
        if (p !is Player)return
        val c = userCache[p]?:return

        val item = e.currentItem?:return

        val id = getID(item)

        e.isCancelled = true

        if (id == "")return

        p.playSound(p.location, Sound.UI_BUTTON_CLICK,0.1F,1.0F)

        when(c.openingMenuID){

            1 ->{

                if (id=="reset"){
                    openResetMenu(p)
                    return
                }

                c.code = id
                userCache[p] = c
                openSubMenu(p)

            }

            2 ->{

                if (id=="bold"){
                    c.isBold = !c.isBold
                    userCache[p] = c
                    openSubMenu(p)
                    return
                }

                if (id=="italic"){
                    c.isItalic = !c.isItalic
                    userCache[p] = c
                    openSubMenu(p)
                    return
                }

                if (id=="decide"){
                    openCheckMenu(p)
                    return
                }

            }

            3 ->{

                if (id=="decide"){
                    p.closeInventory()
                    NameColorData.setColor(p,c.code,c.isBold,c.isItalic)
                    userCache.remove(p)
                    return
                }

                if (id=="cancel"){
                    p.closeInventory()
                    userCache.remove(p)
                    return
                }
            }

            4 ->{
                if (id=="decide"){
                    p.closeInventory()
                    NameColorData.resetColor(p)
                    userCache.remove(p)
                    return
                }

                if (id=="cancel"){
                    p.closeInventory()
                    userCache.remove(p)
                    return
                }

            }

        }

    }


    fun setID(meta: ItemMeta, value:String){
        meta.persistentDataContainer.set(NamespacedKey(plugin,"id"), PersistentDataType.STRING,value)
    }

    fun getID(itemStack: ItemStack):String{
        return itemStack.itemMeta?.persistentDataContainer?.get(NamespacedKey(plugin,"id"), PersistentDataType.STRING)
            ?:""
    }

    class Cache{
        var code = ""
        var isBold = false
        var isItalic = false
        var openingMenuID = -1
    }



}