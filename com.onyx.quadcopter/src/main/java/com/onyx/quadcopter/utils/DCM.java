package com.onyx.quadcopter.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;

public class DCM extends Device {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(DCM.class);
    private static final float ACC_WEIGHT = 0.1f;
    private static final float MAG_WEIGHT = 0.1f;
    private float[][] dcm = new float[3][3];
    private long intr_t = 0;		
    private long time_t = 0;
    private long prev_t = 0;
    private float[] accel = new float[3];
    private float[] magni = new float[3];
    private float[][] data = new float[3][3];

    private boolean dataReady = false;
    
    /**
     * Create a new DCM.
     * @param c
     */
    public DCM(final Controller c) {
	super(c, DeviceID.DCM);
	dcm = new float[][] {
	    {1, 0, 0},
	    {0, 1, 0},
	    {0, 0, 1}
	};
    }
    
    private float[][] rotate(float[][] m, float[] w) {
	int i;
	float[] dR = new float[3];
	//update matrix using formula R(t+1)= R(t) + dR(t) = R(t) + w x R(t)
	for(i = 0; i < 3; i++){
	    dR = crossProduct(w,m[i]);
	    m[i] = sum(m[i],dR);
	}
	//make matrix orthonormal again
	orthonormalize(m);
	return m;
    }
    
    private float[][] orthonormalize(float[][] m) {
	float err = dotProduct((m[0]),(m[1]));
	float[][] delta = new float[2][3];
	delta[0] = scalarProduct(-err/2,(m[1]));
	delta[1] = scalarProduct(-err/2,(m[0]));
	m[0] = sum((m[0]),(delta[0]));
	m[1] = sum((m[1]),(delta[1]));
	m[2] = crossProduct((m[0]),(m[1]));
	m[0] = normalize(m[0]);
	m[1] = normalize(m[1]);
	m[2] = normalize(m[2]);
	return m;
    }
    
    /**
     * Return the length of a vector a.
     * @param a
     * @return
     */
    private float magnitude(float[] a) {
	float R;
	R = a[0]*a[0];
	R += a[1]*a[1];
	R += a[2]*a[2];
	return (float) Math.sqrt(R);
    }
    
    /**
     * Normalize a vector a.
     */
    private float[] normalize(float[] a) {
	float R;  
	R = magnitude(a);
	a[0] /= R;
	a[1] /= R; 
	a[2] /= R;  
	return a;
    }
    
    /**
     * Return the sum of a and b.
     * @param a
     * @param b
     * @return
     */
    private float[] sum(float[] a, float[] b) {
	float[] c = new float[a.length];
	c[0] = a[0] + b[0];
	c[1] = a[1] + b[1];
	c[2] = a[2] + b[2];
	return c;
    }
    
    /**
     * Perform simple cross product.
     * @param a
     * @param b
     * @return
     */
    private float[] crossProduct(float[] a, float[] b) {
	float[] c = new float[3];
	c[0] = a[1] * b[2] - a[2] * b[1];
	c[1] = a[2] * b[0] - a[0] * b[2];
	c[2] = a[0] * b[1] - a[1] * b[0];
	return c;
    }
    
    /**
     * Perform scalar product
     * @param a
     * @param b
     * @return
     */
    private float[] scalarProduct(float a, float[] b) {
	float[] c = new float[3];
	c[0] = a * b[0];
	c[1] = a * b[1];
	c[2] = a * b[2];
	return c;
    }
    
