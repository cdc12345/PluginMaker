<#if data.getDefaultMapping()??>
_default: ${data.getDefaultMapping()}
</#if>
<#if data.getMcreatorMapTemplate()??>
_mcreator_map_template: "${data.getMcreatorMapTemplate()}"
</#if>
<#list data.mappingsContent as entry>
<#if entry.isEdited(data)>
<#if entry.getMappingContent().size() == 1>
${entry.getName()}: ${entry.getFirst()}
</#if>
</#if>
</#list>