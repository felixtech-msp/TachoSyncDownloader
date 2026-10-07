package io.felixtech.teltonika.tachosync;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static io.felixtech.teltonika.tachosync.Utils.getCause;

public final class Downloader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private static final String DRIVER_FILES_URL = "https://api.tacho.teltonika.lt/v1/DriverFiles";
    private static final String VEHICLE_FILES_URL = "https://api.tacho.teltonika.lt/v1/VehicleFiles";

    public void run() {
        String targetDirectory = Settings.getInstance().get("TARGET_DIRECTORY");
        String successDirectory = Settings.getInstance().get("SUCCESS_DIRECTORY", targetDirectory);

        try {
            JSONArray driverFiles = getPages(DRIVER_FILES_URL);

            for (Object entry : driverFiles) {
                JSONObject driverFile = (JSONObject) entry;
                String id = driverFile.getString("id");
                String fileName = driverFile.getString("fileName");

                if (Files.exists(Paths.get(successDirectory, fileName)))
                    continue;

                downloadFile(DRIVER_FILES_URL, id, fileName);
                logger.info("Downloaded driver file: {}", fileName);
            }
        } catch (Throwable t) {
            logger.error("Unable to process driver files: {}", getCause(t).getMessage(), t);
        }

        try {
            JSONArray vehicleFiles = getPages(VEHICLE_FILES_URL);

            for (Object entry : vehicleFiles) {
                JSONObject vehicleFile = (JSONObject) entry;
                String id = vehicleFile.getString("id");
                String fileName = vehicleFile.getString("fileName");

                if (Files.exists(Paths.get(successDirectory, fileName)))
                    continue;

                downloadFile(VEHICLE_FILES_URL, id, fileName);
                logger.info("Downloaded vehicle file: {}", fileName);
            }
        } catch (Throwable t) {
            logger.error("Unable to process vehicle files: {}", getCause(t).getMessage(), t);
        }
        
        logger.info("Run completed");
    }

    private static JSONArray getPages(String url) throws IOException, ParseException {
        String apiKey = Settings.getInstance().get("API_KEY");
        String companyId = Settings.getInstance().get("COMPANY");

        JSONArray allData = new JSONArray();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            int totalPages = 1;

            for (int page = 1; page <= totalPages; page++) {
                HttpGet request = new HttpGet(url + "?PageNumber=" + page + "&PageSize=100" + "&CompanyId=" + companyId);
                request.setHeader("X-Api-Key", apiKey);
                request.setHeader("Accept", "application/json");

                String response = EntityUtils.toString(client.execute(request).getEntity());
                JSONObject json = new JSONObject(response);

                if (page == 1) {
                    totalPages = json.getInt("totalPages");
                }

                JSONArray items = json.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    allData.put(items.get(i));
                }
            }
        }

        return allData;
    }

    private static void downloadFile(String url, String id, String fileName) throws IOException {
        String apiKey = Settings.getInstance().get("API_KEY");
        String targetDirectory = Settings.getInstance().get("TARGET_DIRECTORY");

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(url + "/Download?Ids=" + id);
            request.setHeader("X-Api-Key", apiKey);
            request.setHeader("Accept", "application/octet-stream");

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();

                if (statusCode != 200) {
                    throw new IOException("Download failed. HTTP status: " + statusCode);
                }

                Path targetFile = Paths.get(targetDirectory, fileName);

                try (InputStream input = response.getEntity().getContent();
                     OutputStream output = Files.newOutputStream(targetFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }
}
