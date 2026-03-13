package com.bogdan.client.common;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ClientConfigConstant {

    @Value("${p2p.download-dir:./downloads}")
    public  String DOWNLOAD_DIR;

    @Value("${p2p.shared-dir:./shared}")
    public  String SHARED_DIR;

    @Value("${p2p.port:6881}")
    public  int PORT;
}
