package org.libraryexpress.infrastructure.gateway.adapter;

import org.libraryexpress.infrastructure.gateway.contract.ClientResponse;

import java.io.IOException;

public interface CustomHttpClient {
    ClientResponse get(String url) throws IOException, InterruptedException;
}
