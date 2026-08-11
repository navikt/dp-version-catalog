plugins {
    base
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

val versionCatalog = project.rootProject.extensions
    .getByType<VersionCatalogsExtension>().named("libs")

tasks.register("verifyVersionCatalogResolves") {
    doLast {
        val notations = versionCatalog.libraryAliases.map { alias ->
            versionCatalog.findLibrary(alias).get().get().let {
                "${it.module.group}:${it.module.name}:${it.versionConstraint.requiredVersion}"
            }
        }

        val failures = notations.mapNotNull { notation ->
            runCatching {
                configurations.detachedConfiguration(dependencies.create(notation)).resolve()
            }.exceptionOrNull()?.let { "$notation -> ${it.message}" }
        }

        require(failures.isEmpty()) {
            "Følgende kunne ikke resolves:\n${failures.joinToString("\n")}"
        }
        println("Alle ${notations.size} dependencies resolvet OK.")
    }
}
