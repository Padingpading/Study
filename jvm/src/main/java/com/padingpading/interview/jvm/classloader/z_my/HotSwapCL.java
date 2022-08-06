package com.padingpading.interview.jvm.classloader.z_my;

import com.padingpading.interview.jvm.classloader.b_cus.load.MyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * @author libin
 * @description
 * @date 2021-07-30
 */

@Slf4j
public class HotSwapCL extends ClassLoader {

    /**
     * 需要该类加载器直接加载的类文件的基目录
     */

    private String basedir;

    /**
     * 需要由该类加载器直接加载的类名
     */
    private HashMap<String, Class<?>> loadedClasses;

    private String packageName;

    /**
     * 默认推荐  HotSwapCL(String basedir, String packageName, String... clazzNames)
     * 此构造函数 慎用
     * 初始化并加载类
     *
     * @param basedir 基目录
     */
    public HotSwapCL(String basedir) {
        this(basedir, "", "");
    }

    /**
     * 初始化并加载类
     *
     * @param basedir     基目录
     * @param packageName 类所在包名
     * @param clazzNames  多个类需要在同一包下
     */
    public HotSwapCL(String basedir, String packageName, String... clazzNames) {
        super(null);
        loadedClasses = new HashMap<>();
        this.basedir = basedir;
        init(packageName, clazzNames);
    }

    /**
     * 初始化
     *
     * @param packageName
     * @param clazzNames
     */
    public void init(String packageName, String... clazzNames) {
        this.packageName = packageName;
        try {
            loadClassByMe(clazzNames);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void loadClassByMe(String... classNames) {
        if (StringUtils.isEmpty(classNames) || classNames[0].isEmpty()) {
            log.error("classNames is null,check your code");
            throw new IllegalArgumentException("非法");
        }

        for (String className : classNames) {
            Class<?> aClass = loadDirectly(className);
            if (aClass == null) {
                log.error("load class failed:" + className);
                continue;
            }
            loadedClasses.put(packageName + "." + className, aClass);
        }
    }

    private Class<?> loadDirectly(String className) {
        Class<?> cls = null;
        try {
            if (StringUtils.isEmpty(className)) {
                log.info("className is null");
                return null;
            }

            String classPath = basedir + File.separator + packageName.replace('.', File.separatorChar) + File.separator + className + ".class";
            log.info("load class:" + classPath);
            File classF = new File(classPath);
            try (FileInputStream inputStream = new FileInputStream(classF)) {
                cls = instantiateClass(packageName + "." + className, inputStream);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return cls;
    }

    private Class<?> instantiateClass(String name, InputStream fin) throws IOException {
        byte[] data = new byte[fin.available()];
        Class<?> aClass = null;
        int count = fin.read(data);
        if (count == 0) {
            log.info("instantiateClass read no byte");
        }
        try {
            aClass = defineClass(name, data, 0, data.length);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        return aClass;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> cls;
        synchronized (getClassLoadingLock(name)) {

            if (loadedClasses.containsKey(name)) {
                cls = loadedClasses.get(name);
            } else {
                cls = findLoadedClass(name);
                cls = cls == null ? MyClassLoader.class.getClassLoader().loadClass(name) : cls;
            }

            if (cls == null) {
                throw new ClassNotFoundException(name);
            }

            if (resolve) {
                resolveClass(cls);
            }
            return cls;
        }
    }

}