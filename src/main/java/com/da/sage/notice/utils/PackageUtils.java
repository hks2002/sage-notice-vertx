/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-04-17 14:58:48                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-02 15:40:44                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PackageUtils {

  /**
   * Only used when running in jar package to get all Class objects under the
   * specified package.
   *
   * @param packageName for example "com.da.docs"
   * @return List of Class<?>
   */
  public static List<Class<?>> getClassesInJarPackage(String packageName) {
    List<Class<?>> classes = new ArrayList<>();
    String path = packageName.replace('.', '/');
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      Enumeration<URL> resources = classLoader.getResources(path);
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        String protocol = resource.getProtocol();

        if ("file".equals(protocol)) {
          File directory = new File(resource.getFile());
          if (directory.exists()) {
            File[] files = directory.listFiles(file -> file.isFile() && file.getName().endsWith(".class"));
            if (files != null) {
              for (File file : files) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                  classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                  log.warn("Class not found: {}", className, e);
                }
              }
            }
          }
        } else if ("jar".equals(protocol)) {
          JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
          JarFile jarFile = jarConn.getJarFile();
          Enumeration<JarEntry> entries = jarFile.entries();
          while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
              String className = name.replace('/', '.').substring(0, name.length() - 6);
              try {
                classes.add(Class.forName(className));
              } catch (ClassNotFoundException e) {
                log.warn("Class not found: {}", className, e);
              }
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Can't get jar package resource: " + packageName, e);
    }
    return classes;
  }

}