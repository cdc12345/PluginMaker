{
  "color": ${data.getColor()}
  <#if data.getParentCategory()??>
  "parent_category": "${data.getParentCategory()}"
  </#if>
}