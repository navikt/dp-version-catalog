import java.net.URI

plugins {
    `version-catalog`
    `maven-publish`
}

group = "no.nav.dagpenger"

catalog {
    // declare the aliases, bundles and versions in this block
    versionCatalog {
        version("kotlin", "1.9.10")
        version("ktor", "2.3.3")
        version("kotest", "5.6.2")

        // Plugins
        plugin("kotlin", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
        plugin("spotless", "com.diffplug.spotless").version("6.20.0")

        // Libraries
        pakke("ktor-server", "io.ktor", "ktor") {
            +"" // ktor-server
            +"auth"
            +"auth-jwt"
            +"content-negoitation"
            +"status-pages"
        }.bundle()
        pakke("ktor-client", "io.ktor", "ktor") {
            +"auth-jvm"
            +"cio"
            +"jackson"
            +"logging-jvm"
            +"content-negotiation"
        }.bundle()
        pakke("jackson", "com.fasterxml.jackson.core", "jackson") {
            +"datatype-jsr310"
            +"core"
            +"annotation"
        }.version("2.15.2").bundle()
        library("jackson-kotlin", "com.fasterxml.jackson.core", "jackson-module-kotlin").versionRef("jackson")
        library("ktor-serialization-jackson", "io.ktor", "ktor-serialization-jackson").versionRef("ktor")

        library("kotest-assertions-core", "io.kotest", "kotest-assertions-core-jvm").versionRef("kotest")
        library("kotest-assertions-json", "io.kotest", "kotest-assertions-json-jvm").versionRef("kotest")

        // Database
        library("flyway", "org.flywaydb", "flyway-core").version("9.21.2")
        library("hikari", "com.zaxxer", "HikariCP").version("5.0.1")
        library("postgresql-driver", "org.postgresql", "postgresql").version("42.6.0")
        library("kotlinquery", "com.github.seratch", "kotlinquery").version("1.9.0")

        pakke("testcontainer", "org.testcontainers", "testcontainer") {
            +""
            +"postgresql"
        }.version("2.3.1")

        library("konfig", "com.natpryce", "konfig").version("1.6.10.0")

        pakke("dp-biblioteker", "com.github.navikt.dp-biblioteker", "dp-biblioteker") {
            +"oauth2-klient"
            +"image-utils"
        }.version("2023.04.27-09.33.fcf0798bf943")

        // Bundles
        bundle("postgres", listOf("flyway", "hikari", "postgresql-driver", "kotlinquery"))
        bundle("kotest-assertions", listOf("kotest-assertions-core", "kotest-assertions-json"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/navikt/dp-version-catalog")
            credentials {
                val githubUser: String? by project
                val githubPassword: String? by project
                username = githubUser
                password = githubPassword
            }
        }
    }
}

fun VersionCatalogBuilder.pakke(
    prefix: String,
    group: String,
    version: String,
    block: WithModules.() -> Unit,
): WithModules {
    val withModules = WithModules(this, prefix, group, version)
    block(withModules)
    return withModules
}

class WithModules(
    private val versionCatalogBuilder: VersionCatalogBuilder,
    private val prefix: String,
    private val group: String,
    private val version: String,
) {
    private val pakker = mutableListOf<String>()
    operator fun String.unaryPlus() {
        val pakke = listOf(prefix, this).filter { it.isNotEmpty() }.joinToString("-").also { pakker.add(it) }
        versionCatalogBuilder.library(pakke, group, pakke).versionRef(version)
    }

    fun bundle(): WithModules {
        versionCatalogBuilder.bundle(prefix, pakker)
        return this
    }

    fun version(version: String): WithModules {
        versionCatalogBuilder.version(prefix, version)
        return this
    }
}
