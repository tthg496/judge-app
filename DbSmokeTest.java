import com.judgeapp.db.DatabaseManager;

public class DbSmokeTest {
    public static void main(String[] args) {
        try {
            DatabaseManager.initDatabase();
            System.out.println("DB_OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
