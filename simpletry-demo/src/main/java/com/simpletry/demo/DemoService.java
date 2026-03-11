package com.simpletry.demo;

import com.simpletry.core.annotation.SimpleTry;

import java.io.IOException;
import java.sql.SQLException;

public class DemoService {

    // ------------------------------------------------
    // TEST 1 — Plain SimpleTry (VOID ONLY)
    // ------------------------------------------------
    @SimpleTry
    public void basicVoid() {
        throw new RuntimeException("basic void");
    }

    // ------------------------------------------------
    // TEST 2 — Specific exception
    // ------------------------------------------------
    @SimpleTry(
            exceptions = ArithmeticException.class,
            fallbackValue = {"0"}
    )
    public int divideSpecific(int a,int b){
        return a/b;
    }

    // ------------------------------------------------
    // TEST 3 — Multiple exceptions
    // ------------------------------------------------
    @SimpleTry(
            exceptions = {ArithmeticException.class,IllegalArgumentException.class},
            fallbackValue = {"10","20"}
    )
    public int multiException(int a,int b){

        if(a<0){
            throw new IllegalArgumentException("Negative");
        }

        return a/b;
    }

    // ------------------------------------------------
    // TEST 4 — fallback single
    // ------------------------------------------------
    @SimpleTry(
            exceptions = ArithmeticException.class,
            fallbackValue = {"100"}
    )
    public int fallbackSingle(int a,int b){
        return a/b;
    }

    // ------------------------------------------------
    // TEST 5 — fallback multiple
    // ------------------------------------------------
    @SimpleTry(
            exceptions = {ArithmeticException.class,IllegalArgumentException.class},
            fallbackValue = {"10","20"}
    )
    public int fallbackMultiple(int a,int b){

        if(a<0){
            throw new IllegalArgumentException();
        }

        return a/b;
    }

    // ------------------------------------------------
    // TEST 6 — fallback method
    // ------------------------------------------------
    @SimpleTry(
            exceptions = ArithmeticException.class,
            fallbackMethod = "divideFallback"
    )
    public int fallbackMethodExample(int a,int b){
        return a/b;
    }

    public int divideFallback(Exception e,int a,int b){
        System.out.println("Fallback called: "+e.getClass().getSimpleName());
        return -1;
    }

    // ------------------------------------------------
    // TEST 7 — Logging
    // ------------------------------------------------
    @SimpleTry(
            exceptions = ArithmeticException.class,
            fallbackValue = {"0"},
            log = true,
            tag = {"DIVIDE_ERROR"}
    )
    public int loggingExample(int a,int b){
        return a/b;
    }

    // ------------------------------------------------
    // TEST 8 — Checked exception
    // ------------------------------------------------
    @SimpleTry(
            exceptions = IOException.class,
            fallbackValue = {"-5"},
            log = true
    )
    public int checkedExceptionExample(boolean fail) throws IOException{

        if(fail){
            throw new IOException("IO failure");
        }

        return 1;
    }

    // ------------------------------------------------
    // TEST 9 — Multiple checked exceptions
    // ------------------------------------------------
    @SimpleTry(
            exceptions = {IOException.class,SQLException.class},
            fallbackValue = {"-10","-20"},
            log = true
    )
    public int multipleCheckedExceptions(int mode)
            throws IOException,SQLException{

        if(mode==1){
            throw new IOException();
        }

        if(mode==2){
            throw new SQLException();
        }

        return 5;
    }

    // ------------------------------------------------
    // TEST 10 — Null pointer
    // ------------------------------------------------
    @SimpleTry(
            exceptions = NullPointerException.class,
            fallbackValue = {"99"}
    )
    public int nullPointerExample(String input){
        return input.length();
    }

    // ------------------------------------------------
    // TEST 11 — void method
    // ------------------------------------------------
    @SimpleTry(
            exceptions = RuntimeException.class,
            log = true,
            tag = {"VOID_ERROR"}
    )
    public void voidExample(int a){

        if(a<0){
            throw new RuntimeException("bad value");
        }

        System.out.println("void executed");
    }

    // ------------------------------------------------
    // TEST 12 — nested exception
    // ------------------------------------------------
    @SimpleTry(
            exceptions = RuntimeException.class,
            fallbackValue = {"50"}
    )
    public int nestedExceptionExample(int x){

        try{
            int y = 10/x;
            return y;
        }
        catch(Exception e){
            throw new RuntimeException("wrapped",e);
        }
    }

    // ------------------------------------------------
    // TEST 13 — object return fallback
    // ------------------------------------------------
    @SimpleTry(
            exceptions = RuntimeException.class,
            fallbackValue = {"null"}
    )
    public String objectReturn(){
        throw new RuntimeException();
    }

    // ------------------------------------------------
    // TEST 14 — boolean fallback
    // ------------------------------------------------
    @SimpleTry(
            exceptions = RuntimeException.class,
            fallbackValue = {"false"}
    )
    public boolean booleanTest(){
        throw new RuntimeException();
    }

    // ------------------------------------------------
    // TEST 15 — retry success
    // ------------------------------------------------
    int retryCounter = 0;

    @SimpleTry(
            exceptions = RuntimeException.class,
            retry = 3,
            fallbackValue = {"0"}
    )
    public int retrySuccess(){

        retryCounter++;

        if(retryCounter < 2){
            throw new RuntimeException("retry");
        }

        return 10;
    }

    // ------------------------------------------------
    // TEST 16 — retry fallback
    // ------------------------------------------------
    int retryFailCounter = 0;

    @SimpleTry(
            exceptions = RuntimeException.class,
            retry = 2,
            fallbackValue = {"500"}
    )
    public int retryFallback(){

        retryFailCounter++;

        throw new RuntimeException("always fail");
    }

    // ------------------------------------------------
    // TEST 17 — ignore
    // ------------------------------------------------
    @SimpleTry(
            exceptions = RuntimeException.class,
            ignore = IllegalArgumentException.class,
            fallbackValue = {"123"}
    )
    public int ignoreExample(int x){

        if(x==1){
            throw new IllegalArgumentException();
        }

        throw new RuntimeException();
    }

    // ------------------------------------------------
    // TEST 18 — transform
    // ------------------------------------------------
    @SimpleTry(
            exceptions = RuntimeException.class,
            transformTo = IllegalStateException.class
    )
    public int transformExample(){

        throw new RuntimeException("transform");
    }

    // ------------------------------------------------
// TEST 19 — Debug trace multi-layer (3 levels)
// ------------------------------------------------

    @SimpleTry(
            exceptions = RuntimeException.class,
            debugTrace = true,
            fallbackValue = {"0"}
    )
    public int debugLayer1(int x, int y) {
        return debugLayer2(x + y);
    }

    @SimpleTry(
            exceptions = RuntimeException.class,
            debugTrace = true,
            fallbackValue = {"0"}
    )
    public int debugLayer2(int value) {
        return debugLayer3(value * 2);
    }

    @SimpleTry(
            exceptions = RuntimeException.class,
            debugTrace = true,
            fallbackValue = {"0"}
    )
    public int debugLayer3(int finalValue) {

        if(finalValue > 10) {
            throw new RuntimeException("deep debug failure");
        }

        return finalValue;
    }

}