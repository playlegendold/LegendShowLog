<a href="https://playlegend.net"><img src="https://static.playlegend.net/full-logo-stone-highres.png" width="60%"></a>

# LegendShowLog
LegendShowLog provides a command to quickly paste current logs onto a paste server. 
This helps a lot with debugging.

## Commands & permissions
| Command | Permission |
| --- | --- |
| /showlog | showlog.paste |

## Config options
LegendShowLog's config provides options to use a custom paste server. LegendShowLog is designed and tested only for hastebin.

| Option | Description |
| --- | --- |
| showlog.log_path | Path to latest log file |
| showlog.post_url | Post endpoint of hastebin server |
| showlog.paste_url | Root url of hastebin server |

## Maven Repository
LegendShowLog is available through our Maven [repository](https://repository.playlegend.net).
You have to replace **version** with your desired values. 

### Gradle (Kotlin)
```kotlin
repositories {
    maven("https://repository.playlegend.net/artifactory/opensource/")
}

dependencies {
    compileOnly("net.playlegend:legendshowlog:VERSION")
}
```

### Maven
```xml
<repositories>
    <!-- This adds the Legend Maven repository to the build -->
    <repository>
        <id>legend-repo</id>
        <url>https://repository.playlegend.net/artifactory/opensource</url>
    </repository>
</repositories>

<dependency>
    <groupId>net.playlegend</groupId>
    <artifactId>legendshowlog</artifactId>
    <version>VERSION</version>
</dependency>
```

## Build a custom version
To build your own version of LegendShowLog just execute the following command in project root.
```shell script
./gradlew shadowJar
```
You can find your artifacts in `/build/libs/`.
