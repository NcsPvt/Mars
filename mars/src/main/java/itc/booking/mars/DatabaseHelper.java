package itc.booking.mars;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH;
    private static final String DATABASE_NAME = "cache.db";
    private static final int DATABASE_VERSION = 9;
    private static Context myContext;
    private static File dbDir;
    private static File dbFile;

    public DatabaseHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext = context;
        DB_PATH = myContext.getDatabasePath(DATABASE_NAME).getPath().substring(0, myContext.getDatabasePath(DATABASE_NAME).getPath().indexOf(DATABASE_NAME));
        dbDir = new File(DB_PATH);
        dbFile = new File(DB_PATH + DATABASE_NAME);
        createDatabaseIfNotExists(context);
    }

    public static void createDatabaseIfNotExists(Context context) throws IOException {

        if (!dbDir.exists()) {
            dbDir.mkdir();
            BookingApplication.dbNotFound = true;
        } else if (!dbFile.exists())
            BookingApplication.dbNotFound = true;

        if (BookingApplication.dbNotFound) {
            // Open your local DB as the input stream
            InputStream myInput = context.getAssets().open(DATABASE_NAME);

            // Open the empty DB as the output stream
            OutputStream myOutput = new FileOutputStream(dbFile);

            // transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0)
                myOutput.write(buffer, 0, length);

            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
    }

    public boolean backupDB() {
        InputStream myInput;
        try {
            myInput = new FileInputStream(DB_PATH + DATABASE_NAME);

            // Path to the just created empty DB
            String outFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DATABASE_NAME;

            // Open the empty DB as the output stream
            OutputStream myOutput;
            myOutput = new FileOutputStream(outFileName);
            // transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0)
                myOutput.write(buffer, 0, length);
            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // if (backupDB())
        // Toast.makeText(myContext, "DB Backedup on SDCARD as " +
        // DATABASE_NAME, Toast.LENGTH_LONG).show();

        if (dbFile.delete())
            try {
                Toast.makeText(myContext, "Updating DB", Toast.LENGTH_LONG).show();
                createDatabaseIfNotExists(myContext);
            } catch (IOException e) {
                Toast.makeText(myContext, "DB Corrupt, Please re-install", Toast.LENGTH_LONG).show();
            }
    }

}
