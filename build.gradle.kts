plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.jakubjirak.ansilog"
version = "0.1.1"

repositories { mavenCentral() }

intellij {
    version.set("2024.2")
    type.set("IC")
    plugins.set(listOf())
}

// Marketplace metadata (configure publishPlugin task)
tasks.publishPlugin {
    token.set(System.getenv("IJ_TOKEN"))
}

tasks.patchPluginXml {
    sinceBuild.set("242")
    untilBuild.set("242.*")
}

tasks.withType<JavaCompile> { options.release.set(17) }

// Plugin Verifier target IDEs
tasks.runPluginVerifier {
    ideVersions.set(listOf("2024.2", "2024.1", "2023.3"))
}


