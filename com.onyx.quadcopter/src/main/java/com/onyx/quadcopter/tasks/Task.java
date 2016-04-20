package com.onyx.quadcopter.tasks;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;

public abstract class Task<T> implements Callable<T>, Comparable<T> {

    public final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * The underlying device which created this task.
     */
    private Device dev;

    /**
     * The priority of this task.
     */
    private int priority;

    /**
     * The task ID for this task.
     */
    private TaskID id;

    /**
     * Construct a new high level operation.
     * 
     * @param id
     *            the id of this task.
     */
    public Task(final TaskID id) {
	setDev(Controller.getInstance());
	if (getDev().isInitialized()) {
	    this.id = id;
	} else {
	    throw new OnyxException("Error surrogate device not initialized.", LOGGER);
	}
	setPriority(Constants.DEFAULT_TASK_PRIORITY);
    }

    /**
     * Construct a new high level operation.
     * 
     * @param id
     *            the id of this task.
     * @param priority
     *            the caller defined priority of this high level operation.
     */
    public Task(final TaskID id, final int priority) {
	this(id);
	setPriority(priority);
    }

    /**
     * Perform this task.
     */
    public abstract void perform();

    /**
     * Called when the task has completed it's job.
     * 
     * @return the completion object.
     */
    protected abstract T complete();

    @Override
    public T call() throws Exception {
	LOGGER.debug("Executing task: " + getName());
	perform();
	LOGGER.debug("Execution complete for task: " + getName());
	return complete();
    }

    /**
     * @return the priority
     */
    public int getPriority() {
	return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(int priority) {
	this.priority = priority;
    }

    @Override
    public int compareTo(Object o) {
	final int BEFORE = -1;
	final int EQUAL = 0;
	final int AFTER = 1;
	if (this == o && o.equals(this)) {
	    return EQUAL;
	}
	return ((Task<?>) o).getPriority() > getPriority() ? AFTER : BEFORE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getDev() == null) ? 0 : getDev().hashCode());
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	result = prime * result + priority;
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof Task)) {
	    return false;
	}
	Task<?> other = (Task<?>) obj;
	if (getDev() == null) {
	    if (other.getDev() != null) {
		return false;
	    }
	} else if (!getDev().equals(other.getDev())) {
	    return false;
	}
	if (id != other.id) {
	    return false;
	}
	if (priority != other.priority) {
	    return false;
	}
	return true;
    }

    /**
     * @return the dev
     */
    public Device getDev() {
	return dev;
    }

    /**
     * @param dev
     *            the dev to set
     */
    public void setDev(Device dev) {
	this.dev = dev;
    }

    /**
     * Return the name of this task
     * 
     * @return
     */
    public String getName() {
	return id.name();
    }

}
