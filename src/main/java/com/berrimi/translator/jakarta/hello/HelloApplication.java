package com.berrimi.translator.jakarta.hello;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("api")
public class HelloApplication extends Application {
    // Needed to enable Jakarta REST and specify path.
}
