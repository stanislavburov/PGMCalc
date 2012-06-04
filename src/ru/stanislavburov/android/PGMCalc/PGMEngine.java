package ru.stanislavburov.android.PGMCalc;

import java.io.Serializable;
import java.util.LinkedList;

import ru.stanislavburov.android.PGMCalc.Operations.BinaryOperation;
import ru.stanislavburov.android.PGMCalc.Operations.Exception;
import ru.stanislavburov.android.PGMCalc.Operations.UnaryOperation;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

public class PGMEngine extends CalcEngine {
	private static final long serialVersionUID = 1L;
	public final NoArgCalcAction
		proceed = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.proceed(); }},
		clear = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.clear(); }},
		changeSign = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.changeSign(); }},
		backspace = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.backspace(); }},
		setPowerOf10 = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.setPowerOf10(); }},		
		setDegree = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.setDegree(); }},
		setRadian = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.setRadian(); }},
		setGradian = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.setGradian(); }},
		changeAngleUnit = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.changeAngleUnit(); }},
		parentheseOpen = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.parentheseOpen(); }},
		parentheseClose = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.parentheseClose(); }},
		swapOperands = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.swapOperands(); }},
		STOM = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.STOM(); }},
		RCLM = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.RCLM(); }},
		mPlus = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.mPlus(); }},
		changeSCIENG = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.changeSCIENG(); }},
		dot = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.addDot(); }},
		halt = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.toString(); }},
		var = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() {
				PGMEngine.super.parentheseOpen();
				pgm[PGMEngine.this.programIndex].newVariable();
				return "[" + Integer.toString(pgm[PGMEngine.this.programIndex].getVarCount()) + "]"; 
			}
		},
		percent = new NoArgCalcAction() {
			private static final long serialVersionUID = 1L;
			@Override public String perform() { return PGMEngine.super.percent(); }
		};
	public final InputCalcAction input = new InputCalcAction();
	public final UnaryOperationCalcAction unaryOperation = new UnaryOperationCalcAction();
	public final BinaryOperationCalcAction binaryOperation = new BinaryOperationCalcAction();
	public final SetFIXCalcAction[] setFIX;
	public final STOCalcAction[] STO = new STOCalcAction[MEM_MAX];
	public final RCLCalcAction[] RCL = new RCLCalcAction[MEM_MAX];
	private static final int PGM_MAX = 10;
	private Program[] pgm = new Program[PGM_MAX];
	private PGMEngineState state = PGMEngineState.REGULAR;
	private int programIndex;
	{
		for(int i=0; i<PGM_MAX; i++) pgm[i] = new Program("Program #" + Integer.toString(i));
		for(int i=0; i<MEM_MAX; i++) {
			STO[i] = new STOCalcAction(i); RCL[i] = new RCLCalcAction(i);
		}
		setFIX = new SetFIXCalcAction[getFIXMax()];
		for(int i=0; i<getFIXMax(); i++)  setFIX[i] = new SetFIXCalcAction(i-1);
	}

	public PGMEngine() { super(); }
	public PGMEngine(Context parent) { super(parent); }
	public NoArgCalcAction makeCalcAction(NoArgCalcAction action) { return action; }
	public InputCalcAction makeCalcAction(InputCalcAction action) { return PGMEngine.this.new InputCalcAction(action.getChar()); }
	public UnaryOperationCalcAction makeCalcAction(UnaryOperationCalcAction action) { return PGMEngine.this.new UnaryOperationCalcAction(action.getUnaryOperation()); }
	public BinaryOperationCalcAction makeCalcAction(BinaryOperationCalcAction action) { return PGMEngine.this.new BinaryOperationCalcAction(action.getBinaryOperation()); }
	public SetFIXCalcAction makeCalcAction(SetFIXCalcAction action) { return PGMEngine.this.new SetFIXCalcAction(action.getInt()); }
	public STOCalcAction makeCalcAction(STOCalcAction action) { return PGMEngine.this.new STOCalcAction(action.getInt()); }
	public RCLCalcAction makeCalcAction(RCLCalcAction action) { return PGMEngine.this.new RCLCalcAction(action.getInt()); }
	
	public String add(NoArgCalcAction action) {
		if(state==PGMEngineState.RECORD_VAR && action!=dot && action!=changeSign && action!=setPowerOf10) state = PGMEngineState.RECORD;
		PGMSelector(action);
		return action.perform();
	}
	public String add(char c) {
		InputCalcAction action = new InputCalcAction(c);
		PGMSelector(action);
		return action.perform();
	}
	public String add(UnaryOperation oper) {
		UnaryOperationCalcAction action = new UnaryOperationCalcAction(oper);
		if(state==PGMEngineState.RECORD_VAR) state=PGMEngineState.RECORD;
		PGMSelector(action);
		return action.perform();
	}
	public String add(BinaryOperation oper) {
		BinaryOperationCalcAction action = new BinaryOperationCalcAction(oper);
		if(state==PGMEngineState.RECORD_VAR) state=PGMEngineState.RECORD;
		PGMSelector(action);
		return action.perform();
	}
	private void PGMSelector(NoArgCalcAction action) {
		if(state==PGMEngineState.RECORD) {
			pgm[programIndex].add(action);
		}
	}
	
	public String[] getAllPrograms() {
		String[] result = new String[PGM_MAX];
		for(int i=0; i<PGM_MAX; i++) result[i] = pgm[i].getDescription();
		return result;
	}
	
	public String continuePGM() {
		if(state==PGMEngineState.VAR) parentheseClose();
		String result=null;
		while(pgm[programIndex].hasNext()) {
			NoArgCalcAction action = pgm[programIndex].next(); 
			result = action.perform();
			if(action==halt) { state = PGMEngineState.HALT; break; }
			if(action==var) { state = PGMEngineState.VAR; break; }
		}
		if(!pgm[programIndex].hasNext()) state = PGMEngineState.REGULAR;
		return result;
	}
	public String runPGM(int which) {
		state = PGMEngineState.PLAY;
		programIndex = which;
		pgm[programIndex].prepare();
		bClearStringsFlag = true;
		return continuePGM();
	}
	public void stopPGM() {
		state = PGMEngineState.REGULAR;
	}
	
	public String startRecord(int which) {
		programIndex = which;
		state = PGMEngineState.RECORD;
		bClearStringsFlag = true;
		pgm[programIndex].clear();
		return toString();
	}
	
	public String stopRecord() {
		state = PGMEngineState.REGULAR;
		return toString();
	}
	
	public PGMEngineState getState() { return state; }
	
	public String PGMVariable() {
		pgm[programIndex].newVariable();
		pgm[programIndex].add(var);
		state = PGMEngineState.RECORD_VAR;
		return "[" + Integer.toString(pgm[programIndex].getVarCount()) + "]";
	}
	public String getVarString() { return "[" + Integer.toString(pgm[programIndex].getVarCount()) + "]"; }
	public int getVarNumber() { return pgm[programIndex].getVarCount(); }

	public class InputCalcAction extends NoArgCalcAction {
		private static final long serialVersionUID = 1L;
		private char c='0';
		public InputCalcAction() { super(); }
		public InputCalcAction(char c) { this.c = c; }
		@Override public String perform() { return PGMEngine.super.input(c); }
		public InputCalcAction setChar(char newChar) { c = newChar; return this; }
		public char getChar() { return c; }
	}
	public class UnaryOperationCalcAction extends NoArgCalcAction {
		private static final long serialVersionUID = 1L;
		private Operations.UnaryOperation oper = PGMEngine.super.operations.zero;
		public UnaryOperationCalcAction() { super(); }
		public UnaryOperationCalcAction(Operations.UnaryOperation oper) { this.oper = oper; }
		@Override public String perform() { return PGMEngine.super.addOperation(oper); }
		public UnaryOperationCalcAction setUnaryOperation(Operations.UnaryOperation newOper) { oper = newOper; return this; }
		public Operations.UnaryOperation getUnaryOperation() { return oper; }
	}
	public class BinaryOperationCalcAction extends NoArgCalcAction {
		private static final long serialVersionUID = 1L;
		private Operations.BinaryOperation oper = PGMEngine.super.operations.doNothing;
		public BinaryOperationCalcAction() { super(); }
		public BinaryOperationCalcAction(Operations.BinaryOperation oper) { this.oper = oper; }
		@Override public String perform() { return PGMEngine.super.addOperation(oper); }
		public BinaryOperationCalcAction setBinaryOperation(Operations.BinaryOperation newOper) { oper = newOper; return this; }
		public Operations.BinaryOperation getBinaryOperation() { return oper; }
	}
	public abstract class IntCalcAction extends NoArgCalcAction {
		private static final long serialVersionUID = 1L;
		protected int v = 0;
		public IntCalcAction() { super(); }
		public IntCalcAction(int v) { this.v = v; }
		public IntCalcAction setInt(int newV) { v = newV; return this; }
		public int getInt() { return v; }
	}
	public class SetFIXCalcAction extends IntCalcAction {
		private static final long serialVersionUID = 1L;
		public SetFIXCalcAction() { super(); }
		public SetFIXCalcAction(int v) { super(v); }
		@Override public String perform() { return PGMEngine.super.setFIX(v); }
	}
	public class STOCalcAction extends IntCalcAction {
		private static final long serialVersionUID = 1L;
		public STOCalcAction() { super(); }
		public STOCalcAction(int v) { super(v); }
		@Override public String perform() { return PGMEngine.super.STO(v); }
	}
	public class RCLCalcAction extends IntCalcAction {
		private static final long serialVersionUID = 1L;
		public RCLCalcAction() { super(); }
		public RCLCalcAction(int v) { super(v); }
		@Override public String perform() { return PGMEngine.super.RCL(v); }
	}
	enum PGMEngineState implements Serializable{
		REGULAR { @Override public String toString() { return ""; } },
		RECORD { @Override public String toString() { return "PGM"; } },
		RECORD_VAR { @Override public String toString() { return "PGM [x]"; } },
		PLAY { @Override public String toString() { return "RUN"; } },
		HALT { @Override public String toString() { return "RUN HALT"; } },
		VAR { @Override public String toString() { return "RUN [x]"; } };
	}
}
