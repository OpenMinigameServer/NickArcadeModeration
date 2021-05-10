package io.github.openminigameserver.nickarcade.moderation.core.invite

import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.data.sender.ArcadeSender
import io.github.openminigameserver.nickarcade.core.database
import kotlinx.datetime.Clock
import org.geysermc.floodgate.api.FloodgateApi
import org.litote.kmongo.eq
import java.util.*

object InviteManager {

    private val inviteCollection by lazy {
        database.getCollection<PlayerInvite>("invites")
    }

    suspend fun getPlayerInvite(invited: UUID): PlayerInvite? {
        return inviteCollection.findOne(PlayerInvite::invited eq invited)
    }

    suspend fun getPlayerInvitesByPlayer(inviter: UUID): List<PlayerInvite> {
        return inviteCollection.find(PlayerInvite::inviter eq inviter).toList()
    }

    private suspend fun countPlayerInvitesByPlayer(inviter: UUID): Long {
        return inviteCollection.countDocuments(PlayerInvite::inviter eq inviter)
    }

    suspend fun addPlayerInvite(invited: UUID, inviter: UUID): PlayerInvite {
        val result = PlayerInvite(inviter, invited).apply { timestamp = Clock.System.now() }
        inviteCollection.insertOne(result)
        return result
    }

    suspend fun removePlayerInvite(invited: UUID): Boolean {
        return inviteCollection.deleteOne(PlayerInvite::invited eq invited).deletedCount > 0
    }

    suspend fun hasPlayerReceivedInvite(invited: UUID): Boolean {
        return getPlayerInvite(invited) != null || (FloodgateApi.getInstance().isFloodgateId(invited))
    }

    private const val inviteLimit = 1
    suspend fun canInvitePlayers(arcadeSender: ArcadeSender): Boolean {
        return canInviteInfinitePlayers(arcadeSender) || countPlayerInvitesByPlayer(arcadeSender.uuid) < inviteLimit
    }

    private fun canInviteInfinitePlayers(arcadeSender: ArcadeSender) =
        arcadeSender.hasAtLeastRank(HypixelPackageRank.ADMIN, true)

    suspend fun getRemainingInvites(arcadeSender: ArcadeSender): Long {
        if (canInviteInfinitePlayers(arcadeSender)) return -1
        return inviteLimit - countPlayerInvitesByPlayer(arcadeSender.uuid)
    }
}