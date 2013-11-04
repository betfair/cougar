---
layout: default
---
{:toc}

# Launching from Maven

```
[launcher]
mvn -P run initialize
```

# Launching from the IDE

Create a debug profile in your IDE with the following characteristics:

|Main class|There will be a class called ```Launcher``` in the ```launcher``` project that the Cougar archetype generated, or you can use ```com.betfair.cougar.core.impl.Main```, or you can use one of your own devising|
|VM parameters|-|
|Program parameters|-|
|Project/module|your ```launcher``` project|

## Special instructions for Eclipse

There is a bug in Eclipse Maven plugin that needs to be fixed by adding following code to your top level pom (by default Eclipse plugin doesn’t add AspectJ artefacts):

```
    <build>
        <pluginManagement>
          <plugins>
            <plugin>
              <artifactId>maven-eclipse-plugin</artifactId>
              <configuration>
                <ajdtVersion>none</ajdtVersion>
              </configuration>
            </plugin>
          </plugins>
        </pluginManagement>
    </build>
```

Now run ```mvn eclipse:eclipse``` from the top level project, then import as existing project into Eclipse.  If you get annoying errors saying 'The project cannot be built until the build path errors are resolved', [follow these instructions](http://www.scottdstrader.com/blog/ether_archives/000921.html) to resolve.

# Custom Launchers

You are free to wrap ```com.betfair.cougar.core.impl.Main``` with your own class, if needed.
