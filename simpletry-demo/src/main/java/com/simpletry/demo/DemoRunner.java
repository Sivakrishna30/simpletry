package com.simpletry.demo;

public class DemoRunner {

    public static void main(String[] args) {

        DemoService service = new DemoService();
        DemoServiceSimpleTryWrapper wrapper =
                new DemoServiceSimpleTryWrapper(service);

        wrapper.basicVoid();

        System.out.println("2 " + wrapper.divideSpecific(10,0));

        System.out.println("3 " + wrapper.multiException(-1,2));

        System.out.println("4 " + wrapper.fallbackSingle(10,0));

        System.out.println("5 " + wrapper.fallbackMultiple(-1,0));

        System.out.println("6 " + wrapper.fallbackMethodExample(10,0));

        System.out.println("7 " + wrapper.loggingExample(10,0));

        try{
            System.out.println("8 " + wrapper.checkedExceptionExample(true));
        }catch(Exception ignored){}

        try{
            System.out.println("9 " + wrapper.multipleCheckedExceptions(2));
        }catch(Exception ignored){}

        System.out.println("10 " + wrapper.nullPointerExample(null));

        wrapper.voidExample(-1);

        System.out.println("12 " + wrapper.nestedExceptionExample(0));

        System.out.println("13 " + wrapper.objectReturn());

        System.out.println("14 " + wrapper.booleanTest());

        System.out.println("15 retry success " + wrapper.retrySuccess());

        System.out.println("16 retry fallback " + wrapper.retryFallback());

        try{
            wrapper.ignoreExample(1);
        }catch(Exception e){
            System.out.println("ignored propagated");
        }

        try{
            wrapper.transformExample();
        }catch(Exception e){
            System.out.println("transformed to " + e.getClass().getSimpleName());
        }

        System.out.println("19 debug chain " + wrapper.debugLayer1(5,3));

    }
}