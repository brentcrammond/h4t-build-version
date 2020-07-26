# h4t-build-version
Bump Build Version/Timestamp Maven Plugin


In the property file:
```
build.version = 1.0.301+build.2001
build.timestamp = Sun 26-Jul-2020 23:59
```

Maven Plugin:
```
<plugin>
  <groupId>nz.h4t</groupId>
  <artifactId>h4t-build-version</artifactId>
  <version>1.0</version>
  <executions>
    <execution>
      <configuration>
        <propertyFilePath>${project.basedir}/src/main/resources/application.properties</propertyFilePath>
      </configuration>
      <goals>
          <goal>BumpBuildVersion</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```      
