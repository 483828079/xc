package com.xuecheng.test.freemarker;

import com.xuecheng.test.freemarker.model.Student;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * freemarker生成页面。
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FreemarkerTest {

	/**
	 * 通过模板文件生成页面
	 * @throws Exception
	 */
	@Test
	public void testGenerateHtml() throws Exception{
		// 配置类
		Configuration configuration = new Configuration(Configuration.getVersion());
		//设置模板路径
		String classpath = this.getClass().getResource("/").getPath();
		configuration.setDirectoryForTemplateLoading(new File(classpath + "/mytemplates/"));
		// 设置字符集
		configuration.setDefaultEncoding("UTF-8");
		// 加载模板
		Template template = configuration.getTemplate("/test1.ftl");

		// 数据模型
		Map<String, Object> map = getData();

		// 静态化, 生成的是一个字符串。可以把该字符串写到本地。
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

		// IOUtils 将字符串读到输入流
		InputStream inputStream = IOUtils.toInputStream(content);
		// 文件输出位置
		FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/test1.html"));
		// IOUtils 输出
		IOUtils.copy(inputStream, fileOutputStream);
	}

	/**
	 * 通过模板字符串生成页面
	 * @throws Exception
	 */
	@Test
	public void testGenerateHtmlByString() throws Exception{
		// 配置类
		Configuration configuration = new Configuration(Configuration.getVersion());
		//模板内容
		String templateString = getTemplateStr();

		// 模板加载器
		StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
		// 给模板加载器设置字符类型的模板。
		stringTemplateLoader.putTemplate("template",templateString);
		// 设置模板加载器到配置类
		configuration.setTemplateLoader(stringTemplateLoader);

		// 通过模板name获取到模板。
		Template template = configuration.getTemplate("template", "UTF-8");

		// 数据模型
		Map<String, Object> map = getData();

		// 静态化
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

		// 将静态化内容输出。
		InputStream inputStream = IOUtils.toInputStream(content);
		OutputStream outputStream = new FileOutputStream("d:/test2.html");
		IOUtils.copy(inputStream, outputStream);
	}

	public Map<String, Object> getData() {
		Map<String, Object> map = new HashMap<>();
		map.put("name","黑马程序员");
		Student stu1 = new Student();
		stu1.setName("小明");
		stu1.setAge(18);
		stu1.setMondy(1000.86f);
		stu1.setBirthday(new Date());
		Student stu2 = new Student();
		stu2.setName("小红");
		stu2.setMondy(200.1f);
		stu2.setAge(19);
		stu2.setBirthday(new Date());
		List<Student> friends = new ArrayList<>();
		friends.add(stu1);
		stu2.setFriends(friends);
		stu2.setBestFriend(stu1);
		List<Student> stus = new ArrayList<>();
		stus.add(stu1);
		stus.add(stu2);
		//向数据模型放数据
		map.put("stus",stus);
		//准备map数据
		HashMap<String,Student> stuMap = new HashMap<>();
		stuMap.put("stu1",stu1);
		stuMap.put("stu2",stu2);
		//向数据模型放数据
		map.put("stu1",stu1);
		//向数据模型放数据
		map.put("stuMap",stuMap);
		map.put("point", 102920122);
		return map;
	}

	public String getTemplateStr() {
		return "<!DOCTYPE html>\n" +
				"<html>\n" +
				"<head>\n" +
				"    <meta charset=\"utf‐8\">\n" +
				"    <title>Hello World!</title>\n" +
				"</head>\n" +
				"<body>\n" +
				"Hello ${name}!\n" +
				"<br/>\n" +
				"<table>\n" +
				"\n" +
				"\n" +
				"\n" +
				"    <tr>\n" +
				"        <td>序号</td>\n" +
				"        <td>姓名</td>\n" +
				"        <td>年龄</td>\n" +
				"        <td>钱包</td>\n" +
				"    </tr>\n" +
				"    <#list stus as stu>\n" +
				"        <tr>\n" +
				"            <td>${stu_index + 1}</td>\n" +
				"            <td <#if stu.name =='小明'>style=\"background:red;\"</#if>>${stu.name}</td>\n" +
				"            <td>${stu.age}</td>\n" +
				"            <td >${stu.mondy}</td>\n" +
				"        </tr>\n" +
				"    </#list>\n" +
				"</table>\n" +
				"<br/><br/>\n" +
				"输出stu1的学生信息：<br/>\n" +
				"姓名：${stuMap['stu1'].name}<br/>\n" +
				"年龄：${stuMap['stu1'].age}<br/>\n" +
				"输出stu1的学生信息：<br/>\n" +
				"姓名：${stu1.name}<br/>\n" +
				"年龄：${stu1.age}<br/>\n" +
				"遍历输出两个学生信息：<br/>\n" +
				"<table>\n" +
				"    <tr>\n" +
				"        <td>序号</td>\n" +
				"        <td>姓名</td>\n" +
				"        <td>年龄</td>\n" +
				"        <td>钱包</td>\n" +
				"    </tr>\n" +
				"    <#list stuMap?keys as k>\n" +
				"        <tr>\n" +
				"            <td>${k_index + 1}</td>\n" +
				"            <td>${stuMap[k].name}</td>\n" +
				"            <td>${stuMap[k].age}</td>\n" +
				"            <td >${stuMap[k].mondy}</td>\n" +
				"        </tr>\n" +
				"    </#list>\n" +
				"</table>\n" +
				"</br>\n" +
				"<table>\n" +
				"    <tr>\n" +
				"        <td>姓名</td>\n" +
				"        <td>年龄</td>\n" +
				"        <td>出生日期</td>\n" +
				"        <td>钱包</td>\n" +
				"        <td>最好的朋友</td>\n" +
				"        <td>朋友个数</td>\n" +
				"        <td>朋友列表</td>\n" +
				"    </tr>\n" +
				"    <#if stus??>\n" +
				"        <#list stus as stu>\n" +
				"\n" +
				"\n" +
				"\n" +
				"            <tr>\n" +
				"                <td>${stu.name!''}</td>\n" +
				"                <td>${stu.age}</td>\n" +
				"                <td>${(stu.birthday?date)!''}</td>\n" +
				"                <td>${stu.mondy}</td>\n" +
				"                <td>${(stu.bestFriend.name)!''}</td>\n" +
				"                <td>${(stu.friends?size)!0}</td>\n" +
				"                <td>\n" +
				"                    <#if stu.friends??>\n" +
				"                        <#list stu.friends as firend>\n" +
				"                            ${firend.name!''}<br/>\n" +
				"                        </#list>\n" +
				"                    </#if>\n" +
				"                </td>\n" +
				"            </tr>\n" +
				"        </#list>\n" +
				"    </#if>\n" +
				"</table>\n" +
				"<br/>\n" +
				"<#assign text=\"{'bank':'工商银行','account':'10101920201920212'}\" />\n" +
				"<#assign data=text?eval />\n" +
				"开户行：${data.bank} 账号：${data.account}\n" +
				"</body>\n" +
				"</html>";
	}
}
