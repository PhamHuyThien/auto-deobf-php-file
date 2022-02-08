package com.thiendz.tool.autodeobfphpfile;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

public class AutoDeObfPhpFile {
    public static void main(String[] args) throws IOException {

        System.setProperty("webdriver.chrome.driver", "D:\\thienph\\java\\auto-deobf-php-file\\chromedriver.exe");
        WebDriver webDriver = new ChromeDriver();

        Scanner sc = new Scanner(System.in);
        System.out.print("Nhap path moi: ");
        String pathAccess = sc.nextLine();

        boolean exit = true;
        do {

            System.out.print("Nhap ten file: ");
            String fileName = sc.nextLine();
            if (fileName.equals("changePath")) {
                System.out.print("Nhap path moi: ");
                pathAccess = sc.nextLine();
            } else if (fileName.equals("exit")) {
                exit = false;
            } else {
                String path = pathAccess + fileName + ".php";

                String pathFile = "D:\\Xampp\\htdocs" + path;
                String runScript = "http://localhost/" + path;

                File file = new File(pathFile);
                String textFile = readFromInputStream(new FileInputStream(file));
                textFile = textFile.replace("eval", "echo");
                writeFile(file, textFile);

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
        } while (exit);
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
