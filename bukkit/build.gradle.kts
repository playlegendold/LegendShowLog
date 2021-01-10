val branch: String? = System.getenv("GITHUB_REF")
        ?.replace("refs/heads/", "")
        ?.replace("refs/tags/", "")

group = "net.playlegend"
version = if (System.getenv("CI") != null) {
    if (branch.equals("stage") || branch.equals("prod")
            || branch!!.matches(Regex("v\\d+[.]\\d+[.]\\d+"))) {
        branch.toString()
    } else {
        "$branch-SNAPSHOT"
    }
} else {
    "dev-SNAPSHOT"
}.replace("/", "-")

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.4-R0.1-SNAPSHOT")
    compileOnly("commons-io:commons-io:2.8.0")
    compileOnly("org.jetbrains:annotations:20.1.0")
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

            artifact(tasks["jar"])
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
            url = uri("https://repository.playlegend.net/artifactory/opensource/")
        }
    }
}
