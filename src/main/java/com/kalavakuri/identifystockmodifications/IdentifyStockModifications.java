package com.kalavakuri.identifystockmodifications;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

public class IdentifyStockModifications {

	private static String MONEY_CONTROL_STOCK_URL = "https://priceapi.moneycontrol.com/pricefeed/nse/equitycash/";
	private static Map<String, String> indicesAndLinks = new HashMap<>();

	static {

		indicesAndLinks.put("MKT_NIFTY_AUTO", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20AUTO");
		indicesAndLinks.put("MKT_NIFTY_BANK", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20BANK");
		indicesAndLinks.put("MKT_NIFTY_COMMODITIES",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20COMMODITIES");
		indicesAndLinks.put("MKT_NIFTY_CPSE", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20CPSE");
		indicesAndLinks.put("MKT_NIFTY_ENERGY",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20ENERGY");
		indicesAndLinks.put("MKT_NIFTY_FINANCIAL_SERVICES",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20FINANCIAL%20SERVICES");
		indicesAndLinks.put("MKT_NIFTY_FMCG", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20FMCG");
		indicesAndLinks.put("MKT_NIFTY_INDIA_CONSUMPTION",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20INDIA%20CONSUMPTION");
		indicesAndLinks.put("MKT_NIFTY_INFRASTRUCTURE",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20INFRASTRUCTURE");
		indicesAndLinks.put("MKT_NIFTY_IT", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20IT");
		indicesAndLinks.put("MKT_NIFTY_MEDIA", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20MEDIA");
		indicesAndLinks.put("MKT_NIFTY_METAL", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20METAL");
		indicesAndLinks.put("MKT_NIFTY_MNC", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20MNC");
		indicesAndLinks.put("MKT_NIFTY_PHARMA",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20PHARMA");
		indicesAndLinks.put("MKT_NIFTY_PRIVATE_BANK",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20PRIVATE%20BANK");
		indicesAndLinks.put("MKT_NIFTY_PSE", "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20PSE");
		indicesAndLinks.put("MKT_NIFTY_PSU_BANK",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20PSU%20BANK");
		indicesAndLinks.put("MKT_NIFTY_REALTY",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20REALTY");
		indicesAndLinks.put("MKT_NIFTY_SERVICES_SECTOR",
				"https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%20SERVICES%20SECTOR");
	}

	public static void main(String[] args) throws Exception {

		int count = 1;

		System.out.println("\n    Checking modifications...");

		for (Map.Entry<String, String> entries : indicesAndLinks.entrySet()) {

			System.out.print("\n" + "    " + count + "   " + entries.getKey().replace("_", " ").replace("MKT ", ""));

			boolean isNotMatched = false;
			Map<String, String> nseStocks = nseStocks(entries);
			Map<String, StockVO> moneyControlStocks = fetchMoneyControlStocks(entries);

			for (Map.Entry<String, String> entry : nseStocks.entrySet()) {

				if (!moneyControlStocks.containsKey(entry.getKey())) {
					System.out.println("Missing in NSE   " + entries.getKey() + "   " + entry.getKey());
					isNotMatched = true;
				}
			}

			if (isNotMatched) {
				break;
			}

			for (Map.Entry<String, StockVO> entry : moneyControlStocks.entrySet()) {

				if (!nseStocks.containsKey(entry.getKey())) {

					System.out.println("Extra in MoneyControl   " + entries.getKey() + "   "
							+ entry.getValue().getMoneyControlSymbol());
					isNotMatched = true;
				}
			}

			if (isNotMatched) {
				break;
			}
			count++;
			System.out.print("   Success...");
		}
		System.out.println("\n\n    No issues found...");
		System.out.print("\n    Wait till 9:15:35...");
	}

	private static Map<String, String> nseStocks(Map.Entry<String, String> entries) throws IOException {

		List<String> nseStocks = new ArrayList<>();

		Response response = Jsoup.connect(entries.getValue()).ignoreContentType(true).userAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36")
				.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0).execute();

		Document doc = response.parse();

		Gson gson = new Gson();
		Map<?, ?> stockDetails = gson.fromJson(doc.text(), Map.class);
		ArrayList<?> stockData = (ArrayList<?>) stockDetails.get("data");

		for (int i = 1; i < stockData.size(); i++) {

			Map<?, ?> dayData = (Map<?, ?>) stockData.get(i);
			nseStocks.add((String) dayData.get("symbol"));

		}
		return nseStocks.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
	}

	private static Map<String, StockVO> fetchMoneyControlStocks(Map.Entry<String, String> entries) throws IOException {

		List<StockVO> stocks = getStocks(entries.getKey());
		List<StockVO> stocksOfTheDay = new ArrayList<>();

		for (StockVO stockVO : stocks) {

			String NSESymbol = "";

			Response response = Jsoup.connect(MONEY_CONTROL_STOCK_URL + stockVO.getMoneyControlSymbol())
					.ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36")
					.timeout(90 * 1000).header("Accept", "application/json").followRedirects(true).maxBodySize(0)
					.execute();

			Document doc = response.parse();

			Gson gson = new Gson();
			Map<?, ?> stockDetails = gson.fromJson(doc.text(), Map.class);
			Map<?, ?> stockData = (Map<?, ?>) stockDetails.get("data");

			try {

				NSESymbol = (String) stockData.get("NSEID");

				stockVO.setNseSymbol(NSESymbol);

			} catch (Exception e) {
				System.out.println(NSESymbol + " Failed to fetch the details");
				continue;
			}
			stocksOfTheDay.add(stockVO);
		}

		return stocksOfTheDay.stream().collect(Collectors.toMap(StockVO::getNseSymbol, Function.identity()));
	}

	private static List<StockVO> getStocks(String tableName) {

		List<StockVO> stocks = new ArrayList<>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "#knagamma1");
			preparedStatement = connection.prepareStatement("SELECT MONEY_CONTROL_SYMBOL FROM " + tableName);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				StockVO stock = new StockVO();
				stock.setMoneyControlSymbol(resultSet.getString("MONEY_CONTROL_SYMBOL"));
				stocks.add(stock);
			}
		} catch (Exception e) {
			System.exit(0);
		} finally {
			try {
				if (null != resultSet && !resultSet.isClosed())
					resultSet.close();
				if (null != preparedStatement && !preparedStatement.isClosed())
					preparedStatement.close();
				if (null != connection && !connection.isClosed())
					connection.close();
			} catch (SQLException e) {
				System.exit(0);
			}
		}

		return stocks;
	}
}
