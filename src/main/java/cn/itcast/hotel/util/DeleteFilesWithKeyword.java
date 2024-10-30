package cn.itcast.hotel.util;

import java.io.File;

public class DeleteFilesWithKeyword {

    public static void main(String[] args) {
        // 要删除文件的文件夹路径
        String folderPath = "C:\\Users\\1\\Desktop\\phtone\\1\\photos";
        // 指定的关键字
        String keyword = "_thumb";

        // 调用方法删除文件夹中文件名包含指定关键字的文件
        deleteFilesWithKeyword(folderPath, keyword);
    }

    public static void deleteFilesWithKeyword(String folderPath, String keyword) {
        File folder = new File(folderPath);
        // 检查文件夹是否存在
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder path!");
            return;
        }
//        int i=0;

        // 遍历文件夹中的所有文件
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
//                i=i+1;
//                 判断文件名是否包含指定关键字
                if (file.getName().contains(keyword)) {
                    // 删除文件
                    if (file.delete()) {
                        System.out.println("Deleted file: " + file.getAbsolutePath());
                    } else {
                        System.out.println("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
//            System.out.println(i);
        }
    }
}
