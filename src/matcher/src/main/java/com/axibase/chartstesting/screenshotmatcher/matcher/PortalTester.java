package com.axibase.chartstesting.screenshotmatcher.matcher;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;
import com.axibase.chartstesting.screenshotmatcher.matcher.capture.ScreenCapturer;
import com.axibase.chartstesting.screenshotmatcher.matcher.capture.TestServerCapturer;
import com.axibase.chartstesting.screenshotmatcher.matcher.capture.WebDriverCapturer;
import com.axibase.chartstesting.screenshotmatcher.matcher.storages.*;
import com.axibase.chartstesting.screenshotmatcher.matcher.storages.hash.Hasher;
import org.apache.commons.cli.*;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Created by aleksandr on 26.09.16.
 */
public class PortalTester implements Callable<Boolean> {
    private static volatile Object falseResultLocker = new Object();
    private static volatile int falseResultCounter = 0;
    private static volatile int MAX_FALSE_RESULTS = Integer.MAX_VALUE;

    private static ChartlabTestingServer server;
    private static PortalSource portalSrc;
    private static String inputFilePath;
    private static String ignoreFilePath;

    private static int threadCount = Runtime.getRuntime().availableProcessors();
    private static Logger _log = Log.getLogger(PortalTester.class);
    private static Integer viewportWidth = null;
    private static Integer viewportHeight = null;

    private static PrintStream output = null;
    private static String outputDir = "output";
    private static String outputFileName = null;

    private WebDriver driver;
    private ScreenCapturer capturer;
    private ScreenshotStorage backupStorage;
    private ScreenshotStorage currentStorage;
    private HashsumStorage hashStorage;
    private Hasher hasher;
    private OutputStorage outputStorage;

    public PortalTester() {
        initWebDriver();
        initScreenCapturer();
        initStorages();
    }

