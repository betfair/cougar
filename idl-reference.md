---
layout: default
---
# IDL Reference

<code>&lt;Interface&gt;.xml</code>:

<pre>
<code>
  &lt;<a href='#interface'>interface</a>>
    &lt;description/>
    &lt;<a href='#operation'>operation</a>>
      &lt;description/>
      &lt;parameters>
        &lt;request>
          &lt;<a href='#parameter'>parameter</a>>
            &lt;description/>
            &lt;validValues>
              &lt;value>
                &lt;description/>
              &lt;/value>
            &lt;/validValues>
          &lt;/parameter>
        &lt;/request>
        &lt;<a href='#response'>response</a>>
          &lt;description/>
        &lt;/response>
        &lt;exceptions>
          &lt;<a href='#exception'>exception</a>>
            &lt;description/>
          &lt;/exception>
        &lt;/exceptions>
      &lt;/parameters>
    &lt;/operation>
    &lt;<a href='#event'>event</a>>
      &lt;description/>
      &lt;<a href='#parameter'>parameter</a>>
        &lt;description/>
        &lt;validValues>
          &lt;value>
            &lt;description/>
          &lt;/value>
        &lt;/validValues>
      &lt;/parameter>
    &lt;/event>
    &lt;<a href='#dataType'>dataType</a>>
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
    &lt;<a href='#simpleType'>simpleType</a>>
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

<code>&lt;Interface-Extensions&gt;.xml</code>:

<pre>
<code>
  &lt;<a href='#interface'>interface</a>>
    &lt;<a href='#operation'>operation</a>>
      &lt;parameters>
        &lt;request>
          &lt;<a href='#parameter'>parameter</a>>
            &lt;<a href='#parameter-extensions'>extensions</a>>
              &lt;style/>
            &lt;/extensions>
          &lt;/parameter>
        &lt;/request>
      &lt;/parameters>
      &lt;<a href='#operation-extensions'>extensions</a>>
        &lt;path/>
        &lt;method/>
      &lt;/extensions>
    &lt;/operation>
    &lt;event>
      &lt;parameter>
        &lt;<a href='#parameter-extensions'>extensions</a>>
          &lt;style/>
        &lt;/extensions>
      &lt;/parameter>
    &lt;/event>
  	&lt;<a href='#interface-extensions'>extensions</a>>
  		&lt;path/>
  	&lt;/extensions>
  &lt;/interface>
</code>
</pre>

## interface

<a name="interface-extensions"></a>
### extensions


## operation

<a name="operation-extensions"></a>
### extensions


## parameter

<a name="parameter-extensions"></a>
### extensions


## response


## exception


## event


## dataType

## simpleType



