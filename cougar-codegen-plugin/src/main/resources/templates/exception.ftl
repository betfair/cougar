<#include "exceptionBase.ftl"><#t>

    public String getMessage() {
        return "responseCode="+getResponseCode()
    <#list exception.params as param>
      <#if param.paramName==firstEnumName>
                +", ${param.paramName}="+${param.paramName}+" ("+prefix+${param.paramName}.getCode()+")"
      <#else>
                +", ${param.paramName}="+${param.paramName}
      </#if>
    </#list>;
    }
}
