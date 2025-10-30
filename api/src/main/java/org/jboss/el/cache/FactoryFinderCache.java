/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.jboss.el.cache;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Stuart Douglas
 * @deprecated for removal in a future major release.
 */
@Deprecated
public class FactoryFinderCache {

    private static final Map<CacheKey, String> CLASS_CACHE = new ConcurrentHashMap<CacheKey, String>();

    /**
     * Called by the container at deployment time to set the name of a given factory, to remove the need for the
     * implementation to look it up on every call.
     *
     * @param classLoader      The deployments class loader
     * @param factoryId        The type of factory that is being recorded (at this stage only jakarta.el.ExpressionFactory has any effect
     * @param factoryClassName The name of the factory class that is present in the deployment, or null if none is present
     */
    public static void addCacheEntry(final ClassLoader classLoader, final String factoryId, final String factoryClassName) {
        if (factoryClassName == null) {
            CLASS_CACHE.put(new CacheKey(classLoader, factoryId), "");
        } else {
            CLASS_CACHE.put(new CacheKey(classLoader, factoryId), factoryClassName);
        }
    }

    /**
     * This should be called by the container on undeploy to remove all references to the given class loader
     * from the cache.
     *
     * @param classLoader The class loader to remove
     */
    public static void clearClassLoader(final ClassLoader classLoader) {
        BeanPropertiesCache.clear(classLoader);
        ImportHandlerCache.clearClassLoader(classLoader);
        final Iterator<Map.Entry<CacheKey, String>> it = CLASS_CACHE.entrySet().iterator();
        while (it.hasNext()) {
            final CacheKey key = it.next().getKey();
            if (key.loader == classLoader) {
                it.remove();
            }
        }
    }

    public static String loadImplementationClassName(final String factoryId, final ClassLoader classLoader) {

        final Map<CacheKey, String> classCache = CLASS_CACHE;
        if (classCache != null) {
            final String value = classCache.get(new CacheKey(classLoader, factoryId));
            if (value != null) {
                if (value.equals("")) {
                    return null;
                }
                return value;
            }
        }

        String serviceId = "META-INF/services/" + factoryId;
        // try to find services in CLASSPATH
        try {
            InputStream is = null;
            if (classLoader == null) {
                is = ClassLoader.getSystemResourceAsStream(serviceId);
            } else {
                is = classLoader.getResourceAsStream(serviceId);
            }

            if (is != null) {
                BufferedReader rd =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String factoryClassName;
                do {
                    factoryClassName = sanitize(rd.readLine());
                } while (factoryClassName != null && factoryClassName.isEmpty());

                rd.close();

                if (factoryClassName != null &&
                        !"".equals(factoryClassName)) {
                    if (classCache != null) {
                        classCache.put(new CacheKey(classLoader, factoryId), factoryClassName);
                    }
                    return factoryClassName;
                }
            }
        } catch (Exception ex) {
        }
        if (classCache != null) {
            classCache.put(new CacheKey(classLoader, factoryId), "");
        }
        return null;
    }

    static String sanitize(String line) {
        if (line == null) {
            return null;
        }

        // strip comment
        int idx = line.indexOf('#');
        if (idx >= 0) {
            line = line.substring(0, idx);
        }

        // strip spaces and tabs
        while (line.startsWith(" ") || line.startsWith("\t")) {
            line = line.substring(1);
        }
        while (line.endsWith(" ") || line.endsWith("\t")) {
            line = line.substring(0, line.length() - 1);
        }

        return line;
    }


}
