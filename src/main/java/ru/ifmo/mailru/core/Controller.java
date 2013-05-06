package ru.ifmo.mailru.core;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Anastasia Lebedeva
 */

public class Controller {
	private Map<String, HostController> hostMap = new ConcurrentHashMap<>();
	private Set<String> crawled = new HashSet<>();
	private Set<WebURL> inQueue = new HashSet<>();
    private Set<WebURL> inProcessing = new HashSet<>();
    private Map<WebURL, Double> ranks = new ConcurrentHashMap<>();
    private PriorityBlockingQueue<WebURL> toCrawl = new PriorityBlockingQueue<>();

    public synchronized WebURL nextURL() {
        WebURL next = toCrawl.poll();
        if (next == null) {
            return null;
        }
        inProcessing.add(next);
        inQueue.remove(next);
        return next;
    }

    public synchronized boolean hasNext() {
        return toCrawl.size() != 0;
    }
	
	public void addAll(Set<WebURL> urls) {
		for (WebURL url: urls) {
            try {
                addHostController(url);
                add(url);
            } catch (URISyntaxException e) {
                System.err.println(e.getMessage());
            }
        }
	}
	
	private synchronized boolean add(WebURL url) throws URISyntaxException {
		if (crawled.contains(url.getUri().toString()) || inProcessing.contains(url)) {
			return false;
		}
        if (inQueue.contains(url)) {
            double rank = ranks.get(url);
            toCrawl.remove(url);
            url.setRank(Math.min(url.getRank(), rank));
        }
       // HostController hc = addHostController(url);
        if (!url.getHostController().addIfCan(url.getUri())) {
            return false;
        }
        ranks.put(url, url.getRank());
		toCrawl.add(url);
		inQueue.add(url);
		return true;
	}

    private HostController addHostController(WebURL url) throws URISyntaxException {
        HostController hc;
        String curHost = url.getUri().getHost();
        if (hostMap.containsKey(curHost)) {
            hc = hostMap.get(curHost);
        } else {
            hc = new HostController(curHost);
            hostMap.put(curHost, hc);
        }
        url.setHostController(hc);
        return hc;
    }
	
	public synchronized void setCrawledURL(WebURL url) {
		crawled.add(url.getUri().toString());
		inProcessing.remove(url);
		//System.out.println(url.getUri().toString());
	}
}
