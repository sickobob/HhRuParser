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
import com.google.api.client.util.Data;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.http.client.utils.DateUtils;
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
    static final String basedUrl = "https://hh.ru/search/vacancy?text=%s&area=%s&experience=%s&search_field=name&search_field=company_name&search_field=description&clusters=true&ored_clusters=true&enable_snippets=true";
    static final String spreadsheetId = "1NWuVUsyykqTCsU9fW8h9Btiu4Mh4njfhTpkyIducbgk";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static String[] workExp = {"", "noExperience", "between1And3", "between3And6", "moreThan6"};
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credential.json";
    final static String userAgent = "Chrome/4.0.249.0";
    final static String refferer = "http://www.google.com";
    final static String cssForParsing = "div.novafilters >div:nth-child(6)>div.novafilters-group-wrapper>div.novafilters-group__items>li>label";
    final static String spaceChar = "?";
    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
        ArrayList<InfoTab> infoTabs = new ArrayList<>();
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String readingRange = "b1:s3";
        final String writtingRange = "a4:d1000";
        final String pattern = "dd.MM.yyyy";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
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
        int countR = 0;
        for (List row : values) {
            switch (countR) {
                case (0):
                    keyWords = (ArrayList<String>) row;
                case (1):
                    amounts = (ArrayList<String>) row;
                case (2):
                    regionIds = (ArrayList<String>) row;
            }
            countR++;
        }
        for (int i = 0; i < keyWords.size(); i++) {
            InfoTab infoTab = new InfoTab();
            infoTab.setKeyWords(URLEncoder.encode(keyWords.get(i), "utf-8"));
            infoTab.setRegionId(regionIds.get(i));
            infoTab.setExpereinceAmount(amounts.get(i));
            infoTabs.add(infoTab);
        }
        for (InfoTab infoTab : infoTabs) {
            if (fetchVacancyData(buildUrl(infoTab)).size() == 1)
                valueList.add(0);
            else valueList.add(calculateValue(fetchVacancyData(buildUrl(infoTab))));

        }


        valueList.add(0, LocalDate.now().format(formatter));
        LocalDate lastDate = LocalDate.parse((String) valuesW.get(valuesW.size() - 1).get(0));
        if (lastDate.equals(now)) {
            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(spreadsheetId, String.format("a%d", valuesW.size() + 3), assemblyRange(valueList))
                    .setValueInputOption("RAW")
                    .execute();
        } else {

            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(spreadsheetId, String.format("a%d", valuesW.size() + 4), assemblyRange(valueList))
                    .setValueInputOption("RAW")
                    .execute();
        }
        //������������� ����, ����� �������, �������� ��������� �������� ������

    }

    public static double calculateValue(ArrayList<VacancyData> vacancyDataArrayList) {

        double[] sredZ = new double[vacancyDataArrayList.size() - 1];//column D never used????
        int[] columnE = new int[vacancyDataArrayList.size() - 1];//column  E
        double[] columnF = new double[vacancyDataArrayList.size() - 1];//column F
        for (int i = 0; i < vacancyDataArrayList.size() - 2; i++) {
            sredZ[i] = (vacancyDataArrayList.get(i).getZp() + vacancyDataArrayList.get(i + 1).getZp()) / 2;
        }
        for (int i = 0; i < vacancyDataArrayList.size() - 2; i++) {
            columnE[i] = vacancyDataArrayList.get(i).getVacancyAmount() - vacancyDataArrayList.get(i + 1).getVacancyAmount();
        }
        for (int i = 0; i < vacancyDataArrayList.size() - 2; i++) {
            columnF[i] = vacancyDataArrayList.get(i).getZp() * columnE[i];
        }
        return DoubleStream.of(columnF).sum() / IntStream.of(columnE).sum();
    }

    public static String buildUrl(InfoTab infoTab) {
        if (Integer.parseInt(infoTab.getExpereinceAmount()) > 5) return basedUrl;
        return String.format(basedUrl, infoTab.getKeyWords(), infoTab.getRegionId(), workExp[Integer.parseInt(infoTab.getExpereinceAmount()) - 1]);
    }

    public static ArrayList<VacancyData> fetchVacancyData(String url) throws java.io.IOException {
        ArrayList<VacancyData> vacancyDataArrayList = new ArrayList<>();
        Document doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer(refferer)
                .get();
        Elements listNews = doc.select(cssForParsing);
        for (Element element : listNews) {
            if (!element.select("input").attr("value").isEmpty() && !element.select("span>span:nth-child(2)").text().isEmpty()) {
                double zp = Double.parseDouble(element.select("input").attr("value"));
                int amount = Integer.parseInt(element.select("span>span:nth-child(2)").text().replace(spaceChar, ""));
                VacancyData vacancyData = new VacancyData(zp, amount);
                vacancyDataArrayList.add(vacancyData);
            }
        }
        if (vacancyDataArrayList.size() != 0) {
            VacancyData vacancyData = new VacancyData(vacancyDataArrayList.get(vacancyDataArrayList.size() - 1).getZp() * 1.2, 0);
            vacancyDataArrayList.add(vacancyData);
            return vacancyDataArrayList;
        } else {
            vacancyDataArrayList.add(new VacancyData(0, 0));
            return vacancyDataArrayList;
        }

    }

    static ValueRange assemblyRange(ArrayList<Object> valueList) {
        List<List<Object>> objs = Arrays.asList(valueList);
        ValueRange appendBody = new ValueRange()
                .setValues(objs);
        return appendBody;
    }
    static boolean compareDates(LocalDate parseDate, LocalDate now){
        if(parseDate.equals(now)) return true;
        else return false;
    }



}

