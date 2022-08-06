package com.padingpading.interview.jvm.classloader.z_my;

import java.io.File;

/**
 * @author libin
 * @description
 * @date 2021-07-30
 */

public class Path {
    /**
     * 上传文件所在根目录
     */
    public static final String UPLOAD_DIR = File.separator + "data" + File.separator + "upload";
    // public static final String UPLOAD_DIR = "D:\\work\\data\\upload";

    private static final String PACKAGE_NAME = Path.class.getPackage().getName();
    /**
     * 基包名
     */
    public static final String BASE_PACKAGE_NAME = PACKAGE_NAME.substring(0, PACKAGE_NAME.lastIndexOf("."));

    /**
     * com.jxl.jcrawler
     *
     * @return
     */
    public static final String getBasePackagePath() {
        return UPLOAD_DIR + File.separator + BASE_PACKAGE_NAME.replace('.', File.separatorChar);
    }
}