import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "2.3.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    `maven-publish`
}

group = "io.github.team-sneakymouse"

// Requires a git checkout with at least one commit (CI and local clones).
version =
    providers.exec {
        workingDir(rootDir)
        commandLine("git", "show", "-s", "--format=%ct:%h", "--abbrev=12", "HEAD")
    }.standardOutput.asText.map { commit ->
        val (timestamp, hash) = commit.trim().split(":", limit = 2)
        val date =
            DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochSecond(timestamp.toLong()))
        "$date-$hash"
    }.get()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    // Internal Team-Sneakymouse / Lords of Minecraft artifacts:
    maven("https://maven.sneakyrp.com/releases")
}

val paperApi = "26.2.build.+"
val mockBukkitVersion = "4.117.0"
/** Keep compileOnly and runServer pluginJars on the same published SneakyUtils. */
val sneakyUtilsVersion = "2026.10.18-657d452bd6f4"

/** Runtime plugin jar for ./gradlew runServer (resolved from maven.sneakyrp.com into the Gradle cache). */
val sneakyUtilsPlugin by configurations.creating

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApi")
    compileOnly("io.github.team-sneakymouse:sneakyutils:$sneakyUtilsVersion")
    // Plugin jar only — do not stage transitive libs (e.g. kotlin-stdlib) as Paper plugins.
    sneakyUtilsPlugin("io.github.team-sneakymouse:sneakyutils:$sneakyUtilsVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.21")

    testImplementation("io.papermc.paper:paper-api:$paperApi")
    testImplementation("io.github.team-sneakymouse:sneakyutils:$sneakyUtilsVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.2:$mockBukkitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
}

java {
    withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks {
    processResources {
        inputs.property("version", project.version.toString())
        filesMatching("paper-plugin.yml") {
            expand("version" to project.version.toString())
        }
    }

    jar {
        archiveBaseName.set(rootProject.name)
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    runServer {
        minecraftVersion("26.2")
        // Resolved from Maven (Gradle cache); Paper still needs a jar on disk under run/plugins.
        pluginJars.from(sneakyUtilsPlugin)
        // Drop any hand-copied / stale SneakyUtils jar so only the pinned version from pluginJars is loaded.
        doFirst {
            val pluginsDir = runDirectory.get().asFile.resolve("plugins")
            if (!pluginsDir.isDirectory) return@doFirst
            pluginsDir.listFiles()
                ?.filter {
                    it.isFile &&
                        it.name.endsWith(".jar", ignoreCase = true) &&
                        it.name.contains("sneakyutils", ignoreCase = true)
                }
                ?.forEach { stale ->
                    logger.lifecycle("runServer: removing stale {}", stale.name)
                    stale.delete()
                }
        }
    }

    test {
        useJUnitPlatform()
    }

    val publishPreflight by registering(Exec::class) {
        group = "verification"
        description = "Catch sneakyrp publish footguns (gradlew mode, wrapper jar, workflow_dispatch)."
        workingDir = rootDir
        commandLine("bash", "scripts/publish-preflight.sh", rootDir.absolutePath)
    }

    check {
        dependsOn(publishPreflight)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "sneakyracing"
            from(components["java"])
            pom {
                name.set("SneakyRacing")
                description.set(
                    "Paper plugin for placeable racing rings and related racing objectives."
                )
                url.set("https://github.com/Team-Sneakymouse/SneakyRacing")
                properties.put("sneakyrp.deployment.type", "paper-plugin")
                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("team-sneakymouse")
                        name.set("Team Sneakymouse")
                        url.set("https://github.com/Team-Sneakymouse")
                    }
                }
                scm {
                    url.set("https://github.com/Team-Sneakymouse/SneakyRacing")
                    connection.set("scm:git:https://github.com/Team-Sneakymouse/SneakyRacing.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sneakyrp"
            url = uri("https://maven.sneakyrp.com/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<org.gradle.authentication.http.BasicAuthentication>("basic")
            }
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn(tasks.check)
}
