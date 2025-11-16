plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.jakubjirak.ansilog"
version = "2.3.1"

repositories { mavenCentral() }

intellij {
    version.set("2025.2")
    type.set("IC")
    plugins.set(listOf())
}

tasks.buildSearchableOptions { enabled = false }

// Marketplace metadata (configure publishPlugin task)
tasks.publishPlugin {
    token.set(System.getenv("IJ_TOKEN"))
}

tasks.patchPluginXml {
    sinceBuild.set("252")
    untilBuild.set("")
}

tasks.withType<JavaCompile> { options.release.set(17) }

// Plugin Verifier target IDEs
tasks.runPluginVerifier {
    ideVersions.set(listOf("2025.2", "2024.2", "2024.1"))
}


