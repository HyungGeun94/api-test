package jpabook.testtest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@SpringBootApplication
public class HospitalController implements CommandLineRunner {

    @Autowired
    private HospitalService hospitalService;

    public static void main(String[] args) {
        SpringApplication.run(HospitalController.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        fetchAndSaveHospitalData();
    }

    public void fetchAndSaveHospitalData() throws IOException {
        int pageNo = 1;
        int totalPage = 1;
        int numOfRows = 10;

        // 첫 페이지를 호출하여 totalCount 확인
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/HsptlAsembySearchService/getHsptlMdcncFullDown");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=zokiSm%2By3xtU8TPm5w9T3s14a6DEcXW0YScHxOdfFk0ZFSxSXPr%2F9tVPQ1a6AkMd2YTSf4yv5kAt18LO%2F9lwcw%3D%3D");
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(pageNo), "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(numOfRows), "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String xmlResponse = sb.toString();
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode jsonNode = xmlMapper.readTree(xmlResponse.getBytes());

        int totalCount = jsonNode.path("body").path("totalCount").asInt();
        totalPage = (totalCount + numOfRows - 1) / numOfRows; // 총 페이지 수 계산

        // 각 페이지별 데이터 가져오기 및 저장
        for (pageNo = 1; pageNo <= totalPage; pageNo++) {
            fetchAndSavePage(pageNo, numOfRows);
        }
    }

    @Transactional
    public void fetchAndSavePage(int pageNo, int numOfRows) throws IOException {
        List<Hospital> hospitals = new ArrayList<>();

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552657/HsptlAsembySearchService/getHsptlMdcncFullDown");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=zokiSm%2By3xtU8TPm5w9T3s14a6DEcXW0YScHxOdfFk0ZFSxSXPr%2F9tVPQ1a6AkMd2YTSf4yv5kAt18LO%2F9lwcw%3D%3D");
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(pageNo), "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(numOfRows), "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String xmlResponse = sb.toString();
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode jsonNode = xmlMapper.readTree(xmlResponse.getBytes());

        JsonNode items = jsonNode.path("body").path("items").path("item");
        if (items.isArray()) {
            for (JsonNode item : items) {
                Hospital hospital = new Hospital();
                hospital.setDutyName(item.path("dutyName").asText());
                hospital.setDutyAddr(item.path("dutyAddr").asText());
                hospitals.add(hospital);
            }
        }

        hospitalService.saveHospitals(hospitals); // 매 페이지별로 데이터 저장
    }
}
