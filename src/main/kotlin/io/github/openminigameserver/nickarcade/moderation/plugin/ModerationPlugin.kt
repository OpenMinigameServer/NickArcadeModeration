package io.github.openminigameserver.nickarcade.moderation.plugin

import io.github.openminigameserver.nickarcade.core.commandAnnotationParser
import io.github.openminigameserver.nickarcade.moderation.plugin.commands.InviteCommands
import io.github.openminigameserver.nickarcade.moderation.plugin.events.handlePreJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class ModerationPlugin : JavaPlugin() {
    override fun onEnable() {
        handlePreJoinEvent()
        commandAnnotationParser.parse(InviteCommands)
    }
}