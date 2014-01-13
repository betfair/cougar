---
layout: default
---
IDL Reference
=============

IN PROGRESS.

Similar to maven pom reference

Interface.xml:

<pre>
<code>
  &lt;<a href='#interface'>interface</a>>
    &lt;description/>
    &lt;<a href='#operation'>operation</a>>
      &lt;description/>
      &lt;parameters>
        &lt;request>
          &lt;parameter>
            &lt;description/>
            &lt;validValues>
              &lt;value>
                &lt;description/>
              &lt;/value>
            &lt;/validValues>
          &lt;/parameter>
        &lt;/request>
        &lt;response>
          &lt;description/>
        &lt;/response>
        &lt;exceptions>
          &lt;exception>
            &lt;description/>
          &lt;/exception>
        &lt;/exceptions>
      &lt;/parameters>
    &lt;/operation>
    &lt;event>
      &lt;description/>
      &lt;parameter>
        &lt;description/>
        &lt;validValues>
          &lt;value>
            &lt;description/>
          &lt;/value>
        &lt;/validValues>
      &lt;/parameter>
    &lt;/event>
    &lt;dataType>
      &lt;description/>
      &lt;parameter>
        &lt;description/>
        &lt;validValues>
          &lt;value>
            &lt;description/>
          &lt;/value>
        &lt;/validValues>
      &lt;/parameter>
    &lt;/dataType>
    &lt;simpleType>
      &lt;description/>
      &lt;validValues>
        &lt;value>
          &lt;description/>
        &lt;/value>
      &lt;/validValues>
    &lt;/simpleType>
  &lt;/interface>
</code>
</pre>

Interface-Extensions.xml:

<pre>
<code>
  &lt;interface>
    &lt;operation>
      &lt;parameters>
        &lt;request>
          &lt;parameter>
            &lt;extensions>
              &lt;style/>
            &lt;/extensions>
          &lt;/parameter>
        &lt;/request>
      &lt;/parameters>
      &lt;extensions>
        &lt;path/>
        &lt;method/>
      &lt;/extensions>
    &lt;/operation>
    &lt;event>
      &lt;parameter>
        &lt;extensions>
          &lt;style/>
        &lt;/extensions>
      &lt;/parameter>
    &lt;/event>
  	&lt;extensions>
  		&lt;path/>
  	&lt;/extensions>
  &lt;/interface>
</code>
</pre>

interface
---------


operation
---------
