package com.onyx.quadcopter.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean up class.
 * 
 * @author fred
 *
 */
public class Cleaner {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Cleaner.class);

    /**
     * Reference to the list of objects to be cleaned.
     */
    private List<Object> items;

    /**
     * Constructor.
     */
    public Cleaner() {
	items = new ArrayList<Object>();
    }

    /**
     * Clean up a single java object.
     *
     * @param toClean
     *            the object to be cleaned.
     */
    public final void cleanUp(final Object toClean) {
	if (toClean != null) {
	    items.add(toClean);
	}
    }

    /**
     * Clean up items. Avoid calling in large loops (calls GC).
     */
    public final void doClean() {
	for (Object c : items) {
	    if (c != null) {
		c = null;
	    }
	}
	items.clear();
	items = null;
	gc();
    }

    /**
     * Call Garbage collector.
     */
    public final void gc() {
	System.gc();
    }
}
