package ru.stanislavburov.android.PGMCalc;

import java.io.Serializable;

public abstract class AngleUnit implements Serializable{
	private static final long serialVersionUID = 1L;
	public abstract double toRadian(double v);
	public abstract double fromRadian(double v);
	public abstract String toString();
}
