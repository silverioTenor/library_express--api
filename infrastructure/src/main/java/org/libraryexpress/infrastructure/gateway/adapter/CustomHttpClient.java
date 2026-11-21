package org.libraryexpress.infrastructure.gateway.adapter;

import java.io.IOException;
import org.libraryexpress.infrastructure.gateway.contract.ClientResponse;

public interface CustomHttpClient {
    ClientResponse get(String url) throws IOException, InterruptedException;
}
