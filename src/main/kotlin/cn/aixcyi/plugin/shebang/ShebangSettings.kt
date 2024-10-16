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

        const val DELIMITER = "|"
        const val PRESET_FILE_SUFFIXES = "bash|sh|zsh|py|pl|pm|rb"
        const val FILETYPE_SHELL_SCRIPT = "com.intellij.sh.ShFileType"

        fun getInstance(): ShebangSettings = service()
    }

    class State : BaseState() {
        var myShebangs by property(PRESET_SHEBANGS) { it == PRESET_SHEBANGS }
        var myFileSuffixes by property(PRESET_FILE_SUFFIXES) { it == PRESET_FILE_SUFFIXES }
        var myAbsChooserBase by property("", String::isEmpty)
        var myChooserSuffixes by property("", String::isEmpty)
    }
}