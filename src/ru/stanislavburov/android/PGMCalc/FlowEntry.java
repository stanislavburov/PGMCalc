package ru.stanislavburov.android.PGMCalc;

import java.io.Serializable;

public class FlowEntry implements Serializable{
	private static final long serialVersionUID = 1L;
	double value;
	Operations.BinaryOperation oper;
	public FlowEntry(double value, Operations.BinaryOperation oper) {
		this.value = value;
		this.oper = oper;
	}
	public double getValue() { return value; }
	public Operations.BinaryOperation getOperation() { return oper; }
	public void setOperation(Operations.BinaryOperation oper) { this.oper = oper; }
	public void setValue(double v) { value = v; }
}
