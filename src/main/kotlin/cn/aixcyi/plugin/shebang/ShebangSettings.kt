package cn.aixcyi.plugin.shebang

import com.intellij.openapi.components.*

/**
 * 插件设置。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
@Service(Service.Level.APP)
@State(name = Zoo.XmlComponent.SHEBANG, storages = [Storage(Zoo.PLUGIN_STORAGE)])
class ShebangSettings : SimplePersistentStateComponent<ShebangSettings.State>(State()) {

    companion object {
        const val DELIMITER = "|"
        const val PRESET_FILE_SUFFIXES = "bash|sh|zsh|py|pl|java|groovy|gy|gant"

        /**
         * 插件预置的所有 shebang。
         */
        val PRESET_SHEBANGS = listOf(
            "/bin/bash",
            "/bin/sh",
            "/bin/sh -",
            "/usr/bin/env bash",
            "/usr/bin/env python",
            "/usr/bin/env python3",
            "/usr/bin/env perl",
            "/usr/bin/env ruby",
            "/usr/bin/python",
            "/usr/bin/python3",
            "/usr/local/bin/python",
            "/usr/local/bin/python3",
            "/usr/local/bin/ruby",
            "./venv/Scripts/python.exe",
        )

        fun getInstance() = service<ShebangSettings>()
    }

    class State : BaseState() {
        var myShebangs by property(PRESET_SHEBANGS) { it == PRESET_SHEBANGS }
        var myFileSuffixes by property(PRESET_FILE_SUFFIXES) { it == PRESET_FILE_SUFFIXES }

        fun getFileSuffixes() = myFileSuffixes.split(DELIMITER)
    }
}