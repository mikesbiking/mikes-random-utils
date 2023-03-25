package mikes.random.utils;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CryptoKrakenTools {

	public static void main(String[] args) throws Exception {

		String ledger = "ledgers1-2022_12-2022.csv";
		List<Transaction> transactions = getTransactions(readFileToList(ledger));

		// Note: Used for price data: https://coinmarketcap.com/coins/

		String prices = "./src/main/resources/Cardano2022-prices.txt";
		List<CryptoValue> cryptoValues = getCryptoValues(readFileToList(prices));
		calculateCryptoStaking(transactions, cryptoValues, "ADA.S");

		prices = "./src/main/resources/Solano2022-prices.txt";
		cryptoValues = getCryptoValues(readFileToList(prices));
		calculateCryptoStaking(transactions, cryptoValues, "SOL.S");
	}

	public static void calculateCryptoStaking(List<Transaction> ledger, List<CryptoValue> cryptoValues, String asset) {
		Float totalAmount = 0f;
		for (Transaction transaction : ledger) {
			if (transaction.asset.equals(asset) && transaction.type.equalsIgnoreCase("staking")) {
				Float costbasis = getCostBasis(transaction.time, cryptoValues);
				// System.out.println("costbasis: " + costbasis);
				totalAmount += costbasis * transaction.amount;
			}
		}
		System.out.println(asset + " amount earned: " + totalAmount);
	}

	public static Float getCostBasis(LocalDate date, List<CryptoValue> cryptoValues) {
		for (CryptoValue value : cryptoValues) {
			if (date.getYear() == value.date.getYear()) {
				if (date.getDayOfYear() == value.date.getDayOfYear()) {
					return value.high;
				}
			}
		}
		return -1f;
	}

	public static List<String> readFileToList(String fileName) throws Exception {
		ArrayList<String> lines = new ArrayList<>();
		File file = new File(fileName);
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static List<Transaction> getTransactions(List<String> lines) {
		List<Transaction> transactions = new ArrayList<>();
		for (String line : lines) {
			transactions.add(new Transaction(line.split(",")));
		}
		return transactions;
	}

	public static List<CryptoValue> getCryptoValues(List<String> lines) {
		List<CryptoValue> values = new ArrayList<>();
		for (String line : lines) {
			values.add(new CryptoValue(line.split("\\s+")));
		}
		return values;
	}

}

class CryptoValue {
	public final LocalDate date;
	public final Float open;
	public final Float high;
	public final Float low;
	public final Float close;
	public final String volume;
	public final String marketcap;

	public CryptoValue(String[] items) {
		String dateString = items[0] + " " + items[1] + " " + items[2];
		// System.out.println(dateString);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
		date = LocalDate.parse(dateString, formatter);
		// date = items[0];
		open = Float.parseFloat(items[3].replaceAll("\\$", "").replaceAll(",", ""));
		high = Float.parseFloat(items[4].replaceAll("\\$", "").replaceAll(",", ""));
		low = Float.parseFloat(items[5].replaceAll("\\$", "").replaceAll(",", ""));
		close = Float.parseFloat(items[6].replaceAll("\\$", "").replaceAll(",", ""));
		volume = items[7];
		marketcap = items[8];
	}
}

class Transaction {

	public final String txid;
	public final String refid;
	public final LocalDate time;
	public final String type;
	public final String subtype;
	public final String aclass;
	public final String asset;
	public final Float amount;
	public final Float fee;
	public final Float balance;

	public Transaction(String[] items) {
		String dateString = items[2].strip().replaceAll("\"", "");
		// 2022-04-01 00:04:35
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		time = LocalDate.parse(dateString, formatter);

		txid = items[0].strip().replaceAll("\"", "");
		refid = items[1].strip().replaceAll("\"", "");
		// time = items[2].strip().replaceAll("\"", "");
		type = items[3].strip().replaceAll("\"", "");
		subtype = items[4].strip().replaceAll("\"", "");
		aclass = items[5].strip().replaceAll("\"", "");
		asset = items[6].strip().replaceAll("\"", "");
		String cleanValue = items[7].strip().replaceAll("\"", "");
		if (cleanValue.length() > 0) {
			amount = Float.parseFloat(cleanValue);
		} else {
			amount = 0f;
		}
		cleanValue = items[8].strip().replaceAll("\"", "");
		if (cleanValue.length() > 0) {
			fee = Float.parseFloat(cleanValue);
		} else {
			fee = 0f;
		}
		cleanValue = items[9].strip().replaceAll("\"", "");
		if (cleanValue.length() > 0) {
			balance = Float.parseFloat(cleanValue);
		} else {
			balance = 0f;
		}
	}

}