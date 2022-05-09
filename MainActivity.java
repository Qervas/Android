package com.sxyin.calculator;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;


@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {

    /**
     * input Logic: nowText --->(string by string)---> originalExpression
     *                              |------> txtResult
     */
    TextView txtResults;
    BaseInputConnection textFieldInputConnection;

    enum belonging {number, AddSub, MultiplicatoinDivision, Equals, Sign, Dot, Function, LeftParenthesis, RightParenthesis , Exception}



    //Using Dijkstra Shunting Yard Algorithm
    private final Stack<String> operator = new Stack<>();
    private final Queue<String> RPN = new LinkedList<>();//RPN of reverse polish expression
    ArrayList<String> originalExpression = new ArrayList<>();//could be called by iterators
    String lastInput = "";//null
    String nowText = "";//null

    private boolean dotValid = true, lastIsNumber = false, lastIsOperator = false, computed = false;
    final boolean TEXTVIEW_ONLY = false;
    final boolean TEXTVIEW_AND_ORIGINALEXPRESSION = true;
    final String NUMBER_PATTERN = "^([-+])?\\d+(\\.\\d+)?$";
    final String ADDSUBMULTIDIV_PATTERN = "^([+\\-*/])$";
    final String LEFTPARENTHESIS_PATTERN = "^(\\()$";
    final String RIGHTPARENTHESIS_PATERN="^(\\))$";
    final String ILLEGAL_ENDING_PATTERN = "^([+\\-*/.])$";
    final String FUNCTION_ENDING_PATTERN = "^(sin|cos|sqrt)$";
    final String LEGAL_ENDING= "[0-9\\)]$";
    int parenthesisCounter=0;//0 means correct

    public  Map<String,Integer> map;

    {
        map = new HashMap<>();
        map.put("sqrt", 3);
        map.put("cos", 3);
        map.put("sin", 3);
        map.put("(", 0);
        map.put("+", 1);
        map.put("-", 1);
        map.put("*", 2);
        map.put("/", 2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtResults = findViewById(R.id.txtResults);
        textFieldInputConnection = new BaseInputConnection(txtResults, true);

        Button btnZero = findViewById(R.id.btnZero);//0
        Button btnOne = findViewById(R.id.btnOne);//1
        Button btnTwo = findViewById(R.id.btnTwo);//2
        Button btnThree = findViewById(R.id.btnThree);//3
        Button btnFour = findViewById(R.id.btnFour);//4
        Button btnFive = findViewById(R.id.btnFive);//5
        Button btnSix = findViewById(R.id.btnSix);//6
        Button btnSeven = findViewById(R.id.btnSeven);//7
        Button btnEight = findViewById(R.id.btnEight);//8
        Button btnNine = findViewById(R.id.btnNine);//9
        Button btnDot = findViewById(R.id.btnDot);//.
        Button btnEquals = findViewById(R.id.btnEquals);//=
        Button btnAdd = findViewById(R.id.btnAdd);//+
        Button btnMinus = findViewById(R.id.btnMinus);//-
        Button btnMultiplication = findViewById(R.id.btnMultiplication);//*
        Button btnDivision = findViewById(R.id.btnDivision);// /
        Button btnSqrt = findViewById(R.id.btnSqrt);// sqrt
        Button btnSign = findViewById(R.id.btnSign);// +/-
        Button btnClear = findViewById(R.id.btnClear);// C
        Button btnLeftP = findViewById(R.id.btnLeftParenthesis);// (
        Button btnRightP = findViewById(R.id.btnRightParenthesis);// )
        Button btnSine = findViewById(R.id.btnSine);
        Button btnCoSine = findViewById(R.id.btnCoSine);

        btnZero.setOnClickListener(buttonClick);
        btnOne.setOnClickListener(buttonClick);
        btnTwo.setOnClickListener(buttonClick);
        btnThree.setOnClickListener(buttonClick);
        btnFour.setOnClickListener(buttonClick);
        btnFive.setOnClickListener(buttonClick);
        btnSix.setOnClickListener(buttonClick);
        btnSeven.setOnClickListener(buttonClick);
        btnEight.setOnClickListener(buttonClick);
        btnNine.setOnClickListener(buttonClick);
        btnDot.setOnClickListener(buttonClick);
        btnEquals.setOnClickListener(buttonClick);
        btnAdd.setOnClickListener(buttonClick);
        btnMinus.setOnClickListener(buttonClick);
        btnMultiplication.setOnClickListener(buttonClick);
        btnSqrt.setOnClickListener(buttonClick);
        btnDivision.setOnClickListener(buttonClick);
        btnSign.setOnClickListener(buttonClick);
        btnClear.setOnClickListener(buttonClick);
        btnLeftP.setOnClickListener(buttonClick);
        btnRightP.setOnClickListener(buttonClick);
        btnSine.setOnClickListener(buttonClick);
        btnCoSine.setOnClickListener(buttonClick);



    }

    private final View.OnClickListener buttonClick = new View.OnClickListener() {
        @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
        @Override
        public void onClick(@NonNull View view) {

            switch (view.getId()) {
                case R.id.btnZero:
                    nowText = "0";
                    break;
                case R.id.btnOne:
                    nowText = "1";
                    break;
                case R.id.btnTwo:
                    nowText = "2";
                    break;
                case R.id.btnThree:
                    nowText = "3";
                    break;
                case R.id.btnFour:
                    nowText = "4";
                    break;
                case R.id.btnFive:
                    nowText = "5";
                    break;
                case R.id.btnSix:
                    nowText = "6";
                    break;
                case R.id.btnSeven:
                    nowText = "7";
                    break;
                case R.id.btnEight:
                    nowText = "8";
                    break;
                case R.id.btnNine:
                    nowText = "9";
                    break;
                case R.id.btnDot:
                    nowText = ".";
                    break;
                case R.id.btnEquals:
                    nowText = "=";
                    break;
                case R.id.btnAdd:
                    nowText = "+";
                    break;
                case R.id.btnMinus:
                    nowText = "-";
                    break;
                case R.id.btnMultiplication:
                    nowText = "*";
                    break;
                case R.id.btnDivision:
                    nowText = "/";
                    break;
                case R.id.btnSign:
                    nowText = "Sign";
                    break;
                case R.id.btnClear:
                    clear();
                    Toast.makeText(getApplicationContext(), "All inputs cleared", Toast.LENGTH_SHORT).show();
                    return;
                case R.id.btnLeftParenthesis:
                    nowText = "(";
                    break;
                case R.id.btnRightParenthesis:
                    nowText = ")";
                    break;
                case R.id.btnSqrt:
                    nowText = "sqrt";
                    break;
                case R.id.btnSine:
                    nowText = "sin";
                    break;
                case R.id.btnCoSine:
                    nowText = "cos";
                    break;
                default:
                    nowText = "default";//this line should never be read
                    Toast.makeText(getApplicationContext(), "PANIC error! Input Exception", Toast.LENGTH_SHORT).show();
                    return;
            }
            /*==========================Hot  Operator===========================================*/
            if (computed) {
                if (typeof(nowText) == belonging.number || typeof(nowText) == belonging.Dot) {
                    clear();//
                }else if(typeof(nowText) == belonging.Function){
                    String tempStr = operator.peek();
                    clear();
                    functionReactor();
                    lastInput = "(";
                    lastIsNumber = false;
                    lastIsOperator = true;
                    nowText = tempStr;
                    numberReactor();
                    return;
                }
//                else if(typeof(nowText) == belonging.Sign){
//                    signReactor();
//                    return;
//                }
                else{
//                else if(nowText.matches(ADDSUBMULTIDIV_PATTERN)){//pressed an operator
                    String tempStr = operator.peek();//the operator is guaranteed not null, "tempStr" only use here for 3 lines
                    clear();
                    lastIsNumber = true;//correct
                    lastIsOperator = false;
                    lastInput = tempStr;
                    if(Double.parseDouble(tempStr) < 0){
                        txtResults.setText("("+tempStr+")");
                    }else{
                        txtResults.setText(tempStr);//todo: should concern about (),sin(),sqrt()
                    }
                }
                if(typeof(nowText)==belonging.Equals){
                    return;}

            }

            /*=============================Main Reactor=====================================*/
            switch (typeof(nowText)) {//That the nowText appends to the txtResult doesn't mean it also could be added to the originalExpression
                case number:
                    numberReactor();
                    break;
                case AddSub://Have to 100% ensure that except from the rightmost operator/number other tokens have been added to the ArrayList<Srting>
                case MultiplicatoinDivision:
                    addSubMultiDivisionReactor();
                    break;
                case Dot:
                    dotReactor();
                    break;
                case Sign:
                    signReactor();
                    break;
                case Equals:
                    equalsReactor();
                    break;
                case LeftParenthesis:
                    leftReactor();
                    break;
                case RightParenthesis:
                    rightReactor();
                    break;
                case Function:
                    functionReactor();
                    break;

            }
            //for debug only
//            System.out.print("originalExpression: ");
//            for (String x : originalExpression) {
//                System.out.print(x + " ");
//            }
//            System.out.println();
        }

    };




    /**
     *
     * @param element is a single string on the button on the keyboard
     * @return the type of element
     */
    belonging typeof(@NonNull String element) {

        switch (element) {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                return belonging.number;

            case "+":
            case "-":
                return belonging.AddSub;

            case "*":
            case "/":
                return belonging.MultiplicatoinDivision;

            case "=":
                return belonging.Equals;


            case ".":
                return belonging.Dot;//dot

            case "Sign":
                return belonging.Sign;

            case "(":
                return belonging.LeftParenthesis;

            case ")":
                return belonging.RightParenthesis;

            case "sqrt":
            case "sin":
            case "cos":
                return belonging.Function;
            default:
                return belonging.Exception;
        }
    }

        private void numberReactor(){
            if (!lastInput.isEmpty()) {
                if (lastInput.matches(ADDSUBMULTIDIV_PATTERN) || typeof(lastInput) == belonging.LeftParenthesis
                        || typeof(lastInput) == belonging.Dot) {
                    originalExpression.add(lastInput);//add the Operator
                    lastInput = "";
                }
                //below
                else if ( 0 == Double.parseDouble(lastInput) && dotValid) { // cannot let lastInput == "" step to this line
                    if (nowText.equals("0")) {
                        return;
                    }
                    backSpace(TEXTVIEW_ONLY);// delete 0
                    lastInput="";
                }
                //above
            }
            //last is number
            lastIsNumber = true;
            lastIsOperator = false;
            lastInput += nowText;
            txtResults.append(nowText);
        }

        private void addSubMultiDivisionReactor(){
            if (lastIsOperator) {
                if (typeof(lastInput) != belonging.RightParenthesis) {//other inputs won't wright into the original expression
                    Log.e("error", "You cannot input " + nowText + " here!");
                    Toast.makeText(getApplicationContext(), "You cannot input " + nowText + " here!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            //lastIsNumber==true
            if (originalExpression.isEmpty() && lastInput.isEmpty()) {
                return;
            }
            originalExpression.add(lastInput);//add number or a different sort of operator
            lastIsNumber = false;
            lastIsOperator = true;
            dotValid = true;
            lastInput = nowText;
            txtResults.append(lastInput);

        }

        private void dotReactor(){
            if (dotValid) {
                // ".0", .+.+
                if (lastIsNumber) {
                    lastInput += nowText;
                    dotValid = false;
                    txtResults.append(nowText);
                    return;
                }
            }
            Toast.makeText(getApplicationContext(), "You cannot put a " + nowText + " here!", Toast.LENGTH_SHORT).show();
            Log.e("error", "You cannot put a " + nowText + " here!");
        }
        private void signReactor(){//todo: 2E-4 ?
            if(lastIsNumber){//lastInput is null?
                if(lastInput.isEmpty()){//computed
                    Toast.makeText(getApplicationContext(), "sineReactor() Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                double tempNum = Double.parseDouble(lastInput);
                int len = lastInput.length();
                if(tempNum<0){
                    backSpace(TEXTVIEW_ONLY);// for )
                    for(int i = 0; i < len; i++ ){
                        backSpace(TEXTVIEW_ONLY);
                    }
                    backSpace(TEXTVIEW_ONLY);// for (
                    tempNum = -tempNum;
                    lastInput = Double.toString(tempNum);
                    txtResults.append(lastInput);
                }
                else if(tempNum>0){
                    for(int i = 0; i < len; i++ ){
                        backSpace(TEXTVIEW_ONLY);
                    }
                    tempNum = -tempNum;
                    lastInput = Double.toString(tempNum);
                    txtResults.append("("+lastInput+")");
                }
                lastIsNumber = true;
                lastIsOperator = false;
            }
        }
        private void leftReactor() {
            // Not support omitting operators between operands and parenthesis 2(3)(4) for now
//            if(lastIsOperator || typeof(lastInput) == belonging.LeftParenthesis){//obsolete
            String nowStr = txtResults.getText().toString();
            if(lastInput.matches(ADDSUBMULTIDIV_PATTERN) || typeof(lastInput) == belonging.LeftParenthesis || nowStr.isEmpty()){
                if(!nowStr.isEmpty()){//PANIC error: if add a "" empty strin into the original expression
                    originalExpression.add(lastInput);//add the last input operator
                }
                lastInput=nowText;
                parenthesisCounter++;
                txtResults.append(lastInput);
                lastIsNumber=false;
                lastIsOperator=true;

            }
        }
        private void rightReactor() {
            if(lastIsNumber || typeof(lastInput) == belonging.RightParenthesis){
                if(parenthesisCounter > 0){
                    originalExpression.add(lastInput);
                    lastInput=nowText;
                    txtResults.append(lastInput);
                    parenthesisCounter--;
                    lastIsOperator=true;
                    lastIsNumber=false;
                    lastInput=nowText;
                }else{//parenthesisCounter <= 0
                    Log.e("error","You cannot put a \""+nowText+"\" here!");
                    Toast.makeText(getApplicationContext(), "You cannot put a \""+nowText+"\" here!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        private void functionReactor() {
            String nowStr = txtResults.getText().toString();
            if( nowStr.isEmpty() || (!lastIsNumber && typeof(lastInput)!= belonging.RightParenthesis)){
                if(!nowStr.isEmpty() && !lastInput.isEmpty()){//redundancy?
                    originalExpression.add(lastInput);
                }
                lastInput=nowText;
                txtResults.append(lastInput);//be dangling first
                originalExpression.add(lastInput);

                lastInput="(";//add a (
                txtResults.append(lastInput);
                parenthesisCounter++;

                lastIsOperator=true;
                lastIsNumber=false;
            }

        }

        private void equalsReactor(){
            if(txtResults.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Input somethin' Plz for God's sake!", Toast.LENGTH_SHORT).show();
                Log.e("error","Null Expression \"Equals\" operation Exception!");
                return;
            }

            String ch = Character.toString(lastInput.charAt(lastInput.length()-1));
            if (typeof(lastInput) != belonging.RightParenthesis && typeof(lastInput) != belonging.number) {
                //end with +-*./
                if (ch.matches(ILLEGAL_ENDING_PATTERN)) {

                    Log.e("error", "Your expression cannot end with \"" + lastInput + "\"");
                    Toast.makeText(getApplicationContext(), "Your expression cannot end with \"" + lastInput + "\"\nWe have corrected it for you~", Toast.LENGTH_SHORT).show();
                    backSpace(TEXTVIEW_AND_ORIGINALEXPRESSION);
                    return;
                }
                //end with *(, +(, +sin(
                else if (lastInput.matches(LEFTPARENTHESIS_PATTERN) || lastInput.matches(FUNCTION_ENDING_PATTERN)) {
                    if(lastInput.matches(LEFTPARENTHESIS_PATTERN)){parenthesisCounter--;}
                    Toast.makeText(getApplicationContext(), "Your expression cannot end with \"" + lastInput + "\"\nWe have corrected it for you~", Toast.LENGTH_SHORT).show();
                    backSpace(TEXTVIEW_AND_ORIGINALEXPRESSION);
                    return;
                }
            }
                if(parenthesisCounter == 0){//correctness
                    if (!lastInput.isEmpty()) {
                        originalExpression.add(lastInput);
                        lastInput = "";
                    }
//                    String answer = compute();
//                    if(Double.parseDouble(answer) < 0){

//                        txtResults.setText("Answer: (" + compute() + ")");
//                    }else{
                        String answer = compute();
                        if(answer.equals("NaN")){
                            Toast.makeText(getApplicationContext(), "Inputs invalid", Toast.LENGTH_SHORT).show();
                            clear();
                        }else{
                            txtResults.setText(MessageFormat.format("Answer: {0}", answer));
                            computed = true;
                        }
//                    }

                }else{//parenthesis < 0
                    Toast.makeText(getApplicationContext(), "You need to input a ) to make the expression complete", Toast.LENGTH_SHORT).show();
                    nowText = ")";//autocomplete
                    rightReactor();
                }


        }

    private String compute() {//todo: add the parenthesis


        for (String x : originalExpression) {//scan the entrie expression
            //distinguish the sign with numbers
            if (x.matches(NUMBER_PATTERN)){// error in this line, for x.charAt-->error fixed
                RPN.add(x);
            }else if(x.matches(LEFTPARENTHESIS_PATTERN)){//push a ( into the stack
                operator.push(x);
            }else if(x.matches(RIGHTPARENTHESIS_PATERN)){//ensured that its appearance is valid
                while( !operator.peek().matches(LEFTPARENTHESIS_PATTERN) ){
                    RPN.add(operator.peek());//maybe the operator is empty sometimes?
                    operator.pop();
                }
                //Now the peek() of the stack matches (
                operator.pop();//discard the (
            }else if(typeof(x) == belonging.Function){
                operator.push(x);//push Function in the stack
            }
            else{//+-*/
                    //meets the greater or same precedence
                //noinspection ConstantConditions
                while (!operator.empty() && map.get(x) <= map.get(operator.peek())){
//                    while ( typeof(operator.peek().charAt(0)).compareTo(typeof(x.charAt(0))) >= 0 ) {
                        RPN.add(operator.peek());
                        operator.pop();
                        if (operator.empty()) {
                            break;/*while*/
                        }
                    }
                operator.push(x);
            }
        }
        while (!operator.empty()) {
            RPN.add(operator.peek());
            operator.pop();
        }
        //Now the stack(operator) is empty now, could be used as calculation buffer

        //Calculate the RPN expression
        String iter;
        double leftOperand, rightOperand;
        while (!RPN.isEmpty()) {
            iter = RPN.peek();
            //proved that iter is a number in the first two runs
            assert iter != null;
            if (!iter.matches(NUMBER_PATTERN)) {
                if(typeof(iter)==belonging.Function){
                    //operator definitely is not null
                    leftOperand = Double.parseDouble(operator.peek());
                    operator.pop();
                    switch (iter){
                        case "sin":
                            leftOperand = Math.sin(leftOperand);
                            break;
                        case "cos":
                            leftOperand = Math.cos(leftOperand);
                            break;
                        case "sqrt":
                            leftOperand = Math.sqrt(leftOperand);
                            break;
                    }
                }
                else{
                    // +-*/ below
                    rightOperand = Double.parseDouble(operator.peek());
                    operator.pop();
                    leftOperand = Double.parseDouble(operator.peek());
                    operator.pop();
                    switch (iter) {
                        case "+":
                            leftOperand = leftOperand + rightOperand;
                            break;
                        case "-":
                            leftOperand = leftOperand - rightOperand;
                            break;
                        case "*":
                            leftOperand = leftOperand * rightOperand;
                            break;
                        case "/":
                            leftOperand = leftOperand / rightOperand;
                            break;
                    }
                }
                operator.push(Double.toString(leftOperand));
            }
            else{
                operator.push(RPN.peek());
            }
            RPN.poll();
        }

        if(Double.parseDouble(operator.peek())==0.){
            return "0";
        }
        return operator.peek();
    }

    private void backSpace (boolean textViewOnlySwitcher) {


        String temp = txtResults.getText().toString();
        if(textViewOnlySwitcher){
            if(lastInput.isEmpty()){return;}//maybe redundant here
            for(int i = 0; i < lastInput.length(); i++){
                temp = temp.substring(0, temp.length() - 1);
            }
            if(!originalExpression.isEmpty()){
                lastInput = originalExpression.get(originalExpression.size()-1);
                originalExpression.remove(originalExpression.size()-1);
            }
        }
        else{//only backSpace 1 character on the screen
            temp = temp.substring(0,temp.length()-1);
        }
        txtResults.setText(temp);
        if (temp.isEmpty()) {
            lastInput = "";
            lastIsNumber = false;
            lastIsOperator = false;
        } else {
            if ( lastInput.matches(NUMBER_PATTERN)) {
                lastIsNumber = true;
                lastIsOperator = false;
            } else {
                lastIsNumber = false;
                lastIsOperator = true;
            }
        }

    }

    private void clear () {
        operator.clear();
        RPN.clear();
        originalExpression.clear();
        txtResults.setText("");
        lastIsNumber = false;
        lastIsOperator = false;
        dotValid = true;
        computed = false;
        lastInput="";//null
        parenthesisCounter=0;

    }


}//MainActivity


