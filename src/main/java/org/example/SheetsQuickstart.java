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
import com.google.api.services.sheets.v4.model.ValueRange;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "src/main/java/resources/credential.json";

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
    public static void main(String... args) throws IOException, GeneralSecurityException {
        ArrayList<Data> datas = new ArrayList<Data>();

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1NWuVUsyykqTCsU9fW8h9Btiu4Mh4njfhTpkyIducbgk";
        final String readingRange = "g2:i4";
        final String writtingRange = "g2:i4";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, readingRange)
                .execute();
        List<List<Object>> values = response.getValues();
        int arrSize = values.size();
        for (int i = 0; i < arrSize; i++) {
            Data data = new Data();
            datas.add(data);
        }
        ArrayList<String> keyWords = new ArrayList<>();
        ArrayList<String> regionIds = new ArrayList<>();
        ArrayList<String> amounts = new ArrayList<>();
        int n = 0;
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                if (!row.isEmpty()) {
                    for (int i = 0; i < arrSize; i++) {
                        switch (i) {
                            case (0):
                                keyWords.add(row.get(i).toString());
                            case (1):
                                amounts.add(row.get(i).toString());
                            case (2):
                                regionIds.add(row.get(i).toString());
                        }


                    }


                }


            }
            for (int i = 0; i < arrSize; i++) {
                datas.get(i).setKeyWords(keyWords.get(i));
                datas.get(i).setExpereinceAmount(amounts.get(i));
                datas.get(i).setRegionId(regionIds.get(i));
            }
            for (Data data : datas) {
                String workExp = new String();
                switch (data.getExpereinceAmount()) {
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
                        System.out.println("work exp is none");

                }
                ArrayList<VacancyData> vacancyDataArrayList = new ArrayList<>();
                final String userAgent = "Chrome/4.0.249.0";
                final String refferer = "http://www.google.com";
                String url = String.format("https://hh.ru/search/vacancy?area=%s&experience=%s&search_field=name&search_field=company_name&search_field=description&text=%s&clusters=true&ored_clusters=true&enable_snippets=true", data.getRegionId(), workExp, data.getKeyWords());
                //?????????? ?? ?????????
                Document doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .referrer(refferer)
                        .get();
                //list vacancy
                Elements listNews = doc.select("div.novafilters >div:nth-child(4)>div.novafilters-group-wrapper>div.novafilters-group__items>li>label");
                for (Element element : listNews) {
                    if (!element.select("input").attr("value").isEmpty() && !element.select("span>span:nth-child(2)").text().isEmpty()) {
                        double zp = Double.parseDouble(element.select("input").attr("value"));
                        int amount = Integer.parseInt(element.select("span>span:nth-child(2)").text().replace("?", ""));
                        VacancyData vacancyData = new VacancyData(zp, amount);
                        vacancyDataArrayList.add(vacancyData);
                    }
                }
                VacancyData vacancyData = new VacancyData(vacancyDataArrayList.get(vacancyDataArrayList.size() - 1).getZp() * 1.2, 0);
                vacancyDataArrayList.add(vacancyData);
            }
        }
    }
    public static double GetValue(ArrayList<VacancyData> vacancyDataArrayList) {
        int j =0;
        double[] sredZ = new double[vacancyDataArrayList.size()-1];//?????? ?? ???? ??
        int[] counts = new int[vacancyDataArrayList.size()-1];//?????? ??????? E
        double[] fMas = new double[vacancyDataArrayList.size()-1];//?????? ??????? F
        double[] sums  =  new double[vacancyDataArrayList.size()];
        int[] nums = new int[vacancyDataArrayList.size()-1];
        for(VacancyData db : vacancyDataArrayList)
        {
            sums[j] = db.getZp();
            j+=1;
        }
        j=0;

        for(VacancyData db : vacancyDataArrayList)
        {
            nums[j] = db.getVacancyAmount();
            j+=1;
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
}

