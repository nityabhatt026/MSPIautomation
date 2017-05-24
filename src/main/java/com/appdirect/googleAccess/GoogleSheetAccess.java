package com.appdirect.googleAccess;

import com.google.api.services.sheets.v4.model.*;
import com.appdirect.googleAccessUtils.GoogleSheetUtils;
import com.appdirect.reporter.GoogleSheetToCsv;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.testng.Reporter;

@SuppressWarnings("rawtypes")
public class GoogleSheetAccess extends GoogleSheetUtils {
	private HashMap<String, Editions> skus = new HashMap<String, Editions>();
	private HashMap<String, Editions> editions = new HashMap<String, Editions>();
	private HashMap<String, Editions> addons = new HashMap<String, Editions>();
	String spreadsheetId = "";

	public void getList() {
		try {
			
			Sheets service = getSheetsService();
			spreadsheetId = new GoogleSheetUtils().getSpreadSheetId();
			String rangevalue = System.getProperty("startrow");
			if (rangevalue == null)
				rangevalue = "4";
			String usdRange, audRange, eurRange, gbpRange;
			Values vals = service.spreadsheets().values();
			ValueRange res = vals.get(spreadsheetId, "D" + rangevalue + ":D").execute();
			usdRange = "USD!" + rangevalue + ":" + res.getValues().size() + (Integer.valueOf(rangevalue) - 1);
			audRange = "AUD!" + rangevalue + ":" + res.getValues().size() + (Integer.valueOf(rangevalue) - 1);
			eurRange = "EUR!" + rangevalue + ":" + res.getValues().size() + (Integer.valueOf(rangevalue) - 1);
			gbpRange = "GBP!" + rangevalue + ":" + res.getValues().size() + (Integer.valueOf(rangevalue) - 1);
			ValueRange response = service.spreadsheets().values().get(spreadsheetId, usdRange).execute();
			List<List<Object>> values = response.getValues();
			if (values == null || values.size() == 0) {
				Reporter.log("No data found.", true);
			} else {
				for (List row : values) {
					Editions edition = new Editions();
					edition.setEditionName(row.get(3).toString());
					edition.setOfferId(row.get(4).toString());
					edition.setSkuType(row.get(7).toString());
					edition.setusdPrice(row.get(10).toString());
					edition.setappId(row.get(11).toString());
					skus.put(row.get(4).toString(), edition);
				}
			}
			new GoogleSheetUtils().getPriceList(spreadsheetId, audRange, skus, "AUD");
			new GoogleSheetUtils().getPriceList(spreadsheetId, gbpRange, skus, "GBP");
			new GoogleSheetUtils().getPriceList(spreadsheetId, eurRange, skus, "EUR");
		} catch (Exception e) {
			Reporter.log("Exception in GoogleSheet Access");
		}
	}

	public HashMap<String, Editions> getAddonList() {
		Iterator<Entry<String, Editions>> iterator = skus.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Editions> pair = iterator.next();
			Editions ed = pair.getValue();
			if (ed.getSkuType().equals("ADDON")) {
				addons.put(ed.getOfferId(), ed);
			} else
				editions.put(ed.getOfferId(), ed);
		}
		return this.addons;
	}

	public HashMap<String, Editions> getEditionsList() {
		return this.editions;
	}

	public void editionDisplay(HashMap<String, Editions> skuSet) {
		Reporter.log("Total rows=" + skuSet.size() + "\n", true);
		int count = 1;
		Iterator<Entry<String, Editions>> iterator = skuSet.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Editions> pair = iterator.next();
			Editions ed = pair.getValue();
			if (!ed.getappId().equals("NA")) {
				Reporter.log(count + "\t" + ed.getEditionName() + "\t" + ed.getOfferId() + "\t" + ed.getusdPrice()
						+ "\t" + ed.geteurPrice() + "\t" + ed.getaudPrice() + "\t" + ed.getgbpPrice() + "\t"
						+ ed.getSkuType() + "\t" + ed.getappId(), true);
				count++;

			}
		}
		//new GoogleSheetToCsv().csvgenerate(editions);
	}

}