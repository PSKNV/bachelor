package ru.ifmo.mailru.core;

import ru.ifmo.mailru.robottxt.PolitenessModule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HostController {
	public final String host;
	private long lastRequest;
	private boolean canRequest;
	private long interval = 1000;
    private PolitenessModule politenessModule;
    private boolean tryAddedPolitenessModule;
    private int pageNumber;
    public static final int maxCount = 500;
    public final Lock lock;
	
	public HostController(String host) {
        this.pageNumber = 0;
		this.host = host;
		lastRequest = 0;
		canRequest = true;
        tryAddedPolitenessModule = false;
        lock = new ReentrantLock();
    }

    public void addPolitenessModule() {
        if (tryAddedPolitenessModule) {
            return;
        }
        try {
            this.politenessModule = new PolitenessModule(this);
        } catch (IOException | URISyntaxException e) {
        }
        tryAddedPolitenessModule = true;
    }
	
	public synchronized boolean canRequest() {
		canRequest = System.currentTimeMillis() - lastRequest > interval;
		return canRequest;
	}
	
	public synchronized void request() {
		canRequest = false;
		lastRequest = System.currentTimeMillis();
	}

    public boolean isItTooMuch() {
        return pageNumber >= maxCount;
    }

    public void incNumber() {
        pageNumber++;
    }

    public boolean canAdd(URI uri) {
        boolean ret = false;
        if (!isItTooMuch()) {
            ret = politenessModule == null || politenessModule.isAllow(uri);
        }
        return ret;
    }
}
