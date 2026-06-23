package com.featureflag.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "ff")
public interface FfConfig {
    Admin admin();
    Token token();

    interface Admin {
        String username();
        String password();
    }

    interface Token {
        String pepper();
    }
}
