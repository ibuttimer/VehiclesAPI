package com.udacity.pricing.misc;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class Utils {

    private Utils() {
        // no instantiation
    }

    /**
     * Execute a class method using reflection
     * @param cls - object class
     * @param name - the name of the method
     * @param parameterTypes – the list of parameters
     * @param obj – the object the underlying method is invoked from
     * @param args – the arguments used for the method call
     * @return
     * @throws Throwable
     */
    public static Object executeMethod(Class<?> cls, String name, List<Class<?>> parameterTypes, Object obj, List<Object> args)
            throws Throwable {
        Object result = null;
        try {
            Method met = cls.getMethod(name, parameterTypes.toArray(new Class[0]));
            if (obj == null && !Modifier.isStatic(met.getModifiers())) {
                throw new IllegalArgumentException("Method '" + name + "' is not static, so 'obj' cannot be null");
            }
            try {
                if (args != null) {
                    result = met.invoke(obj, args.toArray());
                } else {
                    result = met.invoke(obj, (Object) new Array[0]);
                }
            }
            catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        } catch (NoSuchMethodException e) {
            // no-op ignore
        }
        return result;
    }
}
