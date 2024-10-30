package cn.itcast.hotel.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveNumber {
    public static void main(String[] args) throws IOException {
        // 示例输入文本
        String filePath = "C:\\Users\\1\\Downloads\\1.docx";

        String content = readWordDocument(filePath);

        // 删除每行中超过 5 位的数字
        String result = removeLongNumbers(content);


        // 将修改后的内容写入新的 Word 文档
        writeWordDocument(result, filePath);
    }

    public static String removeLongNumbers(String text) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n");

        // 遍历每一行
        for (String line : lines) {
            // 使用正则表达式匹配数字，并且将超过12位并且下一位是0的数字替换为空字符串
            String pattern = "\\b\\d{12}0\\b";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(line);
            line = m.replaceAll("");

            // 将处理后的行添加到结果中
            result.append(line).append("\n");



        }

        return result.toString().trim().replaceAll("\n","");
    }



    public static String readWordDocument(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument doc = new XWPFDocument(fis)) {
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
        }
        return content.toString();
    }


    public static void writeWordDocument(String content, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(content);
            doc.write(fos);
        }
    }
}
