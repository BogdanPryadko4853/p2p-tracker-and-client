package com.bogdan.client.common;

import org.springframework.beans.factory.annotation.Value;

public class Constant {

    @Value("${p2p.download-dir:./downloads}")
    public static String DOWNLOAD_DIR;
    @Value("${p2p.shared-dir:./shared}")
    public static String SHARED_DIR;
    @Value("${p2p.port:6881}")
    public static int PORT;
}
