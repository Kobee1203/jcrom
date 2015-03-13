# Dependencies #

JCROM 2.2.0 depends on the following third party JARs:

  * JCR API, version 2.0 ([get it here](http://repo1.maven.org/maven2/javax/jcr/jcr/2.0/jcr-2.0.jar))
  * CGLIB, version 2.2.2 ([get it here](http://repo1.maven.org/maven2/cglib/cglib/2.2.2/cglib-2.2.2.jar))
  * ASM, version 1.5.3 (CGLIB depends on this) ([get it here](http://repo1.maven.org/maven2/asm/asm/3.3.1/asm-3.3.1.jar))
  * Apache Tika, version 1.1 ([get it here](http://repo1.maven.org/maven2/org/apache/tika/tika-core/1.1/tika-core-1.1.jar))
  * JavaFX, it's a weak dependency because this dependence is only required if you use JavaFX properties
> > JavaFX is not currently present on the Maven repository.
> > If you want to add it to your classpath, for example, you can declare a property whose value is the path to the JavaFX JAR in your settings.xml:

```
<settings ...>
...
	<profiles>
		<profile>
			<id>javafx</id>
			<activation>
				<activebydefault>true</activebydefault>
			</activation>
			<properties>
				<javafx.rt.jar>[JDK_PATH]/jre/lib/jfxrt.jar</javafx.rt.jar>
			</properties>
		</profile>
	</profiles>
...

	<activeProfiles>
		<activeProfile>javafx</activeProfile>
	</activeProfiles>
</settings>
```


> In your pom.xml, you add the following dependency:
```
<dependency>
	<groupId>com.oracle</groupId>
	<artifactId>javafx</artifactId>
	<version>2.2</version>
	<systemPath>${javafx.rt.jar}</systemPath>
	<scope>system</scope>
</dependency>
```

If you use [Maven2](http://maven.apache.org) to build your project, it will automatically download all dependencies for you (except JavaFX).