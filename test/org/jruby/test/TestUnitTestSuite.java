/*
 * TestUnitTestSuite.java
 * JUnit based test
 *
 * Created on January 15, 2007, 4:06 PM
 */

package org.jruby.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.exceptions.RaiseException;

/**
 *
 * @author headius
 */
public class TestUnitTestSuite extends TestSuite {
    private static final String TEST_DIR = "test";
    private static final String REGEXP_TEST_CASE_SUBCLASS = "^\\s*class\\s+([^\\s]+)\\s*<.*TestCase\\s*$";
    private static final Pattern PATTERN_TEST_CASE_SUBCLASS = Pattern.compile(REGEXP_TEST_CASE_SUBCLASS);

    /**
     * suite method automatically generated by JUnit module
     */
    public TestUnitTestSuite(String testIndex) throws Exception {
        File testDir;
        if (System.getProperty("basedir") != null) {
            testDir = new File(System.getProperty("basedir"), "target/test-classes/" + TEST_DIR);
        } else {
            testDir = new File(TEST_DIR);
        }

        File testIndexFile = new File(testDir, testIndex);

        if (!testIndexFile.canRead()) {
            // Since we don't have any other error reporting mechanism, we
            // add the error message as an always-failing test to the test suite.
            addTest(new FailingTest("TestUnitTestSuite",
                                          "Couldn't locate " + testIndex +
                                          ". Make sure you run the tests from the base " +
                                          "directory of the JRuby sourcecode."));
            return;
        }

        BufferedReader testFiles =
            new BufferedReader(new InputStreamReader(new FileInputStream(testIndexFile)));

        String line;
        Interpreter[] ints = new Interpreter[8];
        for (int i = 0; (line = testFiles.readLine()) != null; i++) {
            line = line.trim();
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            }
            if (ints[i % ints.length] == null) {
                ints[i % ints.length] = new Interpreter();
            }
            addTest(createTest(line, testDir, ints[i % ints.length]));
        }
    }

    protected TestCase createTest(String line, File testDir, Interpreter interpreter) {
        return new ScriptTest(line, testDir, interpreter);
    }

    protected static class Interpreter {
        ByteArrayInputStream in;
        ByteArrayOutputStream out;
        ByteArrayOutputStream err;
        PrintStream printOut;
        PrintStream printErr;
        Ruby runtime;

        public Interpreter() {
            in = new ByteArrayInputStream(new byte[0]);
            out = new ByteArrayOutputStream();
            err = new ByteArrayOutputStream();
            printOut = new PrintStream(out);
            printErr = new PrintStream(err);
            runtime = Ruby.newInstance(in, printOut, printErr);
            ArrayList loadPath = new ArrayList();

            loadPath.add("test/externals/bfts");
            loadPath.add("test/externals/ruby_test/lib");

            runtime.getLoadService().init(loadPath);
            runtime.defineGlobalConstant("ARGV", runtime.newArray());
        }

        public void setUp() {
            runtime.setCurrentDirectory(System.getProperty("user.dir"));
        }

        public void tearDown() {
            in.reset();
            out.reset();
            err.reset();
        }
    }

    protected class ScriptTest extends TestCase {
        private final String filename;
        private final File testDir;
        private final Interpreter interpreter;

        public ScriptTest(String filename, File dir, Interpreter interpreter) {
            super(filename);
            this.filename = filename;
            this.testDir = dir;
            this.interpreter = interpreter;
        }

        @Override
        protected void setUp() throws Exception {
            interpreter.setUp();
        }

        @Override
        protected void tearDown() throws Exception {
            interpreter.tearDown();
        }

        protected String generateTestScript(String scriptName, String testClass) {
            StringBuffer script = new StringBuffer();
            script.append("require 'test/junit_testrunner.rb'\n");
            script.append("require '" + scriptName + "'\n");
            script.append("runner = Test::Unit::UI::JUnit::TestRunner.new(" + testClass + ")\n");
            script.append("runner.start\n");
            script.append("runner.faults\n");

            return script.toString();
        }

        private String scriptName() {
            return new File(testDir, filename).getPath();
        }

        private String pretty(List list) {
            StringBuffer prettyOut = new StringBuffer();

            for (Iterator iter = list.iterator(); iter.hasNext();) {
                prettyOut.append(iter.next().toString());
            }

            return prettyOut.toString();
        }

        private List<String> getTestClassNamesFromReadingTestScript(String filename) {
            List<String> testClassNames = new ArrayList<String>();
            File f = new File(TEST_DIR + "/" + filename + ".rb");
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(f));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) break;
                    Matcher m = PATTERN_TEST_CASE_SUBCLASS.matcher(line);
                    if (m.find())
                    {
                        testClassNames.add(m.group(1));
                    }
                }
                if (!testClassNames.isEmpty()) {
                    return testClassNames;
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not close reader!", e);
                    }
                }

            }
            throw new RuntimeException("No *TestCase derivative found in '" + filename + ".rb'!");
        }

        @Override
        public void runTest() throws Throwable {
            List<String> testClassNames = getTestClassNamesFromReadingTestScript(filename);

            // there might be more test classes in a single file, so we iterate over them
            for (String testClass : testClassNames) {
                System.out.println(testClass);
                try {
                    synchronized(interpreter) {
                        RubyArray faults = (RubyArray) interpreter.runtime.executeScript(generateTestScript(scriptName(), testClass), scriptName() + "_generated_test.rb");

                        if (!faults.isEmpty()) {
                            StringBuffer faultString = new StringBuffer("Faults encountered running " + scriptName() + ", complete output follows:\n");
                            for (Iterator iter = faults.iterator(); iter.hasNext();) {
                                String fault = iter.next().toString();

                                faultString.append(fault).append("\n");
                            }

                            System.out.write(interpreter.out.toByteArray());
                            System.err.write(interpreter.err.toByteArray());
                            fail(faultString.toString());
                        }
                    }
                } catch (RaiseException re) {
                    synchronized (interpreter) {
                        System.out.write(interpreter.out.toByteArray());
                        System.err.write(interpreter.err.toByteArray());
                    }
                    fail("Faults encountered running " + scriptName() + ", complete output follows:\n" + re.getException().message + "\n" + pretty(((RubyArray)re.getException().backtrace()).getList()));
                }
            }
        }
    }
}
