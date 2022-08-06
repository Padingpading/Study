package com.padingpading.interview.jvm.classloader.z_my;

/**
 * @author libin
 * @description
 * @date 2021-07-30
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.http11.Constants;
import org.springframework.util.StringUtils;
import org.testcontainers.shaded.org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.testcontainers.shaded.org.apache.commons.io.monitor.FileAlterationMonitor;
import org.testcontainers.shaded.org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 插件管理类。内部已实现读写锁机制
 *
 * @author King
 * @update 2018/11/20 9:38
 */
@Slf4j
public class PluginManager {
    private static final int MAX_LOCK_WAITED_TIME = 3;//获取读写锁的最大时间，单位：分钟
    private static final ReentrantReadWriteLock rrwLock = new ReentrantReadWriteLock();//可重入读写锁
    private static final ConcurrentHashMap<String, Class<AuthorizedCrawler>> loadedSites = new ConcurrentHashMap<>();
    //插件自动更新管理器变量
    private static boolean needUpdate = false;
    private static long updateTime = System.currentTimeMillis();


    static {
        reload();
        startUpdateManger();
        startUpdateMonitor();
    }

    private static void startUpdateManger() {
        log.info("开始启动插件自动更新管理器...");
        new Thread(() -> {
            log.info("插件自动更新管理器启动成功！");
            while (true) {
                try {
                    if (needUpdate) {
                        long nowTime = System.currentTimeMillis();
                        long endTime = updateTime + 10 * 1000;
                        if (nowTime >= endTime) {
                            try {
                                rrwLock.writeLock().lock();
                                log.info("插件自动更新管理器检测到插件更新，将重载插件...");
                                PluginManager.reload();
                                needUpdate = false;
                                log.info("插件自动更新管理器重载插件成功！");
                            } finally {
                                rrwLock.writeLock().unlock();
                            }
                        }
                    }
                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.error("插件自动更新管理器异常, 自动退出.", e);
                    break;
                }
            }
        }).start();
    }

    private static void startUpdateMonitor() {
        try {
            log.info("开始启动文件监听器...");
            // 使用过滤器
            FileAlterationObserver observer = new FileAlterationObserver(Path.UPLOAD_DIR);
            observer.addListener(new FileAlterationListenerAdaptor() {
                @Override
                public void onFileCreate(File file) {
                    onFileChange(file);
                }

                @Override
                public void onFileChange(File file) {
                    try {
                        rrwLock.readLock().lock();
                        updateTime = System.currentTimeMillis();
                        needUpdate = true;
                    } finally {
                        rrwLock.readLock().unlock();
                    }
                }
            });
            //创建文件变化监听器
            FileAlterationMonitor monitor = new FileAlterationMonitor(60000, observer);
            // 开始监控
            monitor.start();
            log.info("文件监听器启动成功！");
        } catch (Exception e) {
            log.error("文件监听器启动异常：" + e);
        }
    }

