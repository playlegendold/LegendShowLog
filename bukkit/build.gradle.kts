val branch: String? = System.getenv("GITHUB_REF")?.replace("refs/heads/", "")

group = "net.playlegend"
version = if (System.getenv("CI") != null) {
    branch.toString()
} else {
    "dev"
}.replace("/", "-")

val branchVersion = when (branch?.toLowerCase()) {
    "master" -> "master"
    "staging" -> "staging"
    else -> "dev"
}

dependencies {
    compileOnly("net.playlegend:bewear-server:1.15.2-$branchVersion")
}

val tokens = mapOf("VERSION" to project.version)

tasks.withType<Jar> {
    filesMatching("*.yml") {
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokens)
    }
}

tasks.register<Jar>("fatSources") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name.toLowerCase()
            version = project.version.toString()

            from(components["java"])
            artifact(tasks["shadowJar"])
            artifact(tasks["fatSources"])
        }
    }
    repositories {
        maven {
            credentials {
                username = System.getenv("repositoryUser")
                password = System.getenv("repositoryPassword")
            }
            url = uri("https://repository.playlegend.net/artifactory/legend/")
        }
    }
}