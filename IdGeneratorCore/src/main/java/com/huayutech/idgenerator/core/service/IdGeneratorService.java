package com.huayutech.idgenerator.core.service;

import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.executor.RingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.executor.impl.DefaultRingBufferPaddingExecutor;
import com.huayutech.idgenerator.core.exception.UidGenerateException;
import com.huayutech.idgenerator.core.provider.IdProvider;
import com.huayutech.idgenerator.core.provider.impl.SequenceIdProvider;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class IdGeneratorService {

    Logger logger = Logger.getLogger(IdGeneratorService.class.getName());

    public static int DEFAULT_BUFFER_SIZE = 256 << 3;

    private final AtomicBoolean paused = new AtomicBoolean(false);

    private final ExecutorService executorService;

    private final HashMap<String, IdProvider> paddingExecutorHashMap = new HashMap<>();


    public IdGeneratorService(ExecutorService executorService, IdProvider defaultIddProvider) {

        this.executorService = executorService;

        int cores = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cores*2);

        registryScheme("", defaultIddProvider == null ? new SequenceIdProvider("global", executorService, null) : defaultIddProvider);

    }


    public void registryScheme(String name, IdProvider provider) {
        if (paddingExecutorHashMap.containsKey(name))
            throw new UidGenerateException(String.format("Id generator scheme %s have already exist", name));

        paddingExecutorHashMap.put(name, provider);
    }

    public boolean isRunning() {
        return !paused.get();
    }

    public long generate() {
        return generate("");
    }

    public long generate(String scheme) {
        if (!isRunning())
            throw new UidGenerateException("service have been paused now");

        IdProvider provider = (IdProvider)paddingExecutorHashMap.get(scheme);

        if (provider == null)
            throw new UidGenerateException(String.format("Id generator scheme %s does not exist", scheme));

        return provider.generate();
    }


}
