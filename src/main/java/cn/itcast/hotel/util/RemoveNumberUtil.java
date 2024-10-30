package cn.itcast.hotel.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RemoveNumberUtil {
    public static void main(String[] args) throws IOException {
        // 示例输入文本
        String filePath = "C:\\Users\\1\\Downloads\\1.docx";

        String content = readWordDocument(filePath);

        // 删除每行中超过 12 位且末尾是 0 的数字
        String result = removeLongNumbers(content);

        // 将修改后的内容写入新的 Word 文档
        writeWordDocument(result, filePath);
    }

    public static String removeLongNumbers(String text) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n");

        // 遍历每一行
        for (String line : lines) {
            // 使用逗号分割每行数字，并删除超过 12 位且末尾是 0 的数字
            String[] numbers = line.split(",");
            StringBuilder modifiedLine = new StringBuilder();
            for (String number : numbers) {
                if (number.length() <= 12 && !(number.length() == 12 && number.endsWith("0"))) {
                    modifiedLine.append(number).append(",");
                } else if (number.length() == 13 && number.endsWith("0")) {
                    modifiedLine.append(number, 0, number.length() - 1).append(",");
                }
            }
            if (modifiedLine.length() > 0) {
                modifiedLine.deleteCharAt(modifiedLine.length() - 1); // 删除末尾多余的逗号
                result.append(modifiedLine).append(",");
            }
        }

        return result.toString().trim();
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
