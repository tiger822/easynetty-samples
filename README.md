# easynetty-samples
samples for easynetty

if can't fetch easynetty dependency, please add this repository into your pom file:
```xml
<repository>
            <id>sonatype-nexus-snapshots</id>
            <name>Sonatype Nexus Snapshots</name>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
</repository>
```