    private void initWebDriver() {
        DesiredCapabilities caps = DesiredCapabilities.phantomjs();
        String[] cliArgs = new String[]{
                "--web-security=false", // Disable cross-domain request protection
                "--webdriver-loglevel=NONE", // Disable logging
        };
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgs);
        driver = new PhantomJSDriver(caps);
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
    }

    private void initScreenCapturer() {
        WebDriverCapturer wdcapt = new TestServerCapturer(driver, server);
        if (viewportWidth != null && viewportHeight != null) {
            wdcapt.setViewport(viewportWidth, viewportHeight);
        }
        capturer = wdcapt;
    }

    private void initStorages() {
        try {
            backupStorage = new LocalScreenshotStorage("screenshots/backup");
            currentStorage = new LocalScreenshotStorage("screenshots/latest");
            outputStorage = new OutputStorage(outputDir);
            LocalMD5Storage md5 = new LocalMD5Storage("md5");
            hasher = md5.getHasher();
            hashStorage = md5;
        } catch (IOException e) {
            _log.warn("Storage initialization failed");
            throw new RuntimeException("storage initialization failure", e);
        }
    }


    public Boolean call() {
        boolean result = true;
        try {
            for (Portal portal = portalSrc.get(); portal != null; portal = portalSrc.get()) {
                boolean shouldContinue = true;
                synchronized (falseResultLocker) {
                    shouldContinue = falseResultCounter <= MAX_FALSE_RESULTS;
                }
                if (!shouldContinue) {
                    _log.warn("Portal testing interrupted");
                    break;
                }

                boolean isBackupMode = !backupStorage.contains(portal);

                File screenshot = null;
                try {
                    screenshot = capturer.capture(portal);
                } catch (TimeoutException e) {
                    if (!isBackupMode) {
                        output.println("[TIMEOUT] " + portal.toString());
                    }
                    _log.warn("Timeout on " + portal.toString());
                    synchronized (falseResultLocker) {
                        falseResultCounter++;
                    }
                    result = false;
                    continue;
                } catch (UnreachableBrowserException e) {
                    if (!isBackupMode) {
                        output.println("[WARNING] WebDriver died during processing " + portal.toString());
                    }

                    _log.warn("WebDriver died during processing " + portal.toString());
                    return result;
                }

                if (isBackupMode) {
                    try {
                        backupStorage.save(portal, screenshot);
                        hashStorage.save(portal, screenshot);
                        _log.info("Saved screenshot for " + portal.toString());
                    } catch (IOException e) {
                        _log.warn("backup screenshot save failed, cause " + e.getMessage());
                    }
                    continue;
                }

                try {
                    currentStorage.save(portal, screenshot);
                } catch (IOException e) {
                    _log.warn("current screenshot save failed, cause " + e.getMessage());
                }

                try {
                    String srcHash = hashStorage.getChecksum(portal);
                    String dstHash = hasher.getHashsum(screenshot);

                    boolean match = srcHash.equals(dstHash);
                    if (!match) {
                        output.println("[FAIL]\t" + portal.toString());
                        try {
                            outputStorage.save(portal, backupStorage.getScreenshot(portal), screenshot);
                        } catch (IOException e) {
                            _log.warn("output screenshots save failed, cause " + e.getMessage());
                        }

                        synchronized (falseResultLocker) {
                            falseResultCounter++;
                        }
                        result = false;
                    } else {
                        output.println("[PASS]\t" + portal.toString());
                    }
                } catch (IOException e) {
                    _log.warn("unable to compare hashsums, cause " + e.toString());
                }

            }
        } finally {
            close();
        }
        return result;
    }

    public void close() {
        try {
            driver.quit();
        } catch (UnreachableBrowserException e) {
            // Do nothing
        }
        _log.info("Webdriver stopped");
    }

    // Application running

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger(PhantomJSDriverService.class.getName()).setLevel(Level.WARNING);
        _log.setDebugEnabled(false);
        parseCommandLineArgs(args);

        initPortalSource();

        try {
            startServer();
            initOutput();

            boolean result = true;
            try {
                List<FutureTask<Boolean>> tasks = runTestingTasks();
                for (FutureTask<Boolean> task : tasks) {
                    try {
                        boolean taskResult = task.get();
                        result = taskResult && result;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                server.close();
                output.close();
            }
            System.exit(result ? 0 : 1);
        } catch (IOException e) {
            _log.warn("Unable to start testing server", e);
            System.exit(-1);
        }
    }

    private static void startServer() throws IOException {
        server = new ChartlabTestingServer();
        server.writeOutputTo("server._log");
        server.start();
    }

    private static List<FutureTask<Boolean>> runTestingTasks() {
        List<FutureTask<Boolean>> tasks = new ArrayList<>(threadCount);
        int spawnAtMilliseconds = 3000;
        int spawnDelay = spawnAtMilliseconds / threadCount;

        for (int i = 0; i < threadCount; i++) {
            PortalTester tester = new PortalTester();
            FutureTask<Boolean> task = new FutureTask<>(tester);
            new Thread(task).start();
            tasks.add(task);
            try {
                Thread.sleep(spawnDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return tasks;
    }

    private static void initOutput() throws FileNotFoundException {
        if (outputFileName != null && outputFileName.length() > 0) {
            File outputFile = new File(outputFileName);
            output = new PrintStream(outputFile);
        } else {
            output = System.out;
        }
    }

    private static void initPortalSource() {
        JSONPortalSource jps= new JSONPortalSource();
        try {
            jps.includeFile(inputFilePath);
            jps.ignoreFile(ignoreFilePath);
        } catch (IOException e) {
            System.err.println("Unable to read file " + inputFilePath);
            System.exit(-1);
        } catch (org.json.simple.parser.ParseException e) {
            System.err.println("Unable to parse JSON file " + inputFilePath);
            System.exit(-1);
        }
        portalSrc = jps;
    }

    private static void parseCommandLineArgs(String[] args) {
        Options cliOptions = registerCLIOptions();
        parseCLIOptions(cliOptions, args);
    }

    private static Options registerCLIOptions() {
        Options opts = new Options();

        Option ignoreFile = Option.builder("i")
                .longOpt("ignore")
                .desc("Ignore URLs from JSON file")
                .numberOfArgs(1)
                .argName("json-file")
                .build();

        Option viewportDimensions = Option.builder()
                .longOpt("viewport")
                .desc("Set viewport dimensions")
                .numberOfArgs(2)
                .argName("width height")
                .build();

        Option threads = Option.builder("j")
                .longOpt("num-threads")
                .desc("Set number of running threads")
                .numberOfArgs(1)
                .argName("threads")
                .build();

        Option bads = Option.builder("b")
                .longOpt("max-bad")
                .desc("Set the maximum number of bad portals")
                .numberOfArgs(1)
                .argName("num")
                .build();

        Option outputDir = Option.builder("d")
                .longOpt("output-dir")
                .desc("Output directory for bad screenshots")
                .numberOfArgs(1)
                .argName("dir")
                .build();

        Option output = Option.builder("o")
                .longOpt("output-file")
                .desc("Output _log file")
                .numberOfArgs(1)
                .argName("file")
                .build();

        opts.addOption(ignoreFile);
        opts.addOption(viewportDimensions);
        opts.addOption(threads);
        opts.addOption(bads);
        opts.addOption(outputDir);
        opts.addOption(output);
        return opts;
    }

    private static void parseCLIOptions(Options opts, String[] args) {
        try {
            CommandLineParser cliParser = new DefaultParser();
            CommandLine cli = cliParser.parse(opts, args);

            // Set blacklist file
            ignoreFilePath = cli.getOptionValue('i', null);

            // Set number of threads
            String threadsStr= cli.getOptionValue('j');
            if (threadsStr != null) {
                threadCount = new Integer(threadsStr);
            }

            // Set viewport dimensions
            String[] dimensions = cli.getOptionValues("viewport");
            if (dimensions != null) {
                viewportWidth  = new Integer(dimensions[0]);
                viewportHeight = new Integer(dimensions[1]);
            }

            // Set max number of bad
            String badsStr= cli.getOptionValue('b');
            if (badsStr != null) {
                MAX_FALSE_RESULTS = new Integer(badsStr);
                if (MAX_FALSE_RESULTS < 0) {
                    MAX_FALSE_RESULTS = Integer.MAX_VALUE;
                }
            }

            // Set outputs
            outputDir = cli.getOptionValue('d', "output");
            outputFileName = cli.getOptionValue('o', null);

            // Set input file
            inputFilePath = cli.getArgs()[0];
        } catch (Exception ex) {
            showUsage(opts);
            System.exit(-1);
        }
    }

    private static void showUsage(Options opts) {
        HelpFormatter fmt = new HelpFormatter();
        fmt.printHelp("java -jar matcher.jar [OPTIONS]... <FILE>", "\nOptions:\n", opts, "");
    }
}
