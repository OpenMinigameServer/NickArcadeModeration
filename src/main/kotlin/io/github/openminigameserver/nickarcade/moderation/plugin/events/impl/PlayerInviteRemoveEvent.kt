package io.github.openminigameserver.nickarcade.moderation.plugin.events.impl

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class PlayerInviteRemoveEvent(val invitedUUID: UUID, val inviteeName: String, val invitedName: String) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}