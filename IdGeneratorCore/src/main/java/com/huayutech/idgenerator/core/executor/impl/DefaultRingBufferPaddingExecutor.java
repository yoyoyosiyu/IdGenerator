package com.huayutech.idgenerator.core.executor.impl;

import com.huayutech.idgenerator.core.buffer.BufferedUidProvider;
import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.executor.RingBufferPaddingExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultRingBufferPaddingExecutor implements RingBufferPaddingExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RingBuffer.class);

    private final BufferedUidProvider bufferedUidProvider;

    private final ExecutorService executorService;
    private final RingBuffer ringBuffer;
    private final AtomicBoolean running = new AtomicBoolean(false);


    public DefaultRingBufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider bufferedUidProvider, ExecutorService executor) {

        this.bufferedUidProvider = bufferedUidProvider;
        this.executorService = executor;
        this.ringBuffer = ringBuffer;
        ringBuffer.setBufferPaddingExecutor(this);
        paddingBuffer();
    }

    @Override
    public void asyncPadding() {
        assert executorService != null : "Must supply the executor service";
        executorService.submit(this::paddingBuffer);
    }

    void paddingBuffer() {

        if (!running.compareAndSet(false, true)) {
            System.out.print("Padding buffer is still running.");
            return;
        }


        boolean isFullRingBuffer = false;
        int count = 0;
        while (!isFullRingBuffer) {

            Collection<Long> uidList = bufferedUidProvider.provide(-1);
            System.out.println(uidList.size());

            for (Long uid : uidList) {
                isFullRingBuffer = !ringBuffer.put(uid);
                count++;
                if (isFullRingBuffer) {
                    break;
                }
            }
        }

        System.out.println(String.format("padding %d id", count));

        // not running now
        running.compareAndSet(true, false);
        System.out.println("End to padding buffer {}");
    }

    public boolean isRunning() {
        return running.get();
    }

    public RingBuffer getRingBuffer() {
        return ringBuffer;
    }

}
