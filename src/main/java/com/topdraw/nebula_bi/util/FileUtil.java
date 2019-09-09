package com.topdraw.nebula_bi.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author zqq
 *
 */
public class FileUtil {

	/**
	 * 解压文件到指定目录
	 * 
	 * @param sZipPathFile
	 *            源压缩文件地址
	 * @param sDestPath
	 *            目标文件地址
	 * @return
	 * @throws IOException
	 */
	public static void unZipFile(String sZipPathFile, String sDestPath) throws IOException {

		FileInputStream fins = new FileInputStream(sZipPathFile);

		ZipInputStream zins = new ZipInputStream(fins, Charset.forName("gbk"));
		ZipEntry ze = null;
		byte ch[] = new byte[256];
		while ((ze = zins.getNextEntry()) != null) {
			File zfile = new File(sDestPath + "//" + ze.getName());
			File fpath = new File(zfile.getParentFile().getPath());
			if (ze.isDirectory()) {
				if (!zfile.exists())
					zfile.mkdirs();
				zins.closeEntry();
			} else {
				if (!fpath.exists())
					fpath.mkdirs();
				FileOutputStream fouts = new FileOutputStream(zfile);
				int i;
				String zfilePath = zfile.getAbsolutePath();
				while ((i = zins.read(ch)) != -1)
					fouts.write(ch, 0, i);
				zins.closeEntry();
				fouts.close();

				if (zfilePath.endsWith(".zip")) {
					unZipFile(zfilePath, zfilePath.substring(0, zfilePath.lastIndexOf(".zip")));
				}

			}
		}
		fins.close();
		zins.close();
		// if necessary, delete original zip-file
		File file = new File(sZipPathFile);
		file.delete();
	}

	/**
	 * 创建目录
	 * 
	 * @param strPath
	 *            路径
	 */
	public static void createDirectory(String strPath) {
		File fileDirectory = new File(strPath);
		if (!fileDirectory.isDirectory()) { // 目录不存在
			String[] aPathSegments = strPath.split("/");
			String strWalkThroughPath = "/";
			for (int i = 0; i < aPathSegments.length; i++) {
				strWalkThroughPath = strWalkThroughPath + "/" + aPathSegments[i];
				fileDirectory = new File(strWalkThroughPath);
				if (!fileDirectory.isDirectory()) {
					fileDirectory.mkdir();
				}
			}
		}
	}

	/**
	 * 删除指定路径所有文件
	 * 
	 */
	public void deleteDirectory(String strPath) {

		File file = new File(strPath);
		File[] files = file.listFiles();// 批量删除文件
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {

				files[i].delete();

			} else if (files[i].isDirectory()) {

				deleteDirectory(files[i].getAbsolutePath());

				files[i].delete();
			}
		}
		file.delete();
	}

}
