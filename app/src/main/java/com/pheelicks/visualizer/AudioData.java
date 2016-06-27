/**
 * Copyright 2011, Felix Palmer
 * <p/>
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.pheelicks.visualizer;

// Data class to explicitly indicate that these bytes are raw audio data
public class AudioData {
    public byte[] bytes;

    public AudioData(byte[] bytes) {
        this.bytes = bytes;
    }
}
