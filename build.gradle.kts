import java.net.URI

plugins {
    `version-catalog`
    `maven-publish`
}

group = "no.nav.dagpenger"

// NB: URL-en holdes utenfor maven-blokken nedenfor med vilje.
// Dependabot sin gradle-parser skanner hele fila med en enkel regex som
// leter etter maven-blokker med url og en anført streng, uten å skille
// mellom repositories for dependency-resolution og publishing.repositories
// for publisering. Med en anført streng inne i maven-blokken plukker den
// opp denne GitHub Packages-URL-en som en dependency-repo, og feiler (404)
// når den sjekker f.eks. ktor der mot. Se dependabot-core sin
// RepositoriesFinder klasse, GROOVY_MAVEN_REPO_REGEX.
val gitHubPackagesUrl = URI("https://maven.pkg.github.com/navikt/dp-version-catalog")

catalog {
    versionCatalog {
        from(files("./gradle/libs.versions.toml"))
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
            url = gitHubPackagesUrl
            credentials {
                val githubUser: String? by project
                val githubPassword: String? by project
                username = githubUser
                password = githubPassword
            }
        }
    }
}
