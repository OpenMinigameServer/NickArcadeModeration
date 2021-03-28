plugins {
    id("io.github.openminigameserver.arcadiumgradle") version "1.0-SNAPSHOT"
}

nickarcade {
    name = "Moderation"
    depends("Chat")
}