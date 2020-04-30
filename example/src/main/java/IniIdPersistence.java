import com.huayutech.idgenerator.core.persisten.IdPersistence;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public class IniIdPersistence implements IdPersistence {
    String section;
    Wini wini;

    public IniIdPersistence(String fileName) throws IOException {

        File file = new File(fileName);

        if (!file.exists())
            file.createNewFile();

        wini = new Wini(file);


    }

    @Override
    public Object get(String section, String key) {
        return wini.get(section, key);
    }

    @Override
    public void set(String section, String key, Object value) {

        wini.put(section, key, value);

    }

    @Override
    public void persist() {

        try {
            wini.store();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
