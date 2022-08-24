package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.checkerframework.checker.units.qual.A;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credential.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     * https://docs.google.com/spreadsheets/d/1NWuVUsyykqTCsU9fW8h9Btiu4Mh4njfhTpkyIducbgk/edit#gid=0
     */
    public static void main(String... args) throws IOException, GeneralSecurityException, java.text.ParseException {
        ArrayList<InfoTab> infoTabs = new ArrayList<InfoTab>();
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1NWuVUsyykqTCsU9fW8h9Btiu4Mh4njfhTpkyIducbgk";
        final String readingRange = "b1:j3";
        final String writtingRange = "a4:d1000";
        final String pattern = "dd.MM.yyyy";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        final String userAgent = "Chrome/4.0.249.0";
        final String refferer = "http://www.google.com";
        Date now = new Date();
        ArrayList<String> keyWords = new ArrayList<>();
        ArrayList<String> amounts = new ArrayList<>();
        ArrayList<String> regionIds = new ArrayList<>();
        ArrayList<Object> valueList = new ArrayList<>();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, readingRange)
                .execute();
        List<List<Object>> values = response.getValues();
        ValueRange responseW = service.spreadsheets().values()
                .get(spreadsheetId, writtingRange)
                .execute();
        List<List<Object>> valuesW = responseW.getValues();
        // System.out.println(simpleDateFormat.parse((String) valuesW.get(valuesW.size()-1).get(0)));
        int countR=0;
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
              switch (countR){
                  case (0):
                    keyWords = (ArrayList<String>) row;
                  case (1):
                      amounts =  (ArrayList<String>) row;
                  case (2):
                      regionIds = (ArrayList<String>) row;
              }
              countR++;
            }
        }
        for (int i = 0; i<keyWords.size();i++){
           InfoTab infoTab = new InfoTab();
           infoTab.setKeyWords(URLEncoder.encode(keyWords.get(i), "utf-8"));
           infoTab.setRegionId(regionIds.get(i));
           infoTab.setExpereinceAmount(amounts.get(i));
        infoTabs.add(infoTab);
        }

        for (InfoTab infoTab : infoTabs) {
            //переимменовать данные

        }
        valueList.add(0,simpleDateFormat.format(now));
      //  System.out.println(valueList);
        if (simpleDateFormat.parse((String) valuesW.get(valuesW.size()-1).get(0)).getDate() == now.getDate()){
            List<List<Object>> objs = Arrays.asList(valueList);
            ValueRange appendBody = new ValueRange()
                    .setValues(objs);
            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(spreadsheetId, String.format("a%d",valuesW.size()+3), appendBody)
                    .setValueInputOption("RAW")
                    .execute();
        }
        else {
            List<List<Object>> objs = Arrays.asList(valueList);
            ValueRange appendBody = new ValueRange()
                    .setValues(objs);
            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(spreadsheetId, String.format("a%d",valuesW.size()+4), appendBody)
                    .setValueInputOption("RAW")
                    .execute();
        }


        //переименовать дату, найти строчку, поменять параметры добавить методы

    }

    public static double GetValue(ArrayList<VacancyData> vacancyDataArrayList) {
        int j = 0;
        double[] sredZ = new double[vacancyDataArrayList.size() - 1];//?????? ?? ???? ??
        int[] counts = new int[vacancyDataArrayList.size() - 1];//?????? ??????? E
        double[] fMas = new double[vacancyDataArrayList.size() - 1];//?????? ??????? F
        double[] sums = new double[vacancyDataArrayList.size()];
        int[] nums = new int[vacancyDataArrayList.size()-1];
        for (VacancyData db : vacancyDataArrayList) {
            sums[j] = db.getZp();
            j += 1;
        }
        j = 0;

        for (VacancyData db : vacancyDataArrayList) {
            while (j!=nums.length-1){
                nums[j] = db.getVacancyAmount();
                j += 1;
            }

        }
        for (int i = 0; i < sums.length - 1; i++) {
            sredZ[i] = (sums[i] + sums[i + 1]) / 2;
        }
        for (int i = 0; i < nums.length - 1; i++) {
            counts[i] = nums[i] - nums[i + 1];
        }
        for (int i = 0; i < nums.length - 1; i++) {
            fMas[i] = sums[i] * counts[i];
        }
        return Math.round(DoubleStream.of(fMas).sum() / IntStream.of(counts).sum());
    }

    public static String GetUrl(InfoTab infoTab) {
        String workExp = new String();
        switch (infoTab.getExpereinceAmount()) {
            //?????????? ?? 12345
            case "1":
                workExp = "";
                break;
            case "2":
                workExp = "between1And3";
                break;
            case "3":
                workExp = "noExperience";
                break;
            case "4":
                workExp = "between3And6";
                break;
            case "5":
                workExp = "moreThan6";
                break;
            default:
                System.out.println("0");

        }
        return String.format("https://hh.ru/search/vacancy?text=%s&area=%s&experience=%s&search_field=name&search_field=company_name&search_field=description&clusters=true&ored_clusters=true&enable_snippets=true", infoTab.getKeyWords(), infoTab.getRegionId(), workExp);
    }

}

