val branch: String? = System.getenv("GITHUB_REF")?.replace("refs/heads/", "")

group = "net.playlegend"
version = if (System.getenv("CI") != null) {
    branch.toString()
} else {
    "dev"
}.replace("/", "-")

plugins {
    `java-library`
    `maven-publish`
    checkstyle
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.sonarqube") version "2.7"
    id("com.gorylenko.gradle-git-properties") version "2.2.2"
}

tasks.create<Copy>("copyHooks") {
    from(file("./hooks/"))
    into(file("./.git/hooks/"))
}

tasks.getByPath("prepareKotlinBuildScriptModel").dependsOn("copyHooks")

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.gorylenko.gradle-git-properties")

    checkstyle {
        toolVersion = "8.34"
        config = project.resources.text.fromUri("https://static.playlegend.net/checkstyle.xml")
    }

    gitProperties {
        gitPropertiesName = "git.properties"
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        keys = arrayOf("git.branch", "git.build.host", "git.build.version", "git.commit.id", "git.commit.id.abbrev",
                "git.commit.message.full", "git.commit.message.short", "git.commit.time", "git.commit.user.email",
                "git.commit.user.name", "git.remote.origin.url", "git.total.commit.count").toMutableList()
    }

    repositories {
        mavenCentral()
        jcenter()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven {
            url = uri("https://repository.playlegend.net/artifactory/legend/")
            credentials {
                if (System.getenv("CI") != null) {
                    username = System.getenv("repositoryUser")
                    password = System.getenv("repositoryPassword")
                } else {
                    username = project.properties["repositoryUser"] as String?
                    password = project.properties["repositoryPassword"] as String?
                }
            }
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.12")
        annotationProcessor("org.projectlombok:lombok:1.18.12")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_14
        targetCompatibility = JavaVersion.VERSION_14
    }

    tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
}
