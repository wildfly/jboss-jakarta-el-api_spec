/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.jboss.el.cache;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.el.ELException;

/**
 * @author Stuart Douglas
 * @deprecated for removal in a future major release.
 */
@Deprecated
public class BeanPropertiesCache {

    static private class BPSoftReference extends SoftReference<BeanProperties> {
        final Class<?> key;
        BPSoftReference(Class<?> key, BeanProperties beanProperties,
                        ReferenceQueue<BeanProperties> refQ) {
            super(beanProperties, refQ);
            this.key = key;
        }
    }

    static private class SoftConcurrentHashMap extends
            ConcurrentHashMap<Class<?>, BeanProperties> {

        private static final long serialVersionUID = -178867497897782229L;
        private static final int CACHE_INIT_SIZE = 1024;
        private ConcurrentHashMap<Class<?>, BPSoftReference> map =
                new ConcurrentHashMap<Class<?>, BPSoftReference>(CACHE_INIT_SIZE);
        private ReferenceQueue<BeanProperties> refQ =
                new ReferenceQueue<BeanProperties>();

        // Remove map entries that have been placed on the queue by GC.
        private void cleanup() {
            BPSoftReference BPRef = null;
            while ((BPRef = (BPSoftReference)refQ.poll()) != null) {
                map.remove(BPRef.key);
            }
        }

        protected void clear(ClassLoader classLoader) {
            Iterator<Map.Entry<Class<?>, BPSoftReference>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Class<?>, BPSoftReference> entry = it.next();
                if(entry.getKey().getClassLoader() == classLoader) {
                    it.remove();
                }
            }

        }

        @Override
        public BeanProperties put(Class<?> key, BeanProperties value) {
            cleanup();
            BPSoftReference prev =
                    map.put(key, new BPSoftReference(key, value, refQ));
            return prev == null? null: prev.get();
        }

        @Override
        public BeanProperties putIfAbsent(Class<?> key, BeanProperties value) {
            cleanup();
            BPSoftReference prev =
                    map.putIfAbsent(key, new BPSoftReference(key, value, refQ));
            return prev == null? null: prev.get();
        }

        @Override
        public BeanProperties get(Object key) {
            cleanup();
            BPSoftReference BPRef = map.get(key);
            if (BPRef == null) {
                return null;
            }
            if (BPRef.get() == null) {
                // value has been garbage collected, remove entry in map
                map.remove(key);
                return null;
            }
            return BPRef.get();
        }
    }
    /*
     * Defines a property for a bean.
     */
    public final static class BeanProperty {

        final private Class<?> baseClass;
        final private PropertyDescriptor descriptor;
        private Method readMethod;
        private Method writeMethod;

        public BeanProperty(Class<?> baseClass,
                            PropertyDescriptor descriptor) {
            this.baseClass = baseClass;
            this.descriptor = descriptor;
        }

        public Class getPropertyType() {
            return descriptor.getPropertyType();
        }

        public boolean isReadOnly(Object base) {
            return getWriteMethod(base) == null;
        }

        public Method getReadMethod(Object base) {
            if (readMethod == null) {
                readMethod = getMethod(baseClass, base, descriptor.getReadMethod());
            }
            return readMethod;
        }

        public Method getWriteMethod(Object base) {
            if (writeMethod == null) {
                writeMethod = getMethod(baseClass, base, descriptor.getWriteMethod());
            }
            return writeMethod;
        }
    }

    /*
     * Defines the properties for a bean.
     */
    public final static class BeanProperties {

        private final Map<String, BeanProperty> propertyMap = new HashMap<>();

        public BeanProperties(Class<?> baseClass) {
            PropertyDescriptor[] descriptors;
            try {
                BeanInfo info = Introspector.getBeanInfo(baseClass);
                descriptors = info.getPropertyDescriptors();
                for (PropertyDescriptor descriptor: descriptors) {
                    propertyMap.put(descriptor.getName(), new BeanProperty(baseClass, descriptor));
                }
                /*
                 * Populating from any interfaces solves two distinct problems:
                 * 1. When running under a security manager, classes may be
                 *    unaccessible but have accessible interfaces.
                 * 2. It enables default methods to be included.
                 */
                populateFromInterfaces(baseClass, baseClass);
            } catch (IntrospectionException ie) {
                throw new ELException(ie);
            }
        }

        private void populateFromInterfaces(Class<?> baseClass, Class<?> aClass) throws IntrospectionException {
            Class<?> interfaces[] = aClass.getInterfaces();
            if (interfaces.length > 0) {
                for (Class<?> ifs : interfaces) {
                    BeanInfo info = Introspector.getBeanInfo(ifs);
                    PropertyDescriptor[] pds = info.getPropertyDescriptors();
                    for (PropertyDescriptor pd : pds) {
                        if (!this.propertyMap.containsKey(pd.getName())) {
                            this.propertyMap.put(pd.getName(), new BeanProperty(
                                    baseClass, pd));
                        }
                    }
                }
            }
            Class<?> superclass = aClass.getSuperclass();
            if (superclass != null) {
                populateFromInterfaces(baseClass, superclass);
            }
        }

        public BeanProperty getBeanProperty(String property) {
            return propertyMap.get(property);
        }
    }

    /**
     * sfot references are horrible
     */
    private static final SoftConcurrentHashMap properties =
            new SoftConcurrentHashMap();


    /*
     * This is the same as the ELUtil.getMethod implementation in the Eclipse Jakarta EL project.
     * That class is package protected so we can't call it from this class.
     * In the org.jboss fork, the ELUtil method delegates to this. This is to avoid having
     * two impls of the same code.
     */
    public static Method getMethod(Class<?> type, Object base, Method m) {
        // If base is null, method MUST be static
        // If base is non-null, method may be static or non-static
        if (m == null ||
                (Modifier.isPublic(type.getModifiers()) &&
                        (canAccess(base, m) || base != null && canAccess(null, m)))) {
            return m;
        }
        Class<?>[] inf = type.getInterfaces();
        Method mp = null;
        for (int i = 0; i < inf.length; i++) {
            try {
                mp = inf[i].getMethod(m.getName(), m.getParameterTypes());
                mp = getMethod(mp.getDeclaringClass(), base, mp);
                if (mp != null) {
                    return mp;
                }
            } catch (NoSuchMethodException e) {
                // Ignore
            }
        }
        Class<?> sup = type.getSuperclass();
        if (sup != null) {
            try {
                mp = sup.getMethod(m.getName(), m.getParameterTypes());
                mp = getMethod(mp.getDeclaringClass(), base, mp);
                if (mp != null) {
                    return mp;
                }
            } catch (NoSuchMethodException e) {
                // Ignore
            }
        }
        return null;
    }


    static boolean canAccess(Object base, AccessibleObject accessibleObject) {
        try {
            return accessibleObject.canAccess(base);
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }


    public static Map<Class<?>, BeanProperties> getProperties() {
        return properties;
    }

    static void clear(ClassLoader classLoader) {
        properties.clear(classLoader);
    }
}
