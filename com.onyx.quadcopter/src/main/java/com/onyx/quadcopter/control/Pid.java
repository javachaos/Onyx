package com.onyx.quadcopter.control;

/**
 * Represents a PID along one axis.
 * @author fred
 *
 */
public class Pid {
    
    private float pGain;
    private float iGain;
    private float dGain;

    /**
     * Create a new PID for one axis.
     * @param pGain
     * @param iGain
     * @param dGain
     */
    public Pid(float pGain, float iGain, float dGain) {
	this.setPGain(pGain);
	this.setIGain(iGain);
	this.setDGain(dGain);
    }

    /**
     * @return the pGain
     */
    public float getPGain() {
	return pGain;
    }

    /**
     * @param pGain the pGain to set
     */
    public void setPGain(float pGain) {
	this.pGain = pGain;
    }

    /**
     * @return the iGain
     */
    public float getIGain() {
	return iGain;
    }

    /**
     * @param iGain the iGain to set
     */
    public void setIGain(float iGain) {
	this.iGain = iGain;
    }

    /**
     * @return the dGain
     */
    public float getDGain() {
	return dGain;
    }

    /**
     * @param dGain the dGain to set
     */
    public void setDGain(float dGain) {
	this.dGain = dGain;
    }
}
