package jp.ac.iwatepu.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import jp.ac.iwatepu.util.ScriptRunner;

public class DatabaseCleanupMain {

	public static void main(String[] args) throws SQLException, FileNotFoundException, IOException {
		DatabaseCleanupMain dcm = new DatabaseCleanupMain();
		dcm.run();
	}

	public void run() throws SQLException, FileNotFoundException, IOException {
		ScriptRunner sr = new ScriptRunner(SQLConnector.getInstance().getConnection(), false, false);
		sr.runScript(new BufferedReader(new FileReader("sql/create.sql")));
	}
}
