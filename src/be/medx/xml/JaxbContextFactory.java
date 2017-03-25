// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.xml;

import java.util.HashMap;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.security.MessageDigest;
import java.util.Set;
import org.joda.time.ReadableInstant;
import org.joda.time.Duration;
import org.bouncycastle.util.encoders.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import java.util.TreeSet;
import org.joda.time.DateTime;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.ArrayUtils;
import javax.xml.bind.JAXBContext;
import java.util.Map;
import org.slf4j.Logger;

public final class JaxbContextFactory
{
    public static final String PROP_CACHE_TYPE = "be.ehealth.technicalconnector.utils.impl.JaxbContextFactory.cache_type";
    private static final String PROP_CACHE_TYPE_CLASSNAME = "classname";
    private static final String PROP_CACHE_TYPE_PACKAGE = "package";
    private static final String PROP_CACHE_TYPE_DEFAULT = "classname";
    private static final Logger LOG;
    private static final Map<String, JAXBContext> CACHE;
    private static String cacheType;
    
    private JaxbContextFactory() {
        throw new UnsupportedOperationException();
    }
    
    public static void initJaxbContext(final Class<?>... classesToBeBound) {
        try {
            getJaxbContextForClass(classesToBeBound);
        }
        catch (JAXBException e) {
            JaxbContextFactory.LOG.warn("Unable to load JaxbContext for " + ArrayUtils.toString((Object)classesToBeBound), (Throwable)e);
        }
    }
    
    public static JAXBContext getJaxbContextForClass(final Class<?>... classesToBeBound) throws JAXBException {
        String key = null;
        String packageToBeBound = null;
        if (classesToBeBound.length == 1) {
            if ("package".equals(JaxbContextFactory.cacheType)) {
                key = (packageToBeBound = classesToBeBound[0].getPackage().getName());
            }
            else {
                if (!"classname".equals(JaxbContextFactory.cacheType)) {
                    throw new IllegalArgumentException("Unsupported cachetype [" + JaxbContextFactory.cacheType + "]");
                }
                key = classesToBeBound[0].getName();
            }
        }
        else {
            final DateTime start = new DateTime();
            final Set<String> classList = new TreeSet<String>();
            for (final Class<?> classToBeBound : classesToBeBound) {
                classList.add(classToBeBound.getName());
            }
            final MessageDigest complete = DigestUtils.getMd5Digest();
            for (final String clazz : classList) {
                complete.update(clazz.getBytes());
            }
            key = new String(Base64.encode(complete.digest()));
            JaxbContextFactory.LOG.debug("Calculating digest-key for " + ArrayUtils.toString((Object)classesToBeBound) + " in " + new Duration((ReadableInstant)start, (ReadableInstant)new DateTime()));
        }
        JAXBContext context = JaxbContextFactory.CACHE.get(key);
        if (context == null) {
            final DateTime start2 = new DateTime();
            if (packageToBeBound == null) {
                context = JAXBContext.newInstance((Class[])classesToBeBound);
            }
            else {
                context = JAXBContext.newInstance(packageToBeBound);
            }
            JaxbContextFactory.LOG.debug("Creating new context for package: " + ArrayUtils.toString((Object)classesToBeBound) + " in " + new Duration((ReadableInstant)start2, (ReadableInstant)new DateTime()));
            JaxbContextFactory.CACHE.put(key, context);
        }
        return context;
    }
    
    public static void reset() {
        JaxbContextFactory.CACHE.clear();
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)JaxbContextFactory.class);
        CACHE = new HashMap<String, JAXBContext>();
        JaxbContextFactory.cacheType = "classname";
    }
}
