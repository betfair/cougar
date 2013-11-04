---
layout: default
---
# Cougar Log Level mappings

If your application is using log4j as the logging framework (the default), you can use these mappings to set the appropriate log level via cougar property.

<table>
<tr>
<th>Cougar</th><th>Log4j</th></tr>
<tr>
<td> ALL </td>
<td> ALL </td>
</tr>
<tr>
<td> FINEST </td>
<td> TRACE </td>
</tr>
<tr>
<td> FINE/FINER </td>
<td> DEBUG </td>
</tr>
<tr>
<td> INFO/CONFIG </td>
<td> INFO </td>
</tr>
<tr>
<td> WARNING </td>
<td> WARN </td>
</tr>
<tr>
<td> SEVERE </td>
<td> ERROR </td>
</tr>
<tr>
<td> OFF </td>
<td> OFF </td>
</tr>
</table>

This is as per the implementation in Log4jLoggingControl