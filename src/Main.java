import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Введите URL для загрузки музыки:");
        String musicUrl = inputScanner.nextLine();
        System.out.println("Введите URL для загрузки картинки:");
        String imageUrl = inputScanner.nextLine();

        // Создаем потоки для загрузки музыки и картинки
        Thread musicDownloadThread = new Thread(() -> downloadFileWithProgress(musicUrl, "downloaded_music.mp3"));
        Thread imageDownloadThread = new Thread(() -> downloadFileWithProgress(imageUrl, "downloaded_image.jpg"));

        musicDownloadThread.start();
        imageDownloadThread.start();
    }

    public static void downloadFileWithProgress(String fileUrl, String outputFilePath) {
        try {
            if (!isUrlAccessible(fileUrl)) {
                System.out.println("URL недоступен: " + fileUrl);
                return;
            }

            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int totalFileSize = connection.getContentLength();

            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = new FileOutputStream(outputFilePath)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalBytesDownloaded = 0;
                int lastReportedPercentage = 0;

                // Чтение данных с отображением прогресса
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesDownloaded += bytesRead;

                    int currentPercentage = (int) ((double) totalBytesDownloaded / totalFileSize * 100);
                    if (currentPercentage != lastReportedPercentage) {
                        System.out.println("Загрузка " + outputFilePath + ": " + currentPercentage + "%");
                        lastReportedPercentage = currentPercentage;
                    }
                }

                System.out.println(outputFilePath + " успешно загружен.");
            }

            // Асинхронное открытие файла
            openFileInBackground(outputFilePath);

            // Проверка типа картинки после загрузки
            if (outputFilePath.endsWith("jpg") || outputFilePath.endsWith("jpeg")) {
                String fileType = determineFileType(outputFilePath);
                if (Objects.equals(fileType, "JPEG")) {
                    System.out.println("Тип файла: JPEG");
                } else {
                    System.out.println("Тип файла не совпадает");
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    public static void openFileInBackground(String filePath) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(new File(filePath));
                System.out.println("Открытие файла: " + filePath);
            } catch (IOException e) {
                System.err.println("Не удалось открыть файл: " + filePath);
            }
        }).start();
    }

    public static boolean isUrlAccessible(String fileUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }

    public static String determineFileType(String filename) {  try (FileInputStream fileInputStream = new FileInputStream(filename)) {
        byte[] magicBytes = new byte[4];
        if (fileInputStream.read(magicBytes) != -1) {
            return getFileTypeFromMagicBytes(magicBytes);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
        return null;
    }

    public static String getFileTypeFromMagicBytes(byte[] magicBytes) {
        // Пример определения типа файла по магическим байтам
        // Здесь можно добавить логику для других типов файлов
        if (magicBytes[0] == (byte) 0xFF && magicBytes[1] == (byte) 0xD8) {
            return "JPEG";
        }
        return "UNKNOWN";
    }
}