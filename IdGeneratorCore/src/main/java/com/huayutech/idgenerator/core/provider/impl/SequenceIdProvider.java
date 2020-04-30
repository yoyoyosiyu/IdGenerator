package com.huayutech.idgenerator.core.provider.impl;

import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.executor.RingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.executor.impl.DefaultRingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.persisten.IdPersistence;
import com.huayutech.idgenerator.core.provider.IdProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SequenceIdProvider implements IdProvider {

    long currentId;

    IdPersistence idPersistence;

    RingBuffer ringBuffer;

    RingBufferPaddingExecutor executor;

    String name;


    public SequenceIdProvider(String name, ExecutorService executorService, IdPersistence idPersistence) {
        this.idPersistence = idPersistence;
        this.name = name;

        if (idPersistence != null) {
            Object id = idPersistence.get(name,"lastId");
            if (id == null)
                currentId = 0;
            else {
                currentId = Long.parseLong((String)id);
            }
        }
        else {
            currentId = 0;
        }

        ringBuffer = new RingBuffer(1000, 50);
        executor = new DefaultRingBufferPaddingExecutor(ringBuffer, this::nextIds, executorService);
        ringBuffer.setBufferPaddingExecutor(executor);
    }

    @Override
    public long generate() {
        return ringBuffer.take();
    }


    public Collection<Long> nextIds(long suggestCount) {

        suggestCount = suggestCount <= 0 ? 1000 : suggestCount;

        List<Long> list = new ArrayList<>();
        //synchronized (this) {

            for (int i = 0; i < suggestCount; i++) {
                list.add(currentId++);
            }

            if (idPersistence != null) {
                idPersistence.set(name,"lastId", currentId);
                idPersistence.persist();
            }
        //

        return list;
    }
}
