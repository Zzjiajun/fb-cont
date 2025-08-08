package cn.itcast.hotel.util;

import java.util.Locale;

public class LanguageUtil {
    /**
     * 将语言代码（如 en、zh-CN、fr-CA）转为中文名称
     */
    public static String getChineseLanguageName(String userLanguage) {
        if (userLanguage == null || userLanguage.isEmpty()) {
            return userLanguage;
        }
        // 兼容 zh-CN、en_US、fr 等格式
        String[] parts = userLanguage.split("[-_]", 2);
        String language = parts[0];
        String country = parts.length > 1 ? parts[1] : "";

        Locale locale = country.isEmpty() ? new Locale(language) : new Locale(language, country);
        String languageName = locale.getDisplayLanguage(Locale.CHINESE);
        String countryName = country.isEmpty() ? "" : locale.getDisplayCountry(Locale.CHINESE);

        // 处理未知语言
        if (languageName == null || languageName.equals(language)) {
            return userLanguage;
        }
        return countryName.isEmpty() ? languageName : languageName + "（" + countryName + "）";
    }


    public static void main(String[] args) {
        String userLanguage = "zh";
        String chineseLanguageName = getChineseLanguageName(userLanguage);
        System.out.println(chineseLanguageName);
    }
}
