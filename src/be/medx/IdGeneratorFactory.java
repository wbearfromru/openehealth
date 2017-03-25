package be.medx;

import java.util.HashMap;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import java.util.Map;
import org.slf4j.Logger;

public final class IdGeneratorFactory
{
    private static final Logger LOG;
    public static final String PROPS_IDGENERATOR_PREFIX = "be.ehealth.technicalconnector.idgenerator.";
    public static final String PROPS_IDGENERATOR_SUFFIX = ".classname";
    public static final String DEFAULT = "default";
    public static final String UUID = "uuid";
    public static final String XSID = "xsid";
    public static final String TIMEBASED = "time";
    public static final String NANO = "nano";
    private static final String DEFAULT_INPUT_REF_GENERATOR_CHECKER_CLASS;
    private static Map<String, IdGenerator> cachedInstance;
    private static Map<String, String> defaultGeneratorClasses;
    
    public static IdGenerator getIdGenerator() throws TechnicalConnectorException {
        return getIdGenerator("default");
    }
    
    public static IdGenerator getIdGenerator(final String type) throws TechnicalConnectorException {
        if (!IdGeneratorFactory.cachedInstance.containsKey(type)) {
            final String defaultimpl = StringUtils.defaultString((String)IdGeneratorFactory.defaultGeneratorClasses.get(type), IdGeneratorFactory.DEFAULT_INPUT_REF_GENERATOR_CHECKER_CLASS);
            final ConfigurableFactoryHelper<IdGenerator> helper = new ConfigurableFactoryHelper<IdGenerator>("be.ehealth.technicalconnector.idgenerator." + type + ".classname", defaultimpl);
            IdGeneratorFactory.cachedInstance.put(type, helper.getImplementation());
        }
        return IdGeneratorFactory.cachedInstance.get(type);
    }
    
    public static void invalidateCachedInstance() {
        IdGeneratorFactory.cachedInstance.clear();
    }
    
    public static void registerDefaultImplementation(final String type, final Class<? extends IdGenerator> clazz) {
        if (IdGeneratorFactory.defaultGeneratorClasses.containsKey(type)) {
            IdGeneratorFactory.LOG.warn("Default implementation already exist for type [" + type + "] with value [" + IdGeneratorFactory.defaultGeneratorClasses.get(type) + "] replaced by" + clazz.getName());
        }
        IdGeneratorFactory.defaultGeneratorClasses.put(type, clazz.getName());
    }
    
    static {
        LOG = LoggerFactory.getLogger(IdGeneratorFactory.class);
        DEFAULT_INPUT_REF_GENERATOR_CHECKER_CLASS = DateTimeIdGenerator.class.getName();
        IdGeneratorFactory.cachedInstance = new HashMap<String, IdGenerator>();
        (IdGeneratorFactory.defaultGeneratorClasses = new HashMap<String, String>()).put("uuid", UUIDGenerator.class.getName());
        IdGeneratorFactory.defaultGeneratorClasses.put("xsid", XSIDGenerator.class.getName());
        IdGeneratorFactory.defaultGeneratorClasses.put("default", IdGeneratorFactory.DEFAULT_INPUT_REF_GENERATOR_CHECKER_CLASS);
        IdGeneratorFactory.defaultGeneratorClasses.put("nano", NanoTimeGenerator.class.getName());
    }
}
