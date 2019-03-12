package com.threathunter.basictools.eslogger;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.Service;
import com.threathunter.babel.rpc.impl.ServerContainerImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class EsloggerServer {

    private ServerContainerImpl serverContainer;
    private Service eslogService;

    public EsloggerServer(String esConfig, String serviceMetaFile) {
        this.serverContainer = new ServerContainerImpl();

        Yaml yaml = new Yaml();
        ObjectMapper mapper = new ObjectMapper();
        InputStream isEsConfig;
        InputStream isServiceMeta;
        try {
            isEsConfig = new FileInputStream(new File(esConfig));
            isServiceMeta = new FileInputStream(new File(serviceMetaFile));
            this.eslogService = new EsloggerService((Map<String, Object>)yaml.load(isEsConfig), mapper.readValue(isServiceMeta, Map.class));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void startServer() {
        this.serverContainer.addService(this.eslogService);
        this.serverContainer.start();
    }

    public ServiceMeta getServiceMeta() {
        return this.eslogService.getServiceMeta();
    }

    public void stopServer() {
        serverContainer.stop();
    }
}
