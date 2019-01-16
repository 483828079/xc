<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
<#--
    1、注释，即<#‐‐和‐‐>，介于其之间的内容会被freemarker忽略
    2、插值（Interpolation）：即${..}部分,freemarker会用真实的值代替${..}
    3、FTL指令：和HTML标记类似，名字前加#予以区分，
    Freemarker会解析标签中的表达式或逻辑。
    4、文本，仅文本信息，这些不是freemarker的注释、插值、
    FTL指令的内容会被freemarker忽略解析，直接输出内容。
-->
    Hello ${name}! <#--插值-->

<#--遍历List-->
<#--通过list指令-->
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#list stus as stu>
        <tr>
            <#--可以通过 当前元素_index获取到索引 从0开始。-->
            <td>${stu_index + 1}</td>
            <td>${stu.name}</td>
            <td>${stu.age}</td>
            <td>${stu.mondy}</td>
        </tr>
    </#list>
</table>

<#--遍历Map, 通过key获取value这种形式。
    遍历通过list指令遍历key。
-->

<#--通过key获取value-->
<#--方式一：-->
输出stu1的学生信息：<br/>
姓名：${stuMap['stu1'].name}<br/>
年龄：${stuMap['stu1'].age}<br/>
<#--方式二：-->
输出stu1的学生信息：<br/>
姓名：${stuMap.stu1.name}<br/>
年龄：${stuMap.stu1.age}<br/>
<#--遍历Map的所有key，通过key获取value-->
遍历输出两个学生信息：<br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#--map?keys 获取map中的所有key-->
    <#list stuMap?keys as k>
        <tr>
            <td>${k_index + 1}</td>
            <td>${stuMap[k].name}</td>
            <td>${stuMap[k].age}</td>
            <td >${stuMap[k].mondy}</td>
        </tr>
    </#list>
</table>

<#--if指令-->
<table>
    <tr>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#list stus as stu>
        <tr>
            <td <#if stu.name =='小明'>style="background:red;"</#if>>${stu.name}</td>
            <td>${stu.age}</td>
            <td >${stu.mondy}</td>
        </tr>
    </#list>

    <#--运算符-->
    <#--

        逻辑：
        逻辑运算符只能作用于布尔值,否则将产生错误

        比较：
        =和!=可以用于字符串,数值和日期来比较是否相等,
        但=和!=两边必须是相同类型的值,否则会产生错误。
        其它的运行符可以作用于数字和日期,但不能作用于字符串,大部分的时
        候,使用gt等字母运算符代替>会有更好的效果,
        因为 FreeMarker会把>解释成FTL标签的结束字符,当然,也可以使用括
        号来避免这种情况,如:<#if (x>y)>

        也就是字符串只能比较 != ==。数字日期能比较 > < >= <=。
        而比较> <等会无法解析，要带上();
    -->

    <#--空值处理
        判断某变量是否存在使用 “??”
        用法为:variable??,如果该变量存在,返回true,否则返回false
    -->
    <#if stus??>
        <#list stus as stu>
            ......
        </#list>
    </#if>

    <#--空值替换
        缺失变量默认值使用 “!” 使用!要以指定一个默认值，当变量为空时显示默认值。
        ${name!''}表示如果name为空显示空字符串。
    -->

    <#--内建函数-->
    <#--内建函数语法格式： 变量+?+函数名称-->
    <#--获取集合的大小 ${集合名?size}-->

    <#--日期格式化-->
    <#--显示年月日: ${today?date}
    显示时分秒：${today?time}
    显示日期+时间：${today?datetime} <br>
    自定义格式化： ${today?string("yyyy年MM月")}-->
</table>
</body>
</html>