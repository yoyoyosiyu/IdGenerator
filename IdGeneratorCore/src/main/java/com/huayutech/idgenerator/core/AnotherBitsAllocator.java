/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huayutech.idgenerator.core;


/**
 * Allocate 64 bits for the UID(long)<br>
 * sign (fixed 1bit) -> section -> sequence -> workerId
 * 
 * @author yutianbao
 */
public class AnotherBitsAllocator {
    /**
     * Total 64 bits
     */
    public static final int TOTAL_BITS = 1 << 6;

    /**
     * Bits for [sign-> second-> workId-> sequence]
     */
    private int signBits = 1;
    private final int sectionBits;
    private final int workerIdBits;
    private final int sequenceBits;

    /**
     * Max value for workId & sequence
     */
    private final long maxSectionCode;
    private final long maxWorkerId;
    private final long maxSequence;

    /**
     * Shift for timestamp & workerId
     */
    private final int sectionShift;
    private final int sequenceShift;

    /**
     * Constructor with timestampBits, workerIdBits, sequenceBits<br>
     * The highest bit used for sign, so <code>63</code> bits for timestampBits, workerIdBits, sequenceBits
     */
    public AnotherBitsAllocator(int sectionBits, int workerIdBits, int sequenceBits) {
        // make sure allocated 64 bits
        int allocateTotalBits = signBits + sectionBits + workerIdBits + sequenceBits;
        assert allocateTotalBits <= TOTAL_BITS : "allocate does not more thant 64 bits";

        // initialize bits
        this.sectionBits = sectionBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;

        // initialize max value
        this.maxSectionCode = ~(-1L << sectionBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        // initialize shift
        this.sectionShift = workerIdBits + sequenceBits;
        this.sequenceShift = workerIdBits;
    }

    /**
     * Allocate bits for UID according to delta seconds & workerId & sequence<br>
     * <b>Note that: </b>The highest bit will always be 0 for sign
     * 
     * @param sectionCode
     * @param workerId
     * @param sequence
     * @return
     */
    public long allocate(long sectionCode, long workerId, long sequence) {
        return (sectionCode << sectionShift) | (sequence << sequenceShift) | workerId;
    }
    
    /**
     * Getters
     */
    public int getSignBits() {
        return signBits;
    }

    public int getSectionBits() {
        return sectionBits;
    }

    public int getWorkerIdBits() {
        return workerIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public long getMaxSectionCode() {
        return maxSectionCode;
    }

    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    public long getMaxSequence() {
        return maxSequence;
    }

    public int getSectionShift() {
        return sectionShift;
    }

    public int getSequenceShift() {
        return sequenceShift;
    }

}