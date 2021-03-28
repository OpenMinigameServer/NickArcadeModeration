package io.github.openminigameserver.nickarcade.moderation.core.invite

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.datetime.Instant
import java.util.*

data class PlayerInvite(val inviter: UUID, val invited: UUID, @JsonProperty("timestampValue") var timestamp: Instant = Instant.DISTANT_PAST)