    /**
     * Perform simple dot product.
     * @param a
     * @param b
     * @return
     */
    private float dotProduct(float[] a, float[] b) {
	return (a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]);
    }
    
    /**
     * Update this DCM
     * @param data
     * @return
     */
    @Override
    public void update() {
	
	handleMessage();
	//interval since last call
	time_t = System.currentTimeMillis();
	intr_t = time_t - prev_t;
	prev_t = time_t;
	//---------------
	// I,J,K unity vectors of global coordinate system I-North,J-West,K-zenith
	// i,j,k unity vectors of body's coordiante system  i-"nose", j-"left wing", k-"top"
	//---------------
	//			[I.i , I.j, I.k]
	// DCM =  	        [J.i , J.j, J.k]
	//			[K.i , K.j, K.k]  


	//---------------
	//Accelerometer
	//---------------
	//Accelerometer measures gravity vector G in body coordinate system
	//Gravity vector is the reverse of K unity vector of global system expressed in local coordinates
	//K vector coincides with the z coordinate of body's i,j,k vectors expressed in global coordinates (K.i , K.j, K.k)
	if(!dataReady)
	    return;
	
	//Acc can estimate global K vector(zenith) measured in body's coordinate systems (the reverse of gravitation vector)	
	accel[0] = -data[1][0];	
	accel[1] = -data[1][1];
	accel[2] = -data[1][2];
	accel = normalize(accel);
	//calculate correction vector to bring dcmEst's K vector closer to Acc vector (K vector according to accelerometer)
	float[] wA = new float[3]; 
	wA = crossProduct(dcm[2],accel);	// wA = Kgyro x	 Kacc , rotation needed to bring Kacc to Kgyro

	//---------------
	//Magnetomer
	//---------------
	//calculate correction vector to bring dcmEst's I vector closer to Mag vector (I vector according to magnetometer)
	float[] wM = new float[3]; 
	//in the absense of magnetometer let's assume North vector (I) is always in XZ plane of the device (y coordinate is 0)
	magni[0] = data[2][0];
	magni[1] = data[2][1];
	magni[2] = data[2][2];
	
	wM = crossProduct(dcm[0],magni);	// wM = Igyro x Imag, roation needed to bring Imag to Igyro

	//---------------
	//dcmEst
	//---------------
	//gyro rate direction is usually specified (in datasheets) as the device's(body's) rotation 
	//about a fixed earth's (global) frame, if we look from the perspective of device then
	//the global vectors (I,K,J) rotation direction will be the inverse
	float[] w = new float[3];					//gyro rates (angular velocity of a global vector in local coordinates)
	w[0] = -data[0][0];	//rotation rate about accelerometer's X axis (GY output) in rad/ms
	w[1] = -data[0][1];	//rotation rate about accelerometer's Y axis (GX output) in rad/ms
	w[2] = -data[0][2];	//rotation rate about accelerometer's Z axis (GZ output) in rad/ms
	for(int i = 0; i < 3; i++){
		w[i] *= intr_t;	//scale by elapsed time to get angle in radians
		//compute weighted average with the accelerometer correction vector
		w[i] = (float) ((w[i] + ACC_WEIGHT*wA[i] + MAG_WEIGHT*wM[i])/(1.0+ACC_WEIGHT+MAG_WEIGHT));
	}
	
	dcm = rotate(dcm,w);
    }
    
    private void handleMessage() {
	final ACLMessage aclMessage = new ACLMessage(MessageType.SEND);
	aclMessage.setActionID(ActionId.GET_ORIENT);
	aclMessage.setReciever(DeviceID.GYRO_MAG_ACC);
	aclMessage.setSender(getId());
	getController().getBlackboard().addMessage(aclMessage);
	if(isNewMessage()) {
	    String[] memsdata = getLastACLMessage().getContent().split(";");
	    String[] gyrodata = memsdata[0].split(":");
	    data[0][0] = Float.parseFloat(gyrodata[0]);
	    data[0][1] = Float.parseFloat(gyrodata[1]);
	    data[0][2] = Float.parseFloat(gyrodata[2]);
	    String[] acceldata = memsdata[1].split(":");
	    data[1][0] = Float.parseFloat(acceldata[0]);
	    data[1][1] = Float.parseFloat(acceldata[1]);
	    data[1][2] = Float.parseFloat(acceldata[2]);
	    String[] magdata = memsdata[2].split(":");
	    data[2][0] = Float.parseFloat(magdata[0]);
	    data[2][1] = Float.parseFloat(magdata[1]);
	    data[2][2] = Float.parseFloat(magdata[2]);
	    dataReady = true;
	}
    }

    @Override
    protected void init() {
    }

    @Override
    public void shutdown() {
	data = null;
    }

    @Override
    protected void alternate() {
	LOGGER.debug("Current body vector: "+ dcm[2][0] + ", " + dcm[2][1] + ", " + dcm[2][2]);
	LOGGER.debug("Current Angle: " + Math.sqrt(dcm[2][0]*dcm[2][0] + dcm[2][1]*dcm[2][1]));
	LOGGER.debug("Pitch and roll: " + Math.acos(dcm[2][2]));
	LOGGER.debug("Update time in millis: " + intr_t);
    }

    @Override
    public boolean selfTest() {
	return true;
    }

}