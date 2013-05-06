package ru.ifmo.mailru.core;

import ru.ifmo.mailru.robottxt.PolitenessModule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HostController {
	private String host;
	private long lastRequest;
	private boolean canRequest;
	private long interval = 1000;
    private PolitenessModule politenessModule;
    private int pageNumber;
    public static final int maxCount = 100;
	
	public HostController(String host) throws URISyntaxException {
        this.pageNumber = 0;
		this.host = host;
		lastRequest = 0;
		canRequest = true;
        try {
            this.politenessModule = new PolitenessModule(host);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
	
	public synchronized boolean canRequest() {
		canRequest = System.currentTimeMillis() - lastRequest > interval;
		return canRequest;
	}
	
	public synchronized void request() {
		canRequest = false;
		lastRequest = System.currentTimeMillis();
	}

    private boolean isItTooMuch() {
        return pageNumber >= maxCount;
    }

    public boolean addIfCan(URI uri) {
        boolean ret = false;
        if (!isItTooMuch()) {
            ret = politenessModule == null || politenessModule.isAllow(uri);
            if (ret) pageNumber++;
        }
        return ret;
    }
}
