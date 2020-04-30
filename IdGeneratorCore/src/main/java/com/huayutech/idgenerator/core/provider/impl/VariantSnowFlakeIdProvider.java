package com.huayutech.idgenerator.core.provider.impl;

import com.huayutech.idgenerator.core.AnotherBitsAllocator;
import com.huayutech.idgenerator.core.BitsAllocator;
import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.executor.RingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.executor.impl.DefaultRingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.persisten.IdPersistence;
import com.huayutech.idgenerator.core.provider.IdProvider;
import com.huayutech.idgenerator.core.utils.PaddedAtomicLong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class VariantSnowFlakeIdProvider implements IdProvider {

    private final AnotherBitsAllocator bitsAllocator;
    private final long workerId;

    IdPersistence idPersistence;
    protected PaddedAtomicLong lastSection;

    private final RingBuffer ringBuffer;
    private final RingBufferPaddingExecutor executor;
    private final String name;


    public VariantSnowFlakeIdProvider(String name, ExecutorService executorService, IdPersistence idPersistence, long workerId) {
        this(name, executorService, idPersistence, workerId, 32, 4, 16);
    }

    public VariantSnowFlakeIdProvider(String name, ExecutorService executorService, IdPersistence idPersistence, long workerId, int sectionBits, int workerBits, int seqBits) {
        this.workerId = workerId;
        this.idPersistence = idPersistence;
        bitsAllocator = new AnotherBitsAllocator(sectionBits, workerBits, seqBits);
        this.lastSection = new PaddedAtomicLong(0);
        this.name = name;

        if (idPersistence != null) {
            Object sectionId = idPersistence.get(name,"lastSection");
            if (sectionId != null)
                this.lastSection.set(Long.parseLong((String)sectionId));
        }

        ringBuffer = new RingBuffer(1000, 50);
        executor = new DefaultRingBufferPaddingExecutor(ringBuffer, this::nextIds, executorService);
        ringBuffer.setBufferPaddingExecutor(executor);

    }

    public Collection<Long> nextIds(long suggestCount) {
        return nextIdsForOneSecond(lastSection.getAndIncrement());
    }


    /**
     * Get the UIDs in the same specified second under the max sequence
     *
     * @param currentSecond
     * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
     */
    protected List<Long> nextIdsForOneSecond(long currentSecond) {
        // Initialize result list size of (max sequence + 1)
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;

        List<Long> uidList = new ArrayList<>(listSize);

        synchronized (this) {
            // Allocate the first sequence of the second, the others can be calculated with the offset
            long firstSeqUid = bitsAllocator.allocate(lastSection.getAndIncrement(), workerId, 0L);
            for (int offset = 0; offset < listSize; offset++) {
                uidList.add(firstSeqUid + (offset << bitsAllocator.getWorkerIdBits()));
            }

            if (idPersistence != null) {
                idPersistence.set(name,"lastSection", lastSection.get());
                idPersistence.persist();
            }
        }

        return uidList;
    }


    @Override
    public long generate() {
        return ringBuffer.take();
    }
}
