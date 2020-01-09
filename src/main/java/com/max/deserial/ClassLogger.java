package com.max.deserial;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

public class ClassLogger implements ClassFileTransformer {

    private static final Logger LOG = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ALLOWED_CLASSES;

    static {

        Path whiteListPath = Paths.get(System.getProperty("white.list"));

        StringBuilder buf = new StringBuilder();
        try {
            Files.lines(whiteListPath).forEach(clazzName -> buf.append(clazzName).append("|"));
        }
        catch (IOException ioEx) {
            throw new ExceptionInInitializerError(ioEx);
        }

        ALLOWED_CLASSES = buf.toString();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (className.equals("java/io/ObjectInputStream")) {
            return instrument(className, classfileBuffer);
        }

        return classfileBuffer;
    }

    private static byte[] instrument(String className, byte[] byteCode) {

        LOG.info("Instrumenting: " + className);

        try {

            final ClassPool clPool = ClassPool.getDefault();

            CtClass clazz = clPool.get("java.io.ObjectInputStream");

            CtMethod resolveClassMethod = clazz.getDeclaredMethod("resolveClass",
                                                                  new CtClass[]{clPool.get("java.io.ObjectStreamClass")});

//            ObjectStreamClass desc;
//            if (ALLOWED_CLASSES.indexOf(desc.getName()) == -1) {
//                throw new IllegalStateException(desc.getName() + " can't be deserilised, not in a whitelist.");
//            }

            resolveClassMethod.insertBefore("if (\"" + ALLOWED_CLASSES + "\".indexOf(desc.getName()) == -1) {\n" +
                                                    "                throw new IllegalStateException(desc.getName() + \" " +
                                                    "can't be deserialised, not in a whitelist.\");\n" +
                                                    "            }");
            byte[] enhancedBytecode = clazz.toBytecode();
            clazz.detach();

            LOG.info("'" + className + "' instrumented successfully");

            return enhancedBytecode;
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
            return byteCode;
        }
    }
}
