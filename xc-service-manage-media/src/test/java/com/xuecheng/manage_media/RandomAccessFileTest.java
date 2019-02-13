package com.xuecheng.manage_media;
import org.junit.*;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * RandomAccessFile 的测试。
 *
 */
public class RandomAccessFileTest {
	@Test
	public void testWrite() throws Exception{
		File file = new File("E:/a.txt");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		// 写入utf-8，该utf-8比原来的utf-8前面多两个字节。用来区分是否该段字符。
		raf.writeUTF("你好，我是世界");
		// 写入字节
		raf.write(65);
		// 写入int类型
		raf.writeInt(10);
		raf.close();
	}

	@Test
	public void testRead() throws Exception{
		File file = new File("E:/a.txt");
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		// 读取一段连续的utf字符
		String utf = raf.readUTF();
		System.out.println(utf);
		System.out.println("当前文件指针的位置：" + raf.getFilePointer());
		// 读取一个字节
		byte b = raf.readByte();
		System.out.println(b);
		System.out.println("当前文件指针的位置：" + raf.getFilePointer());
		// 设置指针的位置。
		raf.seek(0);
		raf.seek(24);
		// 读取一个int的数字，读取四个字节。
		int i = raf.readInt();
		System.out.println(i);
		System.out.println("当前文件指针的位置：" + raf.getFilePointer());
		raf.close();
	}
}
