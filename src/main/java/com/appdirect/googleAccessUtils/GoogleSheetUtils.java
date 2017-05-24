package com.appdirect.googleAccessUtils;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.testng.Reporter;

import com.appdirect.googleAccess.Editions;
import com.appdirect.googleAccess.GoogleSheetAccess;
import com.appdirect.utils.DriverUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

public class GoogleSheetUtils {
    FileOutputStream outputFile;
    private Properties properties;
    static int count = 0;
    FileWriter fileWritter;

    private static final String APPLICATION_NAME = "Google Sheets API Java Access";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
            ".credentials/1/sheets.googleapis.com-java-quickstart");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
    public static Credential authorize() throws Exception {
    	
        InputStream in = GoogleSheetAccess.class.getResourceAsStream("client_secret_Gsheet.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        Reporter.log("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }
    public static Sheets getSheetsService() throws Exception {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
                .build();
    }
    public void getPriceList(String spreadsheetId, String range, HashMap<String, Editions> editions, String setCurrencyName) {
        Sheets service;
        try {
            service = getSheetsService();
            ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.size() == 0) {
                Reporter.log("No data found.", true);
            } else {
                for (List row : values) {
                    Editions edition = editions.get(row.get(4).toString());
                    switch (setCurrencyName) {
                        case "AUD":
                            edition.setaudPrice(row.get(10).toString());
                            break;
                        case "EUR":
                            edition.seteurPrice(row.get(10).toString());
                            break;
                        case "GBP":
                            edition.setgbpPrice(row.get(10).toString());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Reporter.log("Exception while reading price from googlesheets");
        }
    }
    
    public String getSpreadSheetId() {
    	properties = new DriverUtils().getProperty("resources/res-utils.properties");
    	if (DriverUtils.platform.equals("orchard"))
            return properties.getProperty("spreadSheetUrlOrchard");
        else
        	return properties.getProperty("spreadSheetUrl");
    }

}
