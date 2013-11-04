---
layout: default
---

* Create a folder in the src/main/resources folder of your application project called ```static-html```
* Copy your file into the directory – let’s assume it’s called ```foo.html```
* Rebuild the application to bundle the file into a jar file
* Start Cougar
* In a browser, go to ```http://<your-host-name>:<port-number>/static-html/foo.html```

To customize the location of the static HTML directory:

* Create the ‘info’ directory under some appropriate JAR’s ```src/main/resources``` dir
* Add the following properties to your app

```
cougar.htmlHandler.contextPath=/info
cougar.htmlHandler.regex=/info/.*
```

I think the regex controls what resources the client of the embedded web server is allowed to access directly.

See also [the config properties page for the relevant module](http://TODO).
