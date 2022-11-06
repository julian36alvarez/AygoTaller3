package com.myorg;

import software.amazon.awscdk.App;

public final class CdkAygoApp {
    public static void main(final String[] args) {
        App app = new App();

        new CdkAygoStack(app, "CdkAygoStack");

        app.synth();
    }
}
