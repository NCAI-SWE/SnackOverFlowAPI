package com.snackOverFlow.service;

import com.snackOverFlow.entity.Configuration;
import com.snackOverFlow.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Cacheable("configurations")
    public Map<String, String> getAllConfigurations() {
        List<Configuration> configurations = configurationRepository.findAll();
        return configurations.stream()
                .collect(Collectors.toMap(Configuration::getKey, Configuration::getValue));
    }

    public String getConfigValue(String key) {
        return getAllConfigurations().get(key);
    }
}
