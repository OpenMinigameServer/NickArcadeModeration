package io.github.openminigameserver.nickarcade.moderation.plugin.events

import io.github.openminigameserver.nickarcade.moderation.core.invite.InviteManager
import io.github.openminigameserver.nickarcade.plugin.extensions.event
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

fun handlePreJoinEvent() {
    event<AsyncPlayerPreLoginEvent>(forceBlocking = true, eventPriority = EventPriority.LOW) {
        if (!InviteManager.hasPlayerReceivedInvite(uniqueId)) {
            loginResult = AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST
            kickMessage(
                text {
                    it.append(
                        text("You are not allowed to join this server!", NamedTextColor.RED)
                            .append(newline())
                    )
                    it.append(newline())
                    it.append(
                        text("Reason: ", NamedTextColor.GRAY).append(
                            text(
                                "You have not received an invite to play on this server.",
                                NamedTextColor.WHITE
                            )
                        ).append(newline())
                    )
                }
            )
        }
    }
}