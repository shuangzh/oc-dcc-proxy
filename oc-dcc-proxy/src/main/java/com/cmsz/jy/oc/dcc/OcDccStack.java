package com.cmsz.jy.oc.dcc;

import org.jdiameter.api.*;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by admin on 2017/2/20.
 */

@Component
public class OcDccStack  implements CommandLineRunner {

    @Value("${configFile}")
    private String configFile;

    @Value("${dictionaryFile}")
    private String dictionaryFile;

    private Stack stack;
    private Configuration configuration;
    private SessionFactory factory;
    private Network network;
    private AvpDictionary dictionary = AvpDictionary.INSTANCE;

    private Map<ApplicationId, NetworkReqListener> listenerMap= new HashMap<>();

    @PostConstruct
    private  void init() {
        String configpath = getClass().getResource("/").getPath() + this.configFile;
        String dictionarypath = getClass().getResource("/").getPath() + this.dictionaryFile;
        try {
            dictionary.parseDictionary(dictionarypath);
            configuration = new XMLConfiguration(new FileInputStream(configpath));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        stack = new StackImpl();
        try {
            factory = stack.init(configuration);
            network = stack.unwrap(Network.class);
        } catch (Exception e) {
            e.printStackTrace();
            if(stack!=null)
                stack.destroy();
            return;
        }
    }

    public void addReqListener(NetworkReqListener listener, ApplicationId applicationId){
        this.listenerMap.put(applicationId, listener);
    }

    @Override
    public void run(String... strings) throws Exception {
        for(ApplicationId applicationId : listenerMap.keySet()) {
            network.addNetworkReqListener(listenerMap.get(applicationId), applicationId);
        }
        stack.start();
    }


    public Session getNewSession(String sessionid) throws InternalException {
        return factory.getNewSession(sessionid);
    }
}
