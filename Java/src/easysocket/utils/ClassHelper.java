package easysocket.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassHelper {

	/**
	 * 从包package�?��?��??�的Class
	 * 
	 * @param pack
	 * @return
	 */
	public static Set<Class<?>> getClasses(String pack) {

		// 第�?个class类的?�合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// ??��循环�?��
		boolean recursive = true;
		// ?�取?�的?�字 并进行替??
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// 定义�?��?�举?�集??并进行循??��处理这个??��下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader()
					.getResources(packageDirName);
			// 循环�?��下去
			while (dirs.hasMoreElements()) {
				// ?�取下�?个元�?
				URL url = dirs.nextElement();
				// 得到?��??�名�?
				String protocol = url.getProtocol();
				// 如果??��?�件?�形式保存在?�务?�上
				if ("file".equals(protocol)) {
					// ?�取?�的?�理�?��
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的?�式?�描?�个?�下?�文�?并添?�到?�合�?
					findAndAddClassesInPackageByFile(packageName, filePath,
							recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果?�jar?�文�?
					// 定义�?��JarFile
					JarFile jar;
					try {
						// ?�取jar
						jar = ((JarURLConnection) url.openConnection())
								.getJarFile();
						// 从�?jar??得到�?��?�举�?
						Enumeration<JarEntry> entries = jar.entries();
						// ?�样?�进行循??���?
						while (entries.hasMoreElements()) {
							// ?�取jar?�的�?��实体 ??��??���??��?些jar?�里?�其他文�?如META-INF等文�?
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果??��/�?��??
							if (name.charAt(0) == '/') {
								// ?�取?�面?�字符串
								name = name.substring(1);
							}
							// 如果?�半?�分?�定义的?�名?�同
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// 如果�?/"结尾 ???个包
								if (idx != -1) {
									// ?�取?�名 ??/"?�换??."
									packageName = name.substring(0, idx)
											.replace('/', '.');
								}
								// 如果??���?��下去 并且???个包
								if ((idx != -1) || recursive) {
									// 如果???�?class?�件 ?�且不是??��
									if (name.endsWith(".class")
											&& !entry.isDirectory()) {
										// ?�掉?�面??.class" ?�取?��??�类??
										String className = name.substring(
												packageName.length() + 1,
												name.length() - 6);
										try {
											// 添加?�classes
											classes.add(Class
													.forName(packageName + '.'
															+ className));
										} catch (ClassNotFoundException e) {
											// log
											// .error("添加?�户?�定义视?�类?��? ?�不?��?类的.class?�件");
											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						// log.error("?�扫?�用?�定义视?�时从jar?�获?�文件出??);
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * 以文件的形式?�获?�包下的??��Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassesInPackageByFile(String packageName,
			String packagePath, final boolean recursive, Set<Class<?>> classes) {
		// ?�取此包?�目�?建立�?��File
		File dir = new File(packagePath);
		// 如果不存?�或??也不??��录就?�接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("?�户定义?�名 " + packageName + " 下没?�任何文�?);
			return;
		}
		// 如果存在 就获?�包下的??��?�件 ?�括??��
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// ?�定义过滤规??如果??��循环(?�含子目�? ?�则??��.class结尾?�文�?编译好的java类文�?
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
						|| (file.getName().endsWith(".class"));
			}
		});
		// 循环??��?�件
		for (File file : dirfiles) {
			// 如果??���??�继�?��??
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(
						packageName + "." + file.getName(),
						file.getAbsolutePath(), recursive, classes);
			} else {
				// 如果?�java类文�??�掉?�面??class ?�留下类??
				String className = file.getName().substring(0,
						file.getName().length() - 6);
				try {
					// 添加?�集?�中??
					// classes.add(Class.forName(packageName + '.' +
					// className));
					// 经过?�复?��??�提?�，这里?�forName?��?些不好，会触?�static?�法，没?�使?�classLoader?�load干�?
					classes.add(Thread.currentThread().getContextClassLoader()
							.loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					// log.error("添加?�户?�定义视?�类?��? ?�不?��?类的.class?�件");
					e.printStackTrace();
				}
			}
		}
	}

}
