package io.github.openminigameserver.nickarcade.moderation.plugin.events.impl

import io.github.openminigameserver.nickarcade.moderation.core.invite.PlayerInvite
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerInviteAddEvent(val invite: PlayerInvite, val inviteeName: String, val invitedName: String) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
