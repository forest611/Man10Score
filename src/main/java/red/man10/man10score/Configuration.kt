package red.man10.man10score

object Configuration {

    var thankAmount = 5
    var fuckAmount = -20

    fun loadConfig(){
        Man10Score.plugin.reloadConfig()

        val config = Man10Score.plugin.config

        thankAmount = config.getInt("thankAmount")
        fuckAmount = config.getInt("fuckAmount")
    }

    fun saveConfig(){
        Man10Score.plugin.config.set("thankAmount", thankAmount)
        Man10Score.plugin.config.set("fuckAmount", fuckAmount)
        Man10Score.plugin.saveConfig()
    }



}