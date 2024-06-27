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
        // 총 페이지 수가 7648이라고 가정합니다 (페이지당 10개의 행).
        int totalPage = 8;
        int numOfRows = 10000;

        for (int pageNo = 1; pageNo <= totalPage; pageNo++) {
            boolean success = fetchAndSavePage(pageNo, numOfRows);

        }
        verifyDataIntegrity();
    }

    @Transactional
    public boolean fetchAndSavePage(int pageNo, int numOfRows) throws Exception {
        List<Hospital> hospitals = new ArrayList<>();

        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/B552657/HsptlAsembySearchService/getHsptlMdcncListInfoInqire");

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

        System.out.println(jsonNode);

        JsonNode items = jsonNode.path("body").path("items").path("item");
        if (items.isArray()) {
            for (JsonNode item : items) {
                Hospital hospital = new Hospital();
                hospital.setDutyName(item.path("dutyName").asText());
                hospital.setDutyAddr(item.path("dutyAddr").asText());
                hospitals.add(hospital);
            }
        }
try {
    hospitalService.saveHospitals(hospitals); // 매 페이지별로 데이터 저장
}catch (Exception e){
    throw new Exception("오류발생");
}
            return true;

    }

    // 데이터 무결성 검증 메소드
    public void verifyDataIntegrity() {
        int apiTotalCount = hospitalService.getApiTotalCount();
        int dbTotalCount = hospitalService.getDbTotalCount();

        if (apiTotalCount != dbTotalCount) {
            System.err.println("데이터 누락 발생! API에서 받은 총 개수: " + apiTotalCount + ", DB에 저장된 총 개수: " + dbTotalCount);
        } else {
            System.out.println("데이터 무결성 확인 완료! 총 개수: " + dbTotalCount);
        }
    }
}

