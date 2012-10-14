package ru.stanislavburov.android.PGMCalc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import ru.stanislavburov.android.PGMCalc.Operations.BinaryOperation;
import ru.stanislavburov.android.PGMCalc.PGMEngine.PGMEngineState;
import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

public class PGMCalcActivity extends Activity {
	private PGMEngine engine; // = new PGMEngine(this);
	private TextView screen, angleUnit, parentheses, tvFIX, tvSCIENG, tvPGMRUN;
	private ImageView proceedButton;
	private ViewGroup managerButtons;
	private ViewAnimator operationalButtons;
	private Resources resources;
	private static final int NUMPAD_INDEX = 0;
	private static final int DIALOG_HOWTOPGM_ID = 0;
	private static final int DIALOG_HELP_ID = 1;
	private static final int DIALOG_QUADRATIC_EQUATION_ID = 2;
	private static final int DIALOG_DOUBLE_FUNCTION_ID = 3;
	private static final int DIALOG_DOUBLE_VARIABLE_ID = 4;
	private int animationDuration=100;
	private String sParentheses = new String(), engineFileName = "engine";
	private Integer nParentheses;
	private int memMax, fixMax;
	private TextView[] tvMemory;
	private LinearLayout memoryLine;
	private View numButton, variableButton, haltButton, runButton;
	DialogInterface.OnClickListener dialogCancelListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int unused) { /*resetManagerButton();*/ }
	};
	private AlertDialog fixDialog = null;
	private boolean switchBackFromMisc = true;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { // TODO unit testing, help on long press, copy screen content to clipboard
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        resources=getResources();
        screen = (TextView)findViewById(R.id.simplescreen);
        screen.setOnClickListener(new View.OnClickListener() { // TODO copy value from the screen to the system buffer
			public void onClick(View v) {
				String errorDescription = engine.getErrorDescription();
				if(errorDescription!=null) {
					Toast.makeText(PGMCalcActivity.this, errorDescription, Toast.LENGTH_LONG).show();
				}
			}
        });
        managerButtons = (ViewGroup)findViewById(R.id.managerbuttons);
        operationalButtons = (ViewAnimator)findViewById(R.id.operationalbuttons);
        angleUnit = (TextView)findViewById(R.id.angleunit);
        parentheses = (TextView)findViewById(R.id.parentheses);
        memMax=CalcEngine.MEM_MAX;
        tvMemory = new TextView[memMax];
        memoryLine = (LinearLayout)findViewById(R.id.memoryline);
        LayoutInflater inflater = getLayoutInflater();
        for(int i=0; i<memMax; i++) {
        	tvMemory[i] = (TextView)inflater.inflate(R.layout.memoryitem, null); 
        	memoryLine.addView(tvMemory[i]);
        	tvMemory[i].setText(Integer.toString(i));
        }
        tvMemory[0].setText("M");
        View.OnTouchListener managerButtonsListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent me) { setPad(v); return true; }
		};
		for(int i=0; i<managerButtons.getChildCount(); i++) managerButtons.getChildAt(i).setOnTouchListener(managerButtonsListener);
		numButton = findViewById(R.id.num); setPad(numButton); numButton.setPressed(true);
		tvFIX = (TextView)findViewById(R.id.fix);
		tvSCIENG = (TextView)findViewById(R.id.scieng);
		tvPGMRUN = (TextView)findViewById(R.id.pgmrun);
		variableButton = findViewById(R.id.bvariable);
		haltButton = findViewById(R.id.bhalt);
		runButton = findViewById(R.id.brun);
		proceedButton = (ImageView)findViewById(R.id.bproceed);
		View bRoundx = findViewById(R.id.broundx);
		bRoundx.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				Toast.makeText(getApplicationContext(), "!!!", Toast.LENGTH_LONG).show();
				return true;
			}
		});
    }
    @Override
    public void onPause() {
    	super.onPause();
   		try {
   			ObjectOutputStream out = new ObjectOutputStream(openFileOutput(engineFileName, MODE_PRIVATE));
   			out.writeObject(engine);
   			out.writeBoolean(switchBackFromMisc);
   			out.close();
		} catch (FileNotFoundException ex) {
			Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException ex) {
			Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
		}
    }
    @Override
    public void onResume() {
    	super.onResume();
    	try {
    		ObjectInputStream in = new ObjectInputStream(openFileInput(engineFileName));
    		engine = (PGMEngine)in.readObject();
    		try {
    			switchBackFromMisc = in.readBoolean();
    		} catch (Exception e) {
				switchBackFromMisc = true;
			}
		} catch (FileNotFoundException e) {
			engine = new PGMEngine();
		} catch(IOException ex) {
			engine = new PGMEngine();
		} catch (ClassNotFoundException ex) {
			engine = new PGMEngine();
		}
    	engine.setContext(this);
    	fixMax = engine.getFIXMax();
    	setScreen(engine.toString(), false);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	if(hasFocus) { resetManagerButton(); }
    }
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	getMenuInflater().inflate(R.menu.options, menu);
    	setMiscSwitcherIcon(menu.findItem(R.id.misc_switcher));
    	return true;
    }
    
