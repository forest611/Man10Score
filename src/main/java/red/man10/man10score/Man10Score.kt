package red.man10.man10score

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Man10Score : JavaPlugin() , Listener{
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

            }

            "score" ->{

            }

            "thank" ->{

            }

            "fuck" ->{

            }

        }

        return false
    }
}