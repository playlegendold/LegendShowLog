group = "net.playlegend"
version = "1.0.0"

plugins {
    `java-library`
    `maven-publish`
    checkstyle
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.gorylenko.gradle-git-properties") version "2.2.4"
    id("com.github.spotbugs") version "4.7.5"
}

tasks.create<Copy>("copyHooks") {
    from(file("./hooks/"))
    into(file("./.git/hooks/"))
}

tasks.getByPath("prepareKotlinBuildScriptModel").dependsOn("copyHooks")

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.gorylenko.gradle-git-properties")
    apply(plugin = "com.github.spotbugs")

    checkstyle {
        toolVersion = "8.40"
        config = project.resources.text.fromUri("https://assets.playlegend.net/checkstyle.xml")
    }

    spotbugs {
        ignoreFailures.set(true)
        showProgress.set(true)
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
        compileOnly("org.projectlombok:lombok:1.18.18")
        annotationProcessor("org.projectlombok:lombok:1.18.18")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
}
