package pl.edu.agh.Analyzer.util;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import pl.edu.agh.Analyzer.repository.LanguageRepository;

import javax.persistence.EntityManager;

/**
 * Created by pawel on 15.07.17.
 */
public class DbUtil {
    private static DbUtil instance = null;
    private ConfigurableApplicationContext configurableApplicationContext;
    private EntityManager entityManager;


    private  DbUtil(){}

    public static DbUtil getInstance(ConfigurableApplicationContext configurableApplicationContext){
        if (instance == null) {
            instance = new DbUtil();
            instance.configurableApplicationContext = configurableApplicationContext;
            instance.entityManager = configurableApplicationContext.getBean(EntityManager.class);
        }
        return instance;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

    public void setConfigurableApplicationContext(ConfigurableApplicationContext configurableApplicationContext) {
        this.configurableApplicationContext = configurableApplicationContext;
    }
}
