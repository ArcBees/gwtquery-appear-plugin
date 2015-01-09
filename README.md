#GQuery Appear Plugin

##Description of the plugin
Appear plugin allows an element to be notified when it enters or leaves screen when scrolling

##How it works ?
To call the plugin:

```java
    import static com.arcbees.gquery.appear.client.Appear.Appear;

    $(myElement).As(Appear)
        .appear(new Function() {
            @Override
            public void f() {
                // Do something onAppear here
            }
        )}
        .disappear(new Function() {
            @Override
            public void f() {
                // Do something onDisappear here
            }
        )};
```

## How to install

There is no official release yet, but you can use a snapshot version
###With maven :
```xml
    <dependency>
        <groupId>com.arcbees.gquery</groupId>
        <artifactId>appear</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```

##Thanks to
[![Arcbees.com](http://arcbees-ads.appspot.com/ad.png)](http://arcbees.com)

[![IntelliJ](https://lh6.googleusercontent.com/--QIIJfKrjSk/UJJ6X-UohII/AAAAAAAAAVM/cOW7EjnH778/s800/banner_IDEA.png)](http://www.jetbrains.com/idea/index.html)

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/361ac462e4947df21a821a3fbbbc3367 "githalytics.com")](http://githalytics.com/ArcBees/gwtquery-appear-plugin)
