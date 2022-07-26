package org.example;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://hh.ru/search/vacancy?area=1&search_field=name&search_field=company_name&search_field=description&text=Java&from=suggest_post&clusters=true&ored_clusters=true&enable_snippets=true")
                .userAgent("Chrome/4.0.249.0")
                .referrer("http://www.google.com")
                .get();
        int n = 0; //счетчик количества полей, в которых есть данные(числовые---->которые можно спарсить)
         List<Double> listZp = new ArrayList<>();
         List<Integer> listNums = new ArrayList<>();
         VacancyData data = new VacancyData(listZp,listNums);
        Elements listNews = doc.select("div.novafilters >div:nth-child(4)>div.novafilters-group-wrapper>div.novafilters-group__items>li>label>span>span");
        for (Element element : listNews) {
            if (n%2==0)
            {
                try {
                    data.listZp.add(Double.parseDouble(element.text().replace("от","").replace("руб.","").trim().replace(" ","")));
                }
                catch (NumberFormatException ex)
                {
                    System.out.println("end of parse first block");
                }
            }
            else
            {
                   data.listNums.add(Integer.parseInt(element.text().replace("от", "").replace("руб.", "").trim().replace(" ", "")));
            }
            n+=1;




        }



    }
}