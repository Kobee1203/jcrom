package org.jcrom;

import java.io.File;
import java.net.URL;
import java.util.logging.LogManager;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.TransientRepository;
import org.junit.After;
import org.junit.Before;

public class TestAbstract {

    protected Repository repo;
    protected Session session;
    protected String userID = "admin";
    protected char[] password = "admin".toCharArray();

    @Before
    public void setUpRepository() throws Exception {
        repo = new TransientRepository();
        session = repo.login(new SimpleCredentials(userID, password));

        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource("logger.properties");
        if (url == null) {
            url = loader.getResource("/logger.properties");
        }
        LogManager.getLogManager().readConfiguration(url.openStream());
    }

    @After
    public void tearDownRepository() throws Exception {
        session.logout();
        deleteDir(new File("repository"));
        new File("repository.xml").delete();
        new File("derby.log").delete();
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String element : children) {
                boolean success = deleteDir(new File(dir, element));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