//    @Override
//    public boolean onPrepareOptionsMenu (Menu menu) {
//    	super.onPrepareOptionsMenu(menu);
//    	MenuItem miscSwitcher = menu.findItem(R.id.misc_switcher);
//    	if(miscSwitcher.isChecked()) miscSwitcher.setIcon(iconRes);
//    	else 
//    	return true;
//    }
    
    @Override 
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
    	super.onOptionsItemSelected(item);
    	switch(item.getItemId()) {
    	case R.id.help:
    		showDialog(DIALOG_HELP_ID); break;
    	case R.id.howtopgm:
    		showDialog(DIALOG_HOWTOPGM_ID); break;
    	case R.id.misc_switcher:
    		switchBackFromMisc = !switchBackFromMisc;
    		setMiscSwitcherIcon(item);
    		break;
    	default: break;
    	}
    	return true;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
    		.setPositiveButton("Close", null)
        	.setIcon(R.drawable.blank);
    	switch(id) {
    	case DIALOG_HOWTOPGM_ID:
    		dialogBuilder.setView(getLayoutInflater().inflate(R.layout.howtopgm_dialog_layout, null)).setTitle(R.string.howtopgm_title);
    		break;
    	case DIALOG_HELP_ID:
    		dialogBuilder.setMessage(R.string.help_message).setTitle(R.string.help);
    		break;
    	case DIALOG_QUADRATIC_EQUATION_ID:
    		dialogBuilder
    			.setView(getLayoutInflater().inflate(R.layout.quadratic_equation_tutorial_layout, null))
    			.setNeutralButton("Back", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						PGMCalcActivity.this.showDialog(DIALOG_HOWTOPGM_ID);
					}
				})
    			.setTitle(R.string.quadratic_equation_description);
    		break;
    	case DIALOG_DOUBLE_VARIABLE_ID:
    		dialogBuilder
    			.setView(getLayoutInflater().inflate(R.layout.double_variable_tutorial_layout, null))
    			.setNeutralButton("Back", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						PGMCalcActivity.this.showDialog(DIALOG_HOWTOPGM_ID);
					}
				})
    			.setTitle(R.string.double_variable_description);
    		break;
    	case DIALOG_DOUBLE_FUNCTION_ID:
    		dialogBuilder
    			.setView(getLayoutInflater().inflate(R.layout.double_function_tutorial_layout, null))
				.setNeutralButton("Back", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						PGMCalcActivity.this.showDialog(DIALOG_HOWTOPGM_ID);
					}
				})
				.setTitle(R.string.double_function_description);
    		break;
    	default:
    		break;
    	}
    	dialog = dialogBuilder.create();
    	return dialog;
    }
    
    public void raiseTutorial(View v) {
    	dismissDialog(DIALOG_HOWTOPGM_ID);
    	switch(v.getId()) {
    	case R.id.quadratic_equation:
    		showDialog(DIALOG_QUADRATIC_EQUATION_ID); break;
    	case R.id.double_function:
    		showDialog(DIALOG_DOUBLE_FUNCTION_ID); break;
    	case R.id.double_variable:
    		showDialog(DIALOG_DOUBLE_VARIABLE_ID); break;
    	default: break;
    	}
    }
    
    private void setScreen(String s, boolean setNumPad) {
    	screen.setText(s);
    	if(setNumPad) setPad(numButton); // setNumPad();
    	setAngleUnit();
    	setParentheses();
    	setMemories();
    	setFIX();
    	setSCIENG();
    	setPGMRUN();
    }
    
    private void setPGMRUN() {
    	tvPGMRUN.setText(engine.getState().toString());
    	boolean haltVariableState = false;
    	if(engine.getState()==PGMEngineState.RECORD) haltVariableState = true; 
    	variableButton.setEnabled(haltVariableState); haltButton.setEnabled(haltVariableState);
    	runButton.setEnabled(!haltVariableState);
    	switch(engine.getState()) {
    	case REGULAR: proceedButton.setImageResource(R.drawable.proceed); break;
    	case HALT: proceedButton.setImageResource(R.drawable.cont); break;
    	case VAR:
    		int drawableId=R.drawable.varx;
    		switch(engine.getVarNumber()) {
    		case 1: drawableId=R.drawable.var1; break;
    		case 2: drawableId=R.drawable.var2; break;
    		case 3: drawableId=R.drawable.var3; break;
    		case 4: drawableId=R.drawable.var4; break;
    		case 5: drawableId=R.drawable.var5; break;
    		case 6: drawableId=R.drawable.var6; break;
    		case 7: drawableId=R.drawable.var7; break;
    		case 8: drawableId=R.drawable.var8; break;
    		case 9: drawableId=R.drawable.var9; break;
    		default: break;    				
    		}
    		proceedButton.setImageResource(drawableId);
    		break;
    	default: proceedButton.setImageResource(R.drawable.proceed); 
    	}
    }
    private void setSCIENG() { tvSCIENG.setText(engine.getExponentType().toString()); }
    
    private void setFIX() {
    	int fix = engine.getFIX();
    	String content;
    	switch(fix){
    	case -1: content = ""; break;
    	default: content = "FIX:" + Integer.toString(fix);
    	}
    	tvFIX.setText(content);
    }
    
    private void setMemories() {
    	boolean[] memFlags = engine.memOccupation();
    	for(int i=0; i<memMax; i++) tvMemory[i].setEnabled(memFlags[i]);
    }
    
    private void setParentheses() {
    	nParentheses = engine.numberOfOpenedParentheses(); 
    	switch(nParentheses) {
    	case 0: sParentheses = ""; break;
    	case 1: sParentheses = "("; break;
    	case 2: sParentheses = "(("; break;
    	case 3: sParentheses = "((("; break;
    	default: sParentheses = nParentheses.toString() + "x("; 
    	}
    	parentheses.setText(sParentheses);
    }
    
    private void setAngleUnit() {
    	angleUnit.setText(engine.getAngleName());
    }
    
    public void setPad(View v) {
    	int indexNew = managerButtons.indexOfChild(v);
    	int indexOld = operationalButtons.getDisplayedChild();
    	if(indexNew>=0 && indexNew!=indexOld) {
    		managerButtons.getChildAt(indexOld).setPressed(false);
    		operationalButtons.setDisplayedChild(indexNew);
    		v.setPressed(true);
    	}
    }
    
    private void resetManagerButton() { managerButtons.getChildAt(operationalButtons.getDisplayedChild()).setPressed(true); }
    
    private void showMemoryDialog(String title, DialogInterface.OnClickListener listener) {
    	new AlertDialog.Builder(this)
    	.setNegativeButton("cancel", dialogCancelListener)
    	.setTitle(title)
    	.setIcon(R.drawable.blank)
    	.setAdapter(new ArrayAdapter<Double>(this, R.layout.memorydialogrow, R.id.simpletextview, engine.getAllMemories()), listener) // TODO Make special ArrayAdapter
    	.show();
    }
    
    private void showPGMDialog(String title, DialogInterface.OnClickListener listener) {
    	new AlertDialog.Builder(this)
    	.setNegativeButton("cancel", dialogCancelListener)
    	.setTitle(title)
    	.setIcon(R.drawable.blank)
    	.setAdapter(new ArrayAdapter<String>(this, R.layout.memorydialogrow, R.id.simpletextview, engine.getAllPrograms()), listener) // TODO Make special ArrayAdapter
    	.show();
    }
    
    private void createFIXDialog() {
    	String [] dialogContent = new String[fixMax];
    	dialogContent[0] = "not fixed";
    	for(int i=1; i<fixMax; i++) dialogContent[i]=Integer.toString(i-1);
    	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { setScreen(engine.add(engine.setFIX[which]), switchBackFromMisc); }
		};
    	fixDialog = new AlertDialog.Builder(this)
    	.setTitle("Set number of decimal places")
    	.setIcon(R.drawable.blank)
    	.setNegativeButton("cancel", dialogCancelListener)
    	.setAdapter(new ArrayAdapter<String>(this, R.layout.fixdialogrow, R.id.simpletextview, dialogContent), listener)
    	.create();
    }
    
    private void setMiscSwitcherIcon(MenuItem item) {
		if(switchBackFromMisc) item.setIcon(android.R.drawable.button_onoff_indicator_on);
		else item.setIcon(android.R.drawable.button_onoff_indicator_off);
    }

    public void b0Click(View v) { setScreen(engine.add('0'), false); }
    public void b1Click(View v) { setScreen(engine.add('1'), false); }
    public void b2Click(View v) { setScreen(engine.add('2'), false); }
    public void b3Click(View v) { setScreen(engine.add('3'), false); }
    public void b4Click(View v) { setScreen(engine.add('4'), false); }
    public void b5Click(View v) { setScreen(engine.add('5'), false); }
    public void b6Click(View v) { setScreen(engine.add('6'), false); }
    public void b7Click(View v) { setScreen(engine.add('7'), false); }
    public void b8Click(View v) { setScreen(engine.add('8'), false); }
    public void b9Click(View v) { setScreen(engine.add('9'), false); }
    public void bDotClick(View v) { setScreen(engine.add(engine.dot), false); }
    public void bClearClick(View v) {
    	if(engine.getState()==PGMEngineState.HALT) engine.stopPGM();
    	setScreen(engine.add(engine.clear), true);
    }
    public void bChangeSignClick(View v) { setScreen(engine.add(engine.changeSign), false); }
    public void bBackspaceClick(View v) { setScreen(engine.add(engine.backspace), false); }
    public void bPowerOf10Click(View v) { setScreen(engine.add(engine.setPowerOf10), true); }
    public void bProceedClick(View v) {
    	if(engine.getState()==PGMEngineState.HALT || engine.getState()==PGMEngineState.VAR)  setScreen(engine.continuePGM(), true); 
    	else setScreen(engine.add(engine.proceed), true);
    }
    public void bPlusClick(View v) { setScreen(engine.add(engine.operations.plus), true); }
    public void bMinusClick(View v) { setScreen(engine.add(engine.operations.minus), true); }
    public void bMultiplyClick(View v) { setScreen(engine.add(engine.operations.multiply), true); }
    public void bDivideClick(View v) { setScreen(engine.add(engine.operations.divide), true); }
    public void bSinClick(View v) { setScreen(engine.add(engine.operations.sin), true); }
    public void bCosClick(View v) { setScreen(engine.add(engine.operations.cos), true); }
    public void bTanClick(View v) { setScreen(engine.add(engine.operations.tan), true); }
    public void bArcsinClick(View v) { setScreen(engine.add(engine.operations.asin), true); }
    public void bArccosClick(View v) { setScreen(engine.add(engine.operations.acos), true); }
    public void bArctanClick(View v) { setScreen(engine.add(engine.operations.atan), true); }
    public void bSinhClick(View v) { setScreen(engine.add(engine.operations.sinh), true); }
    public void bCoshClick(View v) { setScreen(engine.add(engine.operations.cosh), true); }
    public void bTanhClick(View v) { setScreen(engine.add(engine.operations.tanh), true); }
    public void bArsinhClick(View v) { setScreen(engine.add(engine.operations.arsinh), true); }
    public void bArcoshClick(View v) { setScreen(engine.add(engine.operations.arcosh), true); }
    public void bArtanhClick(View v) { setScreen(engine.add(engine.operations.artanh), true); }
    public void bDegreeClick(View v) { setScreen(engine.add(engine.setDegree), false); }
    public void bRadianClick(View v) { setScreen(engine.add(engine.setRadian), false); }
    public void bGradianClick(View v) { setScreen(engine.add(engine.setGradian), false); }
    public void bConvertAngleClick(View v) { setScreen(engine.add(engine.operations.angleConvert), false); }
    public void bChangeAngleClick(View v) { setScreen(engine.add(engine.changeAngleUnit), false); }
    public void bPiClick(View v) { setScreen(engine.add(engine.operations.pi), true); }
    public void bEClick(View v) { setScreen(engine.add(engine.operations.e), true); }
    public void bcClick(View v) { setScreen(engine.add(engine.operations.c), true); }
    public void bhClick(View v) { setScreen(engine.add(engine.operations.h), true); }
    public void bkClick(View v) { setScreen(engine.add(engine.operations.k), true); }
    public void bAvogadroConstantClick(View v) { setScreen(engine.add(engine.operations.avogadroConstant), true); }
    public void bElementaryChargeClick(View v) { setScreen(engine.add(engine.operations.elementaryCharge), true); }
    public void bVacuumPermettivityClick(View v) { setScreen(engine.add(engine.operations.vacuumPermettivity), true); }
    public void bLnClick(View v) { setScreen(engine.add(engine.operations.ln), true); }
    public void bExpClick(View v) { setScreen(engine.add(engine.operations.exp), true); }
    public void bLogClick(View v) { setScreen(engine.add(engine.operations.log), true); }
    public void bAlogClick(View v) { setScreen(engine.add(engine.operations.alog), true); }
    public void bXsquareClick(View v) { setScreen(engine.add(engine.operations.xsquare), true); }
    public void bXsquarerootClick(View v) { setScreen(engine.add(engine.operations.xsquareroot), true); }
    public void bXcubeClick(View v) { setScreen(engine.add(engine.operations.xcube), true); }
    public void bXcuberootClick(View v) { setScreen(engine.add(engine.operations.xcuberoot), true); }
    public void bXinverseClick(View v) { setScreen(engine.add(engine.operations.xinverse), true); }
    public void bFactorialClick(View v) { setScreen(engine.add(engine.operations.factorial), true); }
    public void bXpowyClick(View v) { setScreen(engine.add(engine.operations.xpowy), true); }
    public void bXrootyClick(View v) { setScreen(engine.add(engine.operations.xrooty), true); }
    public void bXlogyClick(View v) { setScreen(engine.add(engine.operations.xlogy), true); }
    public void bPermutationClick(View v) { setScreen(engine.add(engine.operations.permutation), true); }
    public void bCombinationClick(View v) { setScreen(engine.add(engine.operations.combination), true); }
    public void bIntDivideClick(View v) { setScreen(engine.add(engine.operations.intDivide), true); }
    public void bIntRemainderClick(View v) { setScreen(engine.add(engine.operations.intRemainder), true); }
    public void bModulusClick(View v) { setScreen(engine.add(engine.operations.modulus), true); }
    public void bRoundXClick(View v) { setScreen(engine.add(engine.operations.roundx), true); }
    public void bGammaClick(View v) { setScreen(engine.add(engine.operations.gamma), true); }
    public void bRandClick(View v) { setScreen(engine.add(engine.operations.rand), switchBackFromMisc); }
    public void bParentheseOpenClick(View v) { setScreen(engine.add(engine.parentheseOpen), switchBackFromMisc); }
    public void bParentheseCloseClick(View v) { setScreen(engine.add(engine.parentheseClose), switchBackFromMisc); }
    public void bSwapOperandsClick(View v) { setScreen(engine.add(engine.swapOperands), true); }
    public void bSTOMClick(View v) { setScreen(engine.add(engine.STOM), true); }
    public void bCRLMClick(View v) { setScreen(engine.add(engine.RCLM), true); }
    public void bMPlusClick(View v) { setScreen(engine.add(engine.mPlus), true); }
    public void bSTOClick(View v) {
    	showMemoryDialog("Choose cell to store", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setScreen(engine.add(engine.STO[which]), switchBackFromMisc);
			}
    	});
    }
    public void bRCLClick(View v) {
    	showMemoryDialog("Choose value to recall", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setScreen(engine.add(engine.RCL[which]), switchBackFromMisc);
			}
    	});
    }
    public void bFIXClick(View v) { if(fixDialog==null) createFIXDialog(); fixDialog.show(); }
    public void bSCIENGClick(View v) { setScreen(engine.add(engine.changeSCIENG), false); }
    public void bPGMClick(View v) {
    	if(engine.getState()!=PGMEngine.PGMEngineState.RECORD)
	    	showPGMDialog("Choose slot to record", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					setScreen(engine.startRecord(which), switchBackFromMisc);
				}
			});
    	else setScreen(engine.stopRecord(), true);   
    }
    public void bRUNClick(View v) {
	    	showPGMDialog("Choose program to play", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					setScreen(engine.runPGM(which), switchBackFromMisc);
				}
			});
    }
    public void bHALTClick(View v) { if(engine.getState()==PGMEngineState.RECORD) setScreen(engine.add(engine.halt), switchBackFromMisc); }
    public void bVariableClick(View v) { if(engine.getState()==PGMEngineState.RECORD) setScreen(engine.PGMVariable(), switchBackFromMisc); }
    public void bPercentClick(View v) { setScreen(engine.add(engine.percent), switchBackFromMisc); }
    public void memClearButtonClick(View v) {
    	Toast.makeText(this, "!!!", Toast.LENGTH_SHORT).show();
    }
    public void memTrialClick(View v) {
    	Toast.makeText(this, "View " + v.toString(), Toast.LENGTH_SHORT).show();
    }
}