    /**
     * @desc: 重新加载插件
     * @author: YuYangjun
     * @date: 2018/9/14 上午9:11
     */
    public static boolean reload() {
        boolean success = false;
        log.info("开始重载插件...");
        try {
            if (rrwLock.writeLock().tryLock(MAX_LOCK_WAITED_TIME, TimeUnit.MINUTES)) {
                loadedSites.clear();//清空缓存
                File file = new File(Path.UPLOAD_DIR);
                if (!file.isDirectory()) {
                    log.error("upload dir is not exists");
                    return false;
                }

                loadClass(readFiles(file));
                success = true;
            }
        } catch (InterruptedException e) {
            log.error("{}分钟内获取写锁失败：{}", MAX_LOCK_WAITED_TIME, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("插件重载异常：{}", e);
        } finally {
            if (rrwLock.writeLock().isHeldByCurrentThread()) {
                rrwLock.writeLock().unlock();
            }
            ConcurrentHashMap.KeySetView<?, ?> temp = loadedSites.keySet();
            log.info("重载插件结束。已载入插件集合[{}]:{}", temp.size(), temp);
        }
        return success;
    }

    private static void loadClass(List<File> files) {
        Map<String, Set<String>> classMap = new HashMap<>();
        for (File f : files) {
            String fileName = f.getName();
            String packageName = f.getPath().replace(Path.UPLOAD_DIR + File.separator, "").replace(File.separator + fileName, "").replace(File.separatorChar, '.');
            String classNameWithoutPackage = fileName.replace(".class", "");
            Set<String> classes = classMap.get(packageName);
            if (StringUtils.isEmpty(classes)) {
                classes = new HashSet<>();
                classes.add(classNameWithoutPackage);
                classMap.put(packageName, classes);
            } else {
                classes.add(classNameWithoutPackage);
                classMap.put(packageName, classes);
            }
        }
        loadClassFromMap(classMap);
    }

    private static List<File> readFiles(File file) {
        if (file.isHidden()) {
            return Collections.emptyList();
        }

        List<File> list = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files) {
                return Collections.emptyList();
            }

            for (File f : files) {
                List<File> read = readFiles(f);
                if (read == null) {
                    continue;
                }
                list.addAll(read);
            }
        } else if (file.getName().endsWith(".class")) {
            list.add(file);
        }
        return list;
    }

    /**
     * @desc: 获取插件
     * @param: key
     * @author: YuYangjun
     * @date: 2018/9/14 上午9:11
     */
    public static AuthorizedCrawler get(String key) {
        try {
            if (StringUtils.isEmpty(key)) {
                return null;
            }
            if (rrwLock.readLock().tryLock(MAX_LOCK_WAITED_TIME, TimeUnit.MINUTES)) {
                try {
                    Class<?> crawlerClass = loadedSites.get(key);
                    if (crawlerClass == null) {
                        return null;
                    }
                    return (AuthorizedCrawler) crawlerClass.newInstance();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    rrwLock.readLock().unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("{}分钟内获取读锁失败：{}", MAX_LOCK_WAITED_TIME, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static void loadClassFromMap(Map<String, Set<String>> classMap) {
        Map<String, HashSet<String>> sendEmailMap = new HashMap<>();
        try {
            if (rrwLock.writeLock().tryLock(MAX_LOCK_WAITED_TIME, TimeUnit.MINUTES)) {
                try {
                    classMap.forEach((packageName, classNames) -> {
                        HashSet<String> classNameSet = new HashSet<>();
                        HotSwapCL hotSwapCL = new HotSwapCL(Path.UPLOAD_DIR, packageName, classNames.toArray(new String[0]));
                        classNames.forEach((className) -> {
                            try {
                                checkAndSaveClass(hotSwapCL, packageName, className);
                            } catch (Exception e) {
                                log.error("加载类出现错误:{}" + e.getMessage(), className, e);
                                classNameSet.add(className);
                            }
                        });
                        if (!classNameSet.isEmpty()) {
                            sendEmailMap.put(packageName, classNameSet);
                        }
                    });
                } catch (Throwable e) {
                    log.error("文件反射到类异常：{}", e);
                   // MailUtil.sendMail("告警：qz-crawler插件加载异常", "文件反射到类异常", MailMessageType.WARN);
                } finally {
                    rrwLock.writeLock().unlock();
                    if (!sendEmailMap.isEmpty()) {
                        sendEmail(sendEmailMap);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("{}分钟内获取写锁失败：{}", MAX_LOCK_WAITED_TIME, e);
            Thread.currentThread().interrupt();
        }
    }

    private static void sendEmail(Map<String, HashSet<String>> sendEmailMap) {
        StringBuffer content = new StringBuffer();
        Set<String> keys = sendEmailMap.keySet();
        for (String packgeName : keys) {
            HashSet<String> classNameSet = sendEmailMap.get(packgeName);
            content.append("Package:" + packgeName + Constants.LF);
            classNameSet.forEach((className) -> {
                content.append(className + Constants.LF);
            });
        }
       // MailUtil.sendMail("告警：qz-crawler插件加载异常", content.toString(), MailMessageType.WARN);
    }

    /**
     * 检查并添加class
     *
     * @param packageName
     * @param className
     * @throws ClassNotFoundException
     */
    private static void checkAndSaveClass(HotSwapCL hotSwapCL, String packageName, String className) throws ClassNotFoundException {
        Class<?> aClass = hotSwapCL.loadClass(packageName + "." + className);
//        if (!Modifier.isAbstract(aClass.getModifiers()) && !Modifier.isInterface(aClass.getModifiers()) && AuthorizedCrawler.class.isAssignableFrom(aClass)) {
//            SitesInfo info = aClass.getAnnotation(SitesInfo.class);
//            if (info != null) {
//                loadedSites.put(info.value().getName(), (Class<AuthorizedCrawler>) aClass);
//            } else {
//                log.error("sitesInfo is null,check your code :" + aClass.getName());
//            }
//        }
    }

    public static void loadClassFromFile(File file) {
        try {
            if (rrwLock.writeLock().tryLock(MAX_LOCK_WAITED_TIME, TimeUnit.MINUTES)) {
                try {
                    String fileName = file.getName();
                    String packageName = file.getPath().replace(Path.UPLOAD_DIR + File.separator, "").replace(File.separator + fileName, "").replace(File.separatorChar, '.');
                    String classNameWithoutPackage = fileName.replace(".class", "");
                    HotSwapCL hotSwapCL = new HotSwapCL(Path.UPLOAD_DIR, packageName, classNameWithoutPackage);
                    checkAndSaveClass(hotSwapCL, packageName, classNameWithoutPackage);
                } catch (Exception e) {
                    log.error("文件反射到类异常：{}", e);
                } finally {
                    rrwLock.writeLock().unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("{}分钟内获取写锁失败：{}", MAX_LOCK_WAITED_TIME, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取已加载网站集合副本
     *
     * @return java.util.HashMap<java.lang.Stringom.xinyan.qz.crawler.plugin.AbstractCrawler>>
     * @author King
     * @update 2018/11/19 19:41
     */
    public static HashMap<String, Class<AuthorizedCrawler>> getLoadedSitesCopy() {
        HashMap<String, Class<AuthorizedCrawler>> map = new HashMap<>();
        try {
            if (rrwLock.readLock().tryLock(MAX_LOCK_WAITED_TIME, TimeUnit.MINUTES)) {
                try {
                    for (String site : loadedSites.keySet()) {
                        map.put(site, loadedSites.get(site));
                    }
                } finally {
                    rrwLock.readLock().unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("{}分钟内获取读锁失败：{}", MAX_LOCK_WAITED_TIME, e);
            Thread.currentThread().interrupt();
        }
        return map;
    }
}