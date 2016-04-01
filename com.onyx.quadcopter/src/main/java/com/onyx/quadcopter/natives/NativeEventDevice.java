package com.onyx.quadcopter.natives;

/**
 * This file is part of evdev-java - Java interface to native code.
 *
 * evdev-java - Java interface to native code is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * evdev-java - Java interface to native code is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * evdev-java - Java interface to native code. If not, see
 * <http://www.gnu.org/licenses/>.
 */

public class NativeEventDevice {

    public native boolean ioctlGetID(String device, short[] resp);

    public native int ioctlGetEvdevVersion(String device);

    public native boolean ioctlGetDeviceName(String device, byte[] resp);

    public native boolean ioctlEVIOCGBIT(String device, long[] resp, int start, int stop);

    public native boolean ioctlEVIOCGABS(String device, int[] resp, int axis);

}
