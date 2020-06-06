package com.kalavakuri.identifystockmodifications;

import java.io.Serializable;

public class StockVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String moneyControlSymbol;
	private String nseSymbol;

	public String getMoneyControlSymbol() {
		return moneyControlSymbol;
	}

	public void setMoneyControlSymbol(String moneyControlSymbol) {
		this.moneyControlSymbol = moneyControlSymbol;
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}
}
