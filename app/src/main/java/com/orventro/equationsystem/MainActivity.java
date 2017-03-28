package com.orventro.equationsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String[] funcStr = {"sin", "cos", "tan", "asin", "acos", "atan", "sinh",
            "cosh", "tanh", "asinh", "acosh", "atanh", "log2", "log10", "ln", "sqrt", "exp", "abs"};
    private Button mult, div, add, subtr, deg, x, select_var, calc, undoBtn, redoBtn,
            left_bracket, right_bracket, select_func, point, C, add_equation, deg_rad;
    private Button[] n = new Button[10],
            delete_equation = new Button[10],
            varBtn = new Button[26],
            funcBtn = new Button[funcStr.length];
    private EditText[] editText = new EditText[10];
    private LinearLayout[] edText_container = new LinearLayout[10];
    private ImageView left_arrow, right_arrow, settings,  erase;
    private TextView output;
    private int activeEditText = 0, selection = 0;
    private String[] input = new String[10];
    private LinearLayout var_container, func_container, input_container;
    private char activeVar = 'x';
    private InputHistory inputHistory;
    private boolean rad = true;
    private boolean add_ifno = false;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private float accuracy = 1;
    private BackgroundCalc backgroundCalc = new BackgroundCalc();

    static {
        System.loadLibrary("native-lib");
    }

    public native String solveEquationSystem(String s[], float accuracy, boolean add_ifno);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        n[0] = (Button) findViewById(R.id.n0);
        n[1] = (Button) findViewById(R.id.n1);
        n[2] = (Button) findViewById(R.id.n2);
        n[3] = (Button) findViewById(R.id.n3);
        n[4] = (Button) findViewById(R.id.n4);
        n[5] = (Button) findViewById(R.id.n5);
        n[6] = (Button) findViewById(R.id.n6);
        n[7] = (Button) findViewById(R.id.n7);
        n[8] = (Button) findViewById(R.id.n8);
        n[9] = (Button) findViewById(R.id.n9);

        for (Button b : n) b.setOnClickListener(this);

        mult = (Button) findViewById(R.id.mult);
        mult.setOnClickListener(this);

        div = (Button) findViewById(R.id.div);
        div.setOnClickListener(this);

        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(this);

        subtr = (Button) findViewById(R.id.subtr);
        subtr.setOnClickListener(this);

        deg = (Button) findViewById(R.id.deg);
        deg.setOnClickListener(this);

        left_bracket = (Button) findViewById(R.id.left_bracket);
        left_bracket.setOnClickListener(this);

        right_bracket = (Button) findViewById(R.id.right_bracket);
        right_bracket.setOnClickListener(this);

        left_arrow = (ImageView) findViewById(R.id.left_arrow);
        left_arrow.setOnClickListener(this);

        right_arrow = (ImageView) findViewById(R.id.right_arrow);
        right_arrow.setOnClickListener(this);

        undoBtn = (Button) findViewById(R.id.undo);
        undoBtn.setOnClickListener(this);

        redoBtn = (Button) findViewById(R.id.redo);
        redoBtn.setOnClickListener(this);

        point = (Button) findViewById(R.id.point);
        point.setOnClickListener(this);

        select_func = (Button) findViewById(R.id.func);
        select_func.setOnClickListener(this);

        select_var = (Button) findViewById(R.id.select_var);
        select_var.setOnClickListener(this);

        deg_rad = (Button) findViewById(R.id.deg_rad);
        deg_rad.setOnClickListener(this);

        x = (Button) findViewById(R.id.x);
        x.setOnClickListener(this);

        C = (Button) findViewById(R.id.C);
        C.setOnClickListener(this);

        output = (TextView) findViewById(R.id.output);
        output.setOnClickListener(this);

        calc = (Button) findViewById(R.id.calc);
        calc.setOnClickListener(this);

        settings = (ImageView) findViewById(R.id.settings);
        settings.setOnClickListener(this);

        add_equation = (Button) findViewById(R.id.add_equation);
        add_equation.setOnClickListener(this);

        erase = (ImageView) findViewById(R.id.erase);
        erase.setOnClickListener(this);

        var_container = (LinearLayout) findViewById(R.id.var_container);
        for (int i = 0; i < 26; i++){
            varBtn[i] = (Button) LayoutInflater.from(this).inflate(R.layout.button3, null);
            varBtn[i].setId(100000+i);
            varBtn[i].setText(Character.toString((char)('A'+i)));
            varBtn[i].setOnClickListener(this);
            var_container.addView(varBtn[i]);
        }

        func_container = (LinearLayout) findViewById(R.id.func_container);
        for (int i = 0; i < funcStr.length; i++){
            funcBtn[i] = (Button) LayoutInflater.from(this).inflate(R.layout.button3, null);
            funcBtn[i].setId(101000+i);
            funcBtn[i].setText(funcStr[i]);
            funcBtn[i].setAllCaps(false);
            funcBtn[i].setOnClickListener(this);
            func_container.addView(funcBtn[i]);
        }

        input_container = (LinearLayout) findViewById(R.id.input_container);
        for (int i = 0; i < edText_container.length; i++){
            edText_container[i] = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.edit_text_container, null);
            edText_container[i].setId(102000+i);
            if (i > 0) edText_container[i].setVisibility(View.GONE);

            delete_equation[i] = (Button) LayoutInflater.from(this).inflate(R.layout.button2, edText_container[i], false);
            delete_equation[i].setId(103000+i);
            delete_equation[i].setText("✕");
            delete_equation[i].setOnClickListener(this);
            edText_container[i].addView(delete_equation[i]);

            editText[i] = (EditText) LayoutInflater.from(this).inflate(R.layout.input_edit_text, edText_container[i], false);
            editText[i].setId(104000+i);
            editText[i].setOnClickListener(this);
            edText_container[i].addView(editText[i]);
            edText_container[i].addView(LayoutInflater.from(this).inflate(R.layout.equal_zero, edText_container[i], false));
            input_container.addView(edText_container[i]);
        }

        input_container.removeView(add_equation);
        input_container.addView(add_equation);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        sp = getPreferences(MODE_PRIVATE);
        editor = sp.edit();

        accuracy = sp.getFloat("accuracy", 1);
        add_ifno = sp.getBoolean("add_info", false);
        rad = sp.getBoolean("rad", true);
        if (rad) deg_rad.setText("RAD");
        else deg_rad.setText("DEG");

        if(sp.getBoolean("activated", false)){
            String[][] iH = new String[100][10];
            int[] sH = new int[100];
            int[] fETIH = new int[100];
            boolean[][] vIIH = new boolean[100][10];
            int aHI = sp.getInt("aHI",99);
            for(int i = 0; i < 100; i++){
                for(int j = 0; j < 10; j++){
                    iH[i][j] = sp.getString("iH"+Integer.toString(i)+" "+Integer.toString(j),"");
                    vIIH[i][j] = sp.getBoolean("vIIH"+Integer.toString(i)+" "+Integer.toString(j),false);
                }
                sH[i] = sp.getInt("sH"+Integer.toString(i),0);
                fETIH[i] = sp.getInt("fETIH"+Integer.toString(i),0);
            }
            inputHistory = new InputHistory(iH, sH, fETIH, vIIH, aHI);
            applyState();
        } else inputHistory = new InputHistory(100, 10);
        accuracy = 1;
    }

    @Override
    public void onClick(View v) {
        setActiveEditText();

        for(int i = 0; i < 10; i++){
            if(v == n[i]) write(Character.toString((char) (i + 48)));
            if(v == delete_equation[i]) deleteEquation(i);
        }

        for(int i = 0; i < funcBtn.length; i++){
            if(v == funcBtn[i]){
                write(funcStr[i] + "()");
                moveSelection(-1);
            }
        }

        if(v == left_bracket) write("(");
        if(v == right_bracket) write(")");
        if(v == mult) write("*");
        if(v == div) write("/");
        if(v == add) write("+");
        if(v == subtr) write("-");
        if(v == deg) write("^");
        if(v == point) write(".");
        if(v == x) write(Character.toString(activeVar));

        if(v == erase) backspace();
        if(v == C) clearEditText();

        if(v == left_arrow) moveSelection(-1);
        if(v == right_arrow) moveSelection(1);

        if(v == add_equation) addEquaiton();

        if(v == undoBtn) {
            if (inputHistory.getActiveHistoryIndex() == 99) addStateToHistory();
            inputHistory.moveHistory(-1);
        }
        if(v == redoBtn) inputHistory.moveHistory(1);
        if(v == undoBtn || v == redoBtn) applyState();

        if(v == deg_rad) {
            rad = !rad;
            if (rad) deg_rad.setText("RAD");
            else deg_rad.setText("DEG");
        }

        if(v == calc) {
            for(int i = 0; i < 10; i++) input[i] = editText[i].getText().toString();
            backgroundCalc.cancel(true);
            backgroundCalc = new BackgroundCalc();
            backgroundCalc.execute();
        }

        for(int i = 0; i < 26; i++) {
            if(v == varBtn[i]) {
                setVar((char) (i + 97));
                write(Character.toString((char) (i + 97)));
            }
        }

        if(v == select_func){
            if(func_container.getVisibility() == View.GONE)func_container.setVisibility(View.VISIBLE);
            else func_container.setVisibility(View.GONE);
            var_container.setVisibility(View.GONE);
        }

        if(v == select_var){
            if(var_container.getVisibility() == View.GONE)var_container.setVisibility(View.VISIBLE);
            else var_container.setVisibility(View.GONE);
            func_container.setVisibility(View.GONE);
        }

        if (v == settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    void write(String s){
        boolean avii[] = inputHistory.getActiveVisibleInputIndex();
        if (!avii[activeEditText]) return ; // if active EditText is disabled
        selection = editText[activeEditText].getSelectionStart();
        int selectionEnd = editText[activeEditText].getSelectionEnd();
        String str1 = editText[activeEditText].getText().toString();
        String str = str1.substring(0, selection) + s + str1.substring(selectionEnd, str1.length());
        selection += s.length();
        editText[activeEditText].setText(str);
        editText[activeEditText].setSelection(selection);
        if(str.length() > 15) editText[activeEditText].setTextSize(Math.max(10,400/str.length()));
        else editText[activeEditText].setTextSize(30);
        if (s.length() > 1 || s.equals("+") || s.equals("-")) addStateToHistory();
    }

    void backspace(){
        selection = editText[activeEditText].getSelectionStart();
        int selectionEnd = editText[activeEditText].getSelectionEnd();
        if (selection > 0 && selection == selectionEnd) selection--;
        String str1 = editText[activeEditText].getText().toString();
        String str = str1.substring(0, selection) + str1.substring(selectionEnd, str1.length());
        editText[activeEditText].setText(str);
        editText[activeEditText].setSelection(selection);
        addStateToHistory();
    }

    void clearEditText(){
        selection = 0;
        editText[activeEditText].setText("");
        addStateToHistory();
    }

    void setVar(char c){
        activeVar = c;
        x.setText(Character.toString(c));
    }

    void setActiveEditText(){
        for(int i = 0; i < editText.length; i++) if (editText[i].isFocused()) activeEditText = i;
        selection = editText[activeEditText].getSelectionStart();
    }

    void addEquaiton(){
        for(int i = 0; i < edText_container.length; i++){
            if(edText_container[i].getVisibility() == View.GONE){
                edText_container[i].setVisibility(View.VISIBLE);
                editText[i].setText("");
                break;
            }
        }
        addStateToHistory();
    }

    void moveSelection(int s){
        setSelection(Math.min(editText[activeEditText].getText().length(), Math.max(0, selection+s)));
    }

    void setSelection(int s){
        selection = s;
        editText[activeEditText].setSelection(s);
    }

    void deleteEquation(int index){
        if(editText[index].isFocused()) {
            for (int i = edText_container.length - 1; i >= 0; i--) {
                if (edText_container[i].getVisibility() == View.VISIBLE) {
                    editText[i].requestFocus();
                    selection = editText[i].getText().length();
                }
            }
        }
        edText_container[index].setVisibility(View.GONE);
        editText[index].setText("");
        addStateToHistory();
    }

    void applyState(){
        String[] activeInput = inputHistory.getActiveInput();
        selection = inputHistory.getActiveSelectionIndex();
        boolean[] visibleInput = inputHistory.getActiveVisibleInputIndex();
        for(int i = 0; i < 10; i++) {
            editText[i].setText(activeInput[i]);
            if (visibleInput[i]) edText_container[i].setVisibility(View.VISIBLE);
            else edText_container[i].setVisibility(View.GONE);
        }
        editText[inputHistory.getActiveFocusedEditTextIndex()].requestFocus();
        editText[inputHistory.getActiveFocusedEditTextIndex()].setSelection(
                Math.min(activeInput[inputHistory.getActiveFocusedEditTextIndex()].length(), selection));
    }

    void addStateToHistory(){
        String[] add_str_to_history = new String[10];
        for(int i = 0; i < 10; i++) add_str_to_history[i] = editText[i].getText().toString();
        boolean[] add_boolean_to_history = new boolean[10];
        for(int i = 0; i < 10; i++) add_boolean_to_history[i] = edText_container[i].getVisibility() == View.VISIBLE;
        inputHistory.updateHistory(add_str_to_history, selection, activeEditText, add_boolean_to_history);
        String[][] iH = inputHistory.getAllInput();
        boolean[][] vIIH = inputHistory.getAllVisibleInputIndexes();
        int[] sH = inputHistory.getAllSelectionIndexes();
        int[] fETIH = inputHistory.getAllFocusedEditTextIndexes();
        for(int i = 0; i < 100; i++){
            for(int j = 0; j < 10; j++){
                editor.putString("iH" + Integer.toString(i) + " " + Integer.toString(j), iH[i][j]);
                editor.putBoolean("vIIH" + Integer.toString(i)+" "+Integer.toString(j), vIIH[i][j]);
            }
            editor.putInt("sH" + Integer.toString(i), sH[i]);
            editor.putInt("fETIH" + Integer.toString(i), fETIH[i]);
        }
        editor.putBoolean("activated", true);
        editor.putInt("aHI",inputHistory.getActiveHistoryIndex());
        editor.putBoolean("rad", rad);
        editor.putBoolean("add_info", add_ifno);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (inputHistory.getActiveHistoryIndex() == 99) addStateToHistory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent d) {
        super.onActivityResult(requestCode, resultCode, d);
        accuracy = d.getFloatExtra("accuracy", 1);
        editor.putFloat("accuracy", accuracy);
        add_ifno = d.getBooleanExtra("add_info", false);
        editor.putBoolean("add_info", add_ifno);
        editor.commit();
    }

    class BackgroundCalc extends AsyncTask<Void, Integer, Void> {
        String[] exprStr;
        int sl = 0;
        String answer = " ";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            for (String i : input) if (i.replaceAll(" ", "").length() > 0) sl++;
            if (sl == 0){
                answer = getString(R.string.no_input);
                exprStr = null;
                return;
            }
            String tempStr;
            int tempInt, tempInt2;
            exprStr = new String[sl];
            sl = 0;
            for (int i = 0; i < 10; i++) {
                if (input[i] != null && input[i].replaceAll(" ", "").length() > 0) {
                    exprStr[sl] = " " + input[i].replaceAll(" ", "").toLowerCase();
                    sl++;
                } else continue ;

                // if user prefers degrees to radians
                // changes 'cos(x)' to cos(0.01745329251994329577*(x))
                // and acos(x) to 57.29577951308232087429*acos(x)
                if (!rad) {
                    for (int j = 0; j < exprStr[i].length(); j++) {
                        for (int k = 0; k < 3; k++) {
                            if (j + funcStr[k].length() < exprStr[i].length() && exprStr[i].substring(j, j + funcStr[k].length()).equals(funcStr[k])) {
                                j += funcStr[k].length() + 1;
                                tempStr = exprStr[i].substring(0, j) + "0.01745329251994329577*(";
                                tempInt2 = j;
                                tempInt = 1;

                                while (j < exprStr[i].length()) {
                                    if (exprStr[i].charAt(j) == '(') tempInt++;
                                    if (exprStr[i].charAt(j) == ')') tempInt--;
                                    if (tempInt == 0) {
                                        tempStr += exprStr[i].substring(tempInt2, j) + ')' + exprStr[i].substring(j);
                                        exprStr[i] = tempStr;
                                        break;
                                    }
                                    j++;
                                }
                                j += 24;
                                break;
                            }
                        }
                        for (int k = 3; k < 6; k++) {
                            if (j + funcStr[k].length() < exprStr[i].length() && exprStr[i].substring(j, j + funcStr[k].length()).equals(funcStr[k])) {
                                exprStr[i] = exprStr[i].substring(0, j) + "57.29577951308232087429*" + exprStr[i].substring(j);
                                j += funcStr[k].length() + 25;
                                break;
                            }
                        }
                    }
                }
            }
            output.setText(getString(R.string.calculating));
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (exprStr != null) answer = solveEquationSystem(exprStr, accuracy, add_ifno);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (answer.charAt(0)=='N') output.setText(getString(R.string.no_roots)+'\n'+answer.substring(1));
            else if (answer.charAt(0) == 'E') {
                if (answer.length() > 1) output.setText(getString(R.string.error)+' '+answer.substring(1));
                else output.setText(getString(R.string.error_all));
            } else output.setText(answer);
        }

    }

}