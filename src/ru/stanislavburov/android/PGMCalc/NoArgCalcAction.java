package ru.stanislavburov.android.PGMCalc;

import java.io.Serializable;

public abstract class NoArgCalcAction implements Serializable{
	private static final long serialVersionUID = 1L;
	public abstract String perform();
}
