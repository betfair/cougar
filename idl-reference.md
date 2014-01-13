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
  &lt;<a href=''>interface</a>><br/>
    &lt;description/><br/>
    &lt;operation><br/>
      &lt;description/><br/>
      &lt;parameters><br/>
        &lt;request><br/>
          &lt;parameter><br/>
            &lt;description/><br/>
            &lt;validValues><br/>
              &lt;value><br/>
                &lt;description/><br/>
              &lt;/value><br/>
            &lt;/validValues><br/>
          &lt;/parameter><br/>
        &lt;/request><br/>
        &lt;response><br/>
          &lt;description/><br/>
        &lt;/response><br/>
        &lt;exceptions><br/>
          &lt;exception><br/>
            &lt;description/><br/>
          &lt;/exception><br/>
        &lt;/exceptions><br/>
      &lt;/parameters><br/>
    &lt;/operation><br/>
    &lt;event><br/>
      &lt;description/><br/>
      &lt;parameter><br/>
        &lt;description/><br/>
        &lt;validValues><br/>
          &lt;value><br/>
            &lt;description/><br/>
          &lt;/value><br/>
        &lt;/validValues><br/>
      &lt;/parameter><br/>
    &lt;/event><br/>
    &lt;dataType><br/>
      &lt;description/><br/>
      &lt;parameter><br/>
        &lt;description/><br/>
        &lt;validValues><br/>
          &lt;value><br/>
            &lt;description/><br/>
          &lt;/value><br/>
        &lt;/validValues><br/>
      &lt;/parameter><br/>
    &lt;/dataType><br/>
    &lt;simpleType><br/>
      &lt;description/><br/>
      &lt;validValues><br/>
        &lt;value><br/>
          &lt;description/><br/>
        &lt;/value><br/>
      &lt;/validValues><br/>
    &lt;/simpleType><br/>
  &lt;/interface>
</code>
</pre>

Interface-Extensions.xml:

<code>
  &lt;interface><br/>
    &lt;operation><br/>
      &lt;parameters><br/>
        &lt;request><br/>
          &lt;parameter><br/>
            &lt;extensions><br/>
              &lt;style/><br/>
            &lt;/extensions><br/>
          &lt;/parameter><br/>
        &lt;/request><br/>
      &lt;/parameters><br/>
      &lt;extensions><br/>
        &lt;path/><br/>
        &lt;method/><br/>
      &lt;/extensions><br/>
    &lt;/operation><br/>
    &lt;event><br/>
      &lt;parameter><br/>
        &lt;extensions><br/>
          &lt;style/><br/>
        &lt;/extensions><br/>
      &lt;/parameter><br/>
    &lt;/event><br/>
  	&lt;extensions><br/>
  		&lt;path/><br/>
  	&lt;/extensions><br/>
  &lt;/interface>
</code>
