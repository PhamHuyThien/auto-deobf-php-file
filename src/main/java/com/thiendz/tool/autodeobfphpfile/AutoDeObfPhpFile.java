package com.thiendz.tool.autodeobfphpfile;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

public class AutoDeObfPhpFile {
    public static void main(String[] args) throws IOException {
        String path = "\\g88vip\\api\\Account\\getaccountinfo.php";

        String pathFile = "D:\\Xampp\\htdocs" + path;
        String runScript = "http://localhost/" + path;

//        System.out.println(readFromInputStream(new FileInputStream(new File(pathFile))));
        File file = new File(pathFile);
        String textFile = readFromInputStream(new FileInputStream(file));
        textFile = textFile.replace("eval", "echo");
        writeFile(file, textFile);
//        System.exit(0);

        System.setProperty("webdriver.chrome.driver", "D:\\SourceCode\\Java\\auto-deobf-php-file\\chromedriver.exe");

        WebDriver webDriver = new ChromeDriver();
        webDriver.get(runScript);

        textFile = webDriver.getPageSource().replaceAll("<html><head></head><body>", "").replaceAll("</body></html>", "");
        writeFile(file, "<?php " + textFile);

        String regex = "eval\\(\\$.+\\((\\$.+)\\)\\);";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(textFile);
        if (m.find()) {
            String varRegex = m.group();
            int id = varRegex.lastIndexOf("($") + 1;
            String var = varRegex.substring(id, varRegex.lastIndexOf(")") - 1);
            System.out.println(varRegex);
            textFile = textFile.substring(0, textFile.length() - varRegex.length());
            textFile += " echo base64_decode(" + var + ");";
            textFile = "<?php " + textFile.replaceAll("\\n", "");
            textFile = StringEscapeUtils.unescapeHtml4(textFile);
            writeFile(file, textFile);
            webDriver.navigate().refresh();

            textFile = webDriver.getPageSource().replaceAll("<html><head></head><body>", "").replaceAll("</body></html>", "");
            textFile = textFile.substring(10, textFile.length() - 12);
            textFile = StringEscapeUtils.unescapeHtml4(textFile);
            System.out.println(textFile);
            writeFile(file, "<?" + textFile);

        }
    }

    private static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static boolean writeFile(File file, String text) {
        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(text);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
