---
layout: default
---


# Spring Configuration

Spring configuration is found in the `src/main/resources/conf` directory of the `application` module.

# Configuration Defaults and Overrides

    application
        src/main/resources
        cougar-application.properties   <-- Optional properties file to set cougar property values
                            for your application at a level below overrides
                            Will override service-defaults.properties but can still
                            be overridden by overrides.properties
                            Must have this exact name
            conf
                <spring config files>
                service-defaults.properties <-- default properties (incl. overrides of other modules)
                                                You can rename this file to anything you like, just
                                                update the property placeholder in the spring config.
    launcher
        src/main/resources
            conf
                overrides.properties        <-- overrides to Cougar, application and other modules

Notes:

* Application defaults should enumerate sane deployment-scenario-neutral values (or the textual value
`MUST_BE_OVERRIDDEN`) for all your configuration items, appropriately commented
* Wherever `src/main/resources` is mentioned you can also put the same files in `src/test/resources`, which will
take precedence over the ones in `src/main/resources` during the `test` phase of the module.

# Using Configuration Data in Your Module's Spring Assembly

Each and every Cougar module is assembled using its `src/main/resources/conf/cougar-application-spring.xml` file.
Within this file a `PropertyConfigurer` is defined that segregates and scopes your configuration data with the use of
a "property placeholder prefix", to avoid collisions with other modules' data.  The following definition is typical, and
is what the Cougar archetype gives you by default.

    <bean class="com.betfair.cougar.util.configuration.PropertyConfigurer">
            <property name="defaultConfig" value="conf/<artifactId>-defaults.properties"/>
        <property name="configOverride" value="overrides.properties"/>
        <property name="placeholderPrefix" value="$MY_APPLICATION{"/>
    </bean>

Say you've defined the property `magic.number` in `<artifactId>-defaults.properties` and you want to use it in
the assembly - you reference it like so:

    <property name="magicNumber" value="$MY_APPLICATION{magic.number}"/>

If you want to reference another module's configuration data in your assembly, you need to know its property placeholder
prefix.  If you depend on the module in your project, you can navigate to the depended-upon JAR and look inside it at
`src/main/resources/conf/cougar-application-spring.xml` and look for the prefix there.

*It's strongly recommended that you change the property placeholder prefix from MY_APPLICATION to something more specific
to your module, to avoid collisions.*

# Static Cougar Config Overrides

Often you will find that there are a number of core Cougar settings which you always want set to a specific value,
regardless of environment. For this you can use a `cougar-application.properties` file, which is loaded prior to the
overrides file.

# Encrypted Property Values

Cougar supports encrypted property values via implementations of the `StringEncryptor` interface found in
[Jasypt](http://www.jasypt.org). To register an implementation of this interface you must create a [bootstrap](Using_Modules_Libraries_in_Cougar.html)
module, with the following bean definition:

    <bean parent="cougar.core.EncryptorRegisterer" lazy-init="false">
        <constructor-arg>
            <bean class="com.betfair.MyStringEncyptor"/>
        </constructor-arg>
    </bean>

*Note*: Cougar only supports a single encryptor, so any attempt to set 2 will fail.

# Cougar Framework Module Configuration

Those Cougar framework modules that have properties of interest and that can be over-ridden are documented separately.
