import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.buffer.RingBufferPaddingExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RingBufferPaddingExecutorImpl implements RingBufferPaddingExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RingBuffer.class);

    private final ExecutorService bufferPadExecutor;
    private final RingBuffer ringBuffer;
    private final AtomicLong currentId = new AtomicLong(900000000L);
    private final AtomicBoolean running = new AtomicBoolean(false);


    public RingBufferPaddingExecutorImpl(RingBuffer ringBuffer) {

        int cors = Runtime.getRuntime().availableProcessors();
        bufferPadExecutor = Executors.newFixedThreadPool(cors*2);

        this.ringBuffer = ringBuffer;
        ringBuffer.setBufferPaddingExecutor(this);

    }

    @Override
    public void padding(RingBuffer buffer) {
        System.out.println("need to padding the ring buffer");
        bufferPadExecutor.submit(this::paddingBuffer);
    }

    void paddingBuffer() {

        if (!running.compareAndSet(false, true)) {
            LOGGER.info("Padding buffer is still running. {}", ringBuffer);
            return;
        }


        boolean isFullRingBuffer = false;
        while (!isFullRingBuffer) {
            isFullRingBuffer = !ringBuffer.put(currentId.incrementAndGet());
        }

        System.out.println("finish padding buffer");

        // not running now
        running.compareAndSet(true, false);
        LOGGER.info("End to padding buffer {}", ringBuffer);
    }
}
