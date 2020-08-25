package net.drabc.INSWatchDog

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.drabc.INSWatchDog.Vars.Var
import net.drabc.INSWatchDog.RconClient.RconClient
import net.drabc.INSWatchDog.Runnable.*
import net.drabc.INSWatchDog.Setting.SettingBase

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking { start() }
        }

        private suspend fun start() = coroutineScope {
            Var.settingBase = Utility.parseJson("/config.json") as SettingBase
            Var.logger.log("配置文件读取完毕！", Logger.LogType.WARN)
            try {
                val rconClient = RconClient()
                rconClient.connect(
                    Var.settingBase.rcon.ipAddress,
                    Var.settingBase.rcon.rconPort,
                    Var.settingBase.rcon.passWd
                )
                Utility.getMapList(rconClient)
                launch {
                    LogWatcher().run(rconClient)
                }
                launch {
                    Message().run(rconClient)
                }
                launch {
                    SyncPlayerList().run(rconClient)
                }
                if (Var.settingBase.difficult.enable) {
                    launch {
                        DifficultTweak().run(rconClient)
                    }
                }
                if (Var.settingBase.soloBot.enable) {
                    launch {
                        SoloBotTweak().run(rconClient)
                    }
                }
            }catch (e: Exception){
                Var.logger.exception(e)
            }
        }
    }
}