package com.xuecheng.manage_media;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class FileTest {

	/**
	 * 文件分块测试
	 * 读取文件将该文件分块存储
	 * @throws Exception
	 */
	@Test
	public void testChunk() throws Exception{
		// 文件路径
		File sourceFile = new File("F:/develop/ffmpeg/lucene.mp4");
		String chunkPath = "F:/develop/ffmpeg/chunk/";
		File chunkFolder = new File(chunkPath);
		if(!chunkFolder.exists()){
			chunkFolder.mkdirs();
		}
		
		//分块大小
		long chunkSize = 1024*1024*1;
		
		//分块数量
		long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
		
		if(chunkNum<=0){
			chunkNum = 1;
		}
		
		//缓冲区大小
		byte[] b = new byte[1024];
		
		//使用RandomAccessFile访问文件
		RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
		/*
			文件的分块
				先设定好每个文件要的大小，求出分块文件能够分成几块。
				然后读取设定的大小的字节，设定好分块文件及其名称(要有顺序),然后写入。
				循环能够分成的块数的次数，重复创建分块文件按写入顺序命名分块文件，写入当前分块文件的字节。
				直到最后一次不足设定的大小后，将其直接作为最后一块。
		 */
		//分块
		for(int i=0;i<chunkNum;i++){
			//创建分块文件
			File file = new File(chunkPath+i);
			boolean newFile = file.createNewFile();
			if(newFile){
				//向分块文件中写数据
				RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
				int len = -1;
				// 最后一次，不等于需要的字节的时候将其直接作为最后一块。
				while((len = raf_read.read(b))!=-1){
					raf_write.write(b,0,len);
					// 写入到到该分块需要的字节停止读取。
					if(file.length()>chunkSize){
						break;
					}
				}
				// 关闭写入流
				raf_write.close();
			}
		}
		// 关闭读取流
		raf_read.close();
	}

	/**
	 * 测试合并文件
	 * @throws IOException
	 */
	@Test
	public void testMerge() throws IOException {
		//块文件目录
		File chunkFolder = new File("F:/develop/ffmpeg/chunk/");
		//合并文件
		File mergeFile = new File("F:/develop/ffmpeg/lucene1.mp4");
		if(mergeFile.exists()){
			mergeFile.delete();
		}

		//创建新的合并文件
		mergeFile.createNewFile();
		//用于写文件
		RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
		//指针指向文件顶端
		raf_write.seek(0);
		// 缓冲区
		byte[] b = new byte[1024];
		/*
			获取分块文件所在的文件夹，获取该文件夹中的所有文件。
			使用List存储所有的文件对象，将其按照文件名称进行排序。
			按顺序依次读取List中的文件对象，然后将其写入到一个文件中，
			完成对文件夹下的所有分块文件的合并。
		 */
		// 分块列表
		File[] fileArray = chunkFolder.listFiles();
		// 转成集合，便于排序
		List<File> fileList = new ArrayList<File>(Arrays.asList(fileArray));
		// 从小到大排序
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
					return -1;
				}
				return 1;
			}
		});
		//合并文件
		for(File chunkFile:fileList){
			RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"rw");
			int len = -1;
			while((len=raf_read.read(b))!=-1){
				raf_write.write(b,0,len);
			}
			raf_read.close();
		}
		raf_write.close();
	}
}
