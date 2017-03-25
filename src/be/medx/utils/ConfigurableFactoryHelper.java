package be.medx.utils;

import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import be.medx.exceptions.TechnicalConnectorException;

public class ConfigurableFactoryHelper<T>
{
    private static final Logger LOG;
    private final Map<CacheKey, T> cache;
    private final String classPropertyName;
    private final String defaultClassPropertyName;
    
    public ConfigurableFactoryHelper(final String classPropertyName, final String defaultClassPropertyName) {
        this.cache = new HashMap<CacheKey, T>();
        this.classPropertyName = classPropertyName;
        this.defaultClassPropertyName = defaultClassPropertyName;
    }
    
    public ConfigurableFactoryHelper(final String classPropertyName, final String defaultClassPropertyName, final Class<T> clazz) {
        this(classPropertyName, defaultClassPropertyName);
    }
    
    private T createAndConfigureImplementation(final String headerClassName, final Map<String, Object> configParameters, final boolean silent) throws TechnicalConnectorException {
        Object providerObject = null;
        T result;
        try {
            final Class<?> provider = Class.forName(headerClassName);
            try {
                providerObject = provider.newInstance();
            }
            catch (IllegalAccessException e2) {
                ConfigurableFactoryHelper.LOG.debug("Default constructor is not public. Trying to invoke getInstance().");
                final Method method = provider.getMethod("getInstance", (Class<?>[])new Class[0]);
                providerObject = method.invoke(provider, new Object[0]);
            }
            result = (T)providerObject;
        }
        catch (Exception e) {
            if (!silent) {
                throw new TechnicalConnectorException();
            }
            return null;
        }
        return result;
    }
    
    public T getImplementation() throws TechnicalConnectorException {
        return this.getImplementation(new HashMap<String, Object>(), true, false);
    }
    
    public T getImplementation(final boolean useCache) throws TechnicalConnectorException {
        return this.getImplementation(new HashMap<String, Object>(), useCache, false);
    }
    
    public T getImplementation(final Map<String, Object> configParameters) throws TechnicalConnectorException {
        return this.getImplementation(configParameters, true, false);
    }
    
    public T getImplementation(final Map<String, Object> hashMap, final boolean usecache) throws TechnicalConnectorException {
        return this.getImplementation(hashMap, usecache, false);
    }
    
    public T getImplementation(final Map<String, Object> configParameters, final boolean useCaching, final boolean silent) throws TechnicalConnectorException {
        final String headerClassName = this.defaultClassPropertyName;
        final CacheKey cacheKey = new CacheKey(configParameters, headerClassName);
        if (useCaching && this.cache.containsKey(cacheKey)) {
            return this.cache.get(cacheKey);
        }
        if (headerClassName == null && !silent) {
            throw new TechnicalConnectorException();
        }
        final T result = this.getImplementation(headerClassName, configParameters, useCaching, silent);
        if (result == null && !silent) {
            throw new TechnicalConnectorException();
        }
        return result;
    }
    
    private T getImplementation(final String headerClassName, final Map<String, Object> configParameters, final boolean useCache, final boolean silent) throws TechnicalConnectorException {
        final CacheKey key = new CacheKey(configParameters, headerClassName);
        if (useCache && this.cache.containsKey(key)) {
            return this.cache.get(key);
        }
        if (headerClassName == null || headerClassName.isEmpty()) {
            return null;
        }
        final T result = this.createAndConfigureImplementation(headerClassName, configParameters, silent);
        if (useCache) {
            this.cache.put(key, result);
        }
        return result;
    }
    
    public List<T> getImplementations() throws TechnicalConnectorException {
        return this.getImplementations(true);
    }
    
    public List<T> getImplementations(final boolean useCache) throws TechnicalConnectorException {
        return this.getImplementations(useCache, true);
    }
    
    public List<T> getImplementations(final boolean useCache, final boolean silent) throws TechnicalConnectorException {
        return this.getImplementations(new HashMap<String, Object>(), useCache, silent);
    }
    
    public List<T> getImplementations(final Map<String, Object> configParameters) throws TechnicalConnectorException {
        return this.getImplementations(configParameters, true);
    }
    
    public List<T> getImplementations(final Map<String, Object> configParameters, final boolean useCache) throws TechnicalConnectorException {
        return this.getImplementations(configParameters, useCache, true);
    }
    
    public List<T> getImplementations(final Map<String, Object> configParameters, final boolean useCache, final boolean silent) throws TechnicalConnectorException {
        final List<T> result = new ArrayList<T>();
        final T resultItem2 = this.getImplementation(configParameters, useCache, silent);
        if (resultItem2 != null) {
            result.add(resultItem2);
        }
        return result;
    }
    
    
    public void invalidateCache() {
        this.cache.clear();
    }
    
    static {
        LOG = LoggerFactory.getLogger(ConfigurableFactoryHelper.class);
    }
    
    private static class CacheKey
    {
        private String className;
        private Map<String, Object> configProperties;
        
        public CacheKey(final Map<String, Object> configProperties, final String className) {
            this.configProperties = configProperties;
            this.className = className;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey other = (CacheKey)obj;
            if (this.className == null) {
                if (other.className != null) {
                    return false;
                }
            }
            else if (!this.className.equals(other.className)) {
                return false;
            }
            if (this.configProperties == null) {
                if (other.configProperties != null) {
                    return false;
                }
            }
            else if (!this.configProperties.equals(other.configProperties)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 * result + ((this.className == null) ? 0 : this.className.hashCode());
            result = 31 * result + ((this.configProperties == null) ? 0 : this.configProperties.hashCode());
            return result;
        }
    }
}
