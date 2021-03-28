package io.github.openminigameserver.nickarcade.moderation.plugin.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.ProxiedBy
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.data.sender.ArcadeSender
import io.github.openminigameserver.nickarcade.core.data.sender.misc.ArcadeWatcherSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.moderation.core.invite.InviteManager
import io.github.openminigameserver.nickarcade.moderation.core.invite.InviteManager.getRemainingInvites
import io.github.openminigameserver.nickarcade.moderation.plugin.events.impl.PlayerInviteAddEvent
import io.github.openminigameserver.nickarcade.moderation.plugin.events.impl.PlayerInviteRemoveEvent
import io.github.openminigameserver.nickarcade.plugin.extensions.command
import io.github.openminigameserver.nickarcade.plugin.helper.commands.RequiredRank
import io.github.openminigameserver.profile.ProfileApi
import io.github.openminigameserver.profile.models.Profile
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import java.util.*
import kotlin.time.seconds

object InviteCommands {

    @CommandMethod("invite remove <name>")
    @ProxiedBy("removeinvite")
    @RequiredRank(HypixelPackageRank.ADMIN)
    fun removePlayerInviteByName(sender: ArcadeSender, @Argument("name") name: String) =
        command(sender, HypixelPackageRank.ADMIN) {
            val findResult = findPlayerByName(sender, name) ?: return@command
            val resultingPlayer = findResult.first
            val invitedUUID = findResult.second

            if (InviteManager.removePlayerInvite(invitedUUID)) {
                PlayerInviteRemoveEvent(invitedUUID, getActualName(sender), name).callEvent()
                sender.audience.sendMessage(text("Invite for ${resultingPlayer.name!!} was removed successfully.", GREEN))
                ArcadeWatcherSender.sendMessage(
                    text {
                        val color = RED
                        it.append(text("[", color))
                        it.append(text(sender.getChatName(true, colourPrefixOnly = true)))
                        it.append(text("] removed invite for [", color))
                        it.append(text(name, color))
                        it.append(text("]", color))
                    }
                )
            } else {
                sender.audience.sendMessage(text("Unable to find an invite for player ${resultingPlayer.name!!}. Maybe it was removed already?", RED))
            }

        }

    @CommandMethod("invite <name>")
    fun invitePlayerByName(sender: ArcadeSender, @Argument("name") name: String) = command(sender) {
        if (!InviteManager.canInvitePlayers(sender)) {
            sender.audience.sendMessage(text("You have exceeded your player invite limit!", RED))
            return@command
        }

        val findResult = findPlayerByName(sender, name) ?: return@command
        val resultingPlayer = findResult.first
        val invitedUUID = findResult.second

        if (InviteManager.hasPlayerReceivedInvite(invitedUUID)) {
            sender.audience.sendMessage(text("That player has already been invited to this server!", RED))
            return@command
        }

        val requiresConfirmation = !sender.hasAtLeastRank(HypixelPackageRank.ADMIN, true)
        if (requiresConfirmation && sender is ArcadePlayer) {
            val isConfirmation = !sender.coolDown("player-invite", 10.seconds)
            if (!isConfirmation) {
                sender.audience.sendMessage(text("Please type the command again to confirm this action!", RED))
                val remainingInvites = getRemainingInvites(sender) - 1
                sender.audience.sendMessage(text {
                    it.append(text("Once the invite has been sent, ", RED))
                    if (remainingInvites == 0L) {
                        it.append(text("you will no longer be able to invite more players.", RED))
                    } else {
                        it.append(
                            text(
                                "you will able to invite $remainingInvites more player${if (remainingInvites != 1L) "s" else ""}.",
                                RED
                            )
                        )
                    }
                })
                return@command
            }
            sender.data.cooldowns.remove("player-invite")
        }
        val invite = InviteManager.addPlayerInvite(invitedUUID, sender.uuid)

        PlayerInviteAddEvent(
            invite,
            getActualName(sender),
            name
        ).callEvent()

        ArcadeWatcherSender.sendMessage(
            text {
                val color = GREEN
                it.append(text("[", color))
                it.append(text(sender.getChatName(true, colourPrefixOnly = true)))
                it.append(text("] invited [", color))
                it.append(text(name, color))
                it.append(text("]", color))
            }
        )

        sender.audience.sendMessage(
            text {
                it.append(text("You have successfully invited player ", GREEN))
                it.append(text(resultingPlayer.name!!, GOLD))
                it.append(text(" to the server!", GREEN))
                if (requiresConfirmation) {
                    it.append(newline())
                    it.append(text("This action can only be reverted by an administrator.", YELLOW))
                }
            }
        )
    }

    private fun getActualName(sender: ArcadeSender) =
        if (sender is ArcadePlayer) sender.actualDisplayName else sender.displayName

    private suspend fun findPlayerByName(sender: ArcadeSender, name: String): Pair<Profile, UUID>? {
        sender.audience.sendMessage(text("Processing your request..", GRAY))

        val resultingPlayer = ProfileApi.getProfileByName(name)
        val invitedUUID = resultingPlayer?.uuid
        if (invitedUUID == null) {
            sender.audience.sendMessage(text("That player does not exist!", RED))
            return null
        }
        return resultingPlayer to invitedUUID
    }

}