# maven-cougar-codegen-plugin

## Overview

The ```maven-cougar-codegen-plugin``` is a custom plugin used to generate Cougar services. Its design has gone through twists and turns, but its current purpose is to be a one-stop plugin which does _everything_ necessary to set up a Cougar service, so that no additional plugins are required. 

Its functions include:

* generating the necessary Java code (interface, datatypes etc) from the IDD (including custom validation and merging of extensions)
* validating the IDD
* generating the WSDL
* updating the build path to include the custom code

The plugin exposes the ugly, hairy underbelly of plugin development. This page is a dumping ground for various gotchas and issues we've run into.

## Loading IDDs and include files as resources

You can load IDDs in one of two ways:

* as files - the IDD, the extension and any include files should be in ```/src/main/resources```.
* as resources - the IDD, extension and include files should be on the classpath. This options allows IDDs and include files to be managed and versioned independently of the service itself.

Toggle the resource-vs-file behaviour using the ```iddAsResource``` plugin parameter.

## Gotcha with resources

The ideal place to add the idd as a dependency would be as a dependency of the plugin itself, ie. ``project/build/plugins/plugin/dependencies``. This works _unless_ your service is part of a larger project tree in which there are multiple usages of the plugin. In that case you have to include the dependencies as part of the project, ie. ``project/dependencies``.

The reason for this is that Maven resolves the dependencies for the plugin once. If you have project A and project B with corresponding plugin dependencies of ```projectA-idd``` and ```projectB-idd```, then Maven will ignore the second set of dependencies, and both projects will have access to only (say) ```projectA-idd```, causing project B to fail with 'resource not found' errors.
