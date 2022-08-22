package org.example;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws IOException {
        String workExp = new String();
        switch (args[2]) {
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
        ArrayList<VacancyData> vacancyDataArrayList = new ArrayList<>();
        final String userAgent = "Chrome/4.0.249.0";
        final String refferer = "http://www.google.com";
     String url  = String.format("https://hh.ru/search/vacancy?area=%s&experience=%s&search_field=name&search_field=company_name&search_field=description&text=%s&clusters=true&ored_clusters=true&enable_snippets=true",args[1],workExp,args[0]);
        //?????????? ?? ?????????
        Document doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer(refferer)
                .get();
        //list vacancy
        int n = 0;
        Elements listNews = doc.select("div.novafilters >div:nth-child(6)>div.novafilters-group-wrapper>div.novafilters-group__items>li>label");
        for (Element element : listNews) {
            if (!element.select("input").attr("value").isEmpty() && !element.select("span>span:nth-child(2)").text().isEmpty()) {
                double zp = Double.parseDouble(element.select("input").attr("value"));
                int amount = Integer.parseInt(element.select("span>span:nth-child(2)").text().replace("?", ""));
                VacancyData vacancyData = new VacancyData(zp, amount);
                vacancyDataArrayList.add(vacancyData);
            }
        }
        VacancyData vacancyData = new VacancyData(vacancyDataArrayList.get(vacancyDataArrayList.size()-1).getZp()*1.2,0);
        vacancyDataArrayList.add(vacancyData);


//connect java to google sheets




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