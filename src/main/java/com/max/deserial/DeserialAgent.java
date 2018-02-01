package com.max.deserial;


import java.lang.instrument.Instrumentation;

public class DeserialAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        ClassLogger transformer = new ClassLogger();
        instrumentation.addTransformer(transformer);
    }

}
