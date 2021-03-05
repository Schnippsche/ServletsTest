package de.destatis.tests;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Tool
{
  public static Connection getConnection()
  {
    try
    {
      Class.forName("com.mysql.jdbc.Driver");
      return DriverManager
        .getConnection("jdbc:mysql://localhost:3306/regdbtest171?zeroDateTimeBehavior=convertToNull&jdbcCompliantTruncation=false&useCursorFetch=true&useSSL=false", "root", "root");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public static void closeConnection(Connection connection)
  {
    try
    {
      connection.close();
    } catch (SQLException e)
    {
      e.printStackTrace();
    }
  }

  public static void initDatabase(Connection conn)
  {
    try
    {
      ScriptRunner sr = new ScriptRunner(conn);
      //Creating a reader object
      Reader reader = new BufferedReader(new FileReader(Tool.getTestPath().resolve("sql").resolve("prepare.sql").toString()));
      //Running the script
      sr.runScript(reader);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static Path getTestPath()
  {
    try
    {
      URL resource = Tool.class.getClassLoader().getResource("testfiles");
      assert resource != null;
      File file = new File(resource.toURI());
      return file.toPath();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
}
