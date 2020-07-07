# Set the maximum heap size to 1024m.
-Xmx1024m

# Set a system property.
<#if SampleConfigFile_json??>
  <#list SampleConfigFile_json.jvm.properties as prop>
-D[=prop.key]=[=prop.value]
  </#list>
</#if>
