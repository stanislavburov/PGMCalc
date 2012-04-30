package ru.stanislavburov.android.PGMCalc;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class Program implements Serializable{
	private static final long serialVersionUID = 1L;
	private int varCount=0;
	private LinkedList<NoArgCalcAction> pgm = new LinkedList<NoArgCalcAction>();
	private transient Iterator<NoArgCalcAction> iterator = null;
	private String description = null, defaultDescription;

	public Program(String defaultDescription) { this.defaultDescription = defaultDescription; }
	
	public void clear() { pgm.clear(); description = null; varCount=0; }
	public void add(NoArgCalcAction action) { pgm.add(action); }
	public void prepare() { iterator=pgm.iterator(); varCount=0; }
	public boolean hasNext() {
		boolean result=false;
		if(iterator!=null) result = iterator.hasNext();
		return result;
	}
	public NoArgCalcAction next() {
		if(iterator!=null) return iterator.next();
		else return new NoArgCalcAction() { public String perform() { return ""; } }; 
	}
	public void setDescription(String s) { description = s; }
	public String getDescription() {
		if(description!=null) return description;
		else {
			if(pgm.size()==0) return "empty";
			else return defaultDescription;
		}
	}
	public int size() { return pgm.size(); }
	public void newVariable() { varCount++; }
	public int getVarCount() { return varCount; }
}
