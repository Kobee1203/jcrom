# Using with Guice #

Guice is a lightweight annotation-based dependency injection framework from Google. It is easy to use JCROM with Guice, just instantiate Jcrom in your Guice module, and bind it there. For example:

```
import com.google.inject.AbstractModule;
import org.jcrom.Jcrom;

public class MyGuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		Jcrom jcrom = new Jcrom();
		jcrom.map(WeblogEntry.class);
		bind(Jcrom.class).toInstance(jcrom);
	}
}
```

And then you can inject Jcrom into your DAOs.

Go [back](UserGuide.md) to the overview.