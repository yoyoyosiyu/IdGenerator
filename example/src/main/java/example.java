import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.buffer.RingBufferPaddingExecutor;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public class example {

    public static void main(String[] args) throws IOException {

        File file = new File("setting.ini");

        if (!file.exists())
            file.createNewFile();

        Wini wini = new Wini(file);

        wini.put("global", "commodity", 10);

        wini.store();

        int bufferSize = 256 << 3;

        RingBuffer ringBuffer = new RingBuffer(bufferSize);

        Thread consumerThread = new Thread() {
            @Override
            public void run()  {
                do {
                    try {
                        long id = ringBuffer.take();
                        sleep(100L);
                        System.out.println(id);
                    }
                    catch (Exception e) {
                    }

                } while(true);
            }
        };

        Thread producerThread = new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    if (ringBuffer.put(i))
                        i++;
                    else
                        System.out.println("RingBuffer fulled");
                }
            }
        };

        RingBufferPaddingExecutor paddingExecutor = new RingBufferPaddingExecutorImpl(ringBuffer);


        //producerThread.start();
        consumerThread.start();

        System.in.read();

        consumerThread.stop();
        consumerThread.stop();


        System.out.println("finish");
    }
}
