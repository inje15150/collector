import mariadb.DataBaseCon;

import java.util.List;
import java.util.Set;

public class DBTest {

    public static void main(String[] args) {

        DataBaseCon dataBaseCon = new DataBaseCon();
        dataBaseCon.connection();

    }
}
