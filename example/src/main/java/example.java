import com.huayutech.idgenerator.core.buffer.RingBuffer;
import com.huayutech.idgenerator.core.exception.UidGenerateException;
import com.huayutech.idgenerator.core.provider.IdProvider;
import com.huayutech.idgenerator.core.provider.impl.SequenceIdProvider;
import com.huayutech.idgenerator.core.provider.impl.SnowFlakeIdProvider;
import com.huayutech.idgenerator.core.provider.impl.VariantSnowFlakeIdProvider;
import com.huayutech.idgenerator.core.service.IdGeneratorService;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class example {

    public static void main(String[] args) throws IOException {

        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(cores*2);

        IniIdPersistence idPersistence = new IniIdPersistence("setting.ini");


        IdProvider idProvider = new SequenceIdProvider("global", executorService, idPersistence);

        IdGeneratorService service = new IdGeneratorService(executorService, idProvider);

        service.registryScheme("snowflake", new SnowFlakeIdProvider("snowflake", executorService, 2000));


        idProvider = new VariantSnowFlakeIdProvider("variantSnowflake", executorService, idPersistence, 1);
        service.registryScheme("variantSnowflake", idProvider);



        Thread consumerThread = new Thread() {
            @Override
            public void run()  {
                do {
                    try {
                        long id = service.generate();
                        sleep(10L);
                        //System.out.println("normal " + id);
                    }
                    catch (UidGenerateException e) {
                        e.printStackTrace();
                        return;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                } while(true);

            }
        };

        Thread consumerThread1 = new Thread() {
            @Override
            public void run()  {
                do {
                    try {
                        long id = service.generate("variantSnowflake");
                        sleep(10L);
                        System.out.println("variant snow flake: "+id);
                    }
                    catch (UidGenerateException e) {
                        e.printStackTrace();
                        return;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                } while(true);

            }
        };


        consumerThread.start();
        //consumerThread1.start();
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        //consumerThread.stop();


        System.out.println("finish");
    }
}
