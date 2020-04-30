package com.huayutech.idgenerator.core.provider.impl;

import com.huayutech.idgenerator.core.BitsAllocator;
import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.exception.UidGenerateException;
import com.huayutech.idgenerator.core.executor.RingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.executor.impl.DefaultRingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.provider.IdProvider;
import com.huayutech.idgenerator.core.utils.PaddedAtomicLong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class SnowFlakeIdProvider implements IdProvider {

    private final BitsAllocator bitsAllocator;
    private final long workerId;

    /** Customer epoch, unit as second. For example 2016-05-20 (ms: 1463673600000)*/
    protected String epochStr = "2016-05-20";
    protected long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1463673600000L);

    private final RingBuffer ringBuffer;
    private final RingBufferPaddingExecutor executor;

    protected PaddedAtomicLong lastSecond;

    private final String name;

    public SnowFlakeIdProvider(String name, ExecutorService executorService, long workerId) {
        this(name, executorService, workerId, 28, 22, 13);
    }

    public SnowFlakeIdProvider(String name, ExecutorService executorService, long workerId, int timeBits, int workerBits, int seqBits) {
        this.workerId = workerId;
        this.name = name;
        bitsAllocator = new BitsAllocator(timeBits, workerBits, seqBits);
        this.lastSecond = new PaddedAtomicLong(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        ringBuffer = new RingBuffer(1000, 50);
        executor = new DefaultRingBufferPaddingExecutor(ringBuffer, this::nextIds, executorService);
        ringBuffer.setBufferPaddingExecutor(executor);

    }


    public Collection<Long> nextIds(long suggestCount) {
        return nextIdsForOneSecond(lastSecond.getAndIncrement());
    }


    /**
     * Get the UIDs in the same specified second under the max sequence
     *
     * @param currentSecond
     * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
     */
    protected Collection<Long> nextIdsForOneSecond(long currentSecond) {
        // Initialize result list size of (max sequence + 1)
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);

        // Allocate the first sequence of the second, the others can be calculated with the offset
        long firstSeqUid = bitsAllocator.allocate(currentSecond - epochSeconds, workerId, 0L);
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }

        return uidList;
    }


    @Override
    public long generate() {
        return ringBuffer.take();
    }
}
