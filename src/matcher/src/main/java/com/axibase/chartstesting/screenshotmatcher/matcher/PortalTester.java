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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PortalTester implements Callable<Boolean> {
    private static final Object falseResultLocker = new Object();
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

    private static int retry = 0;

    private WebDriver driver;
    private ScreenCapturer capturer;
    private ScreenshotStorage backupStorage;
    private ScreenshotStorage currentStorage;
    private HashsumStorage hashStorage;
    private Hasher hasher;
    private OutputStorage outputStorage;

    private boolean isBackupMode = false;

    public PortalTester() {
        initWebDriver();
        initScreenCapturer();
        initStorages();
    }

    private void initWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-composited-antialiasing");
        options.addArguments("--disable-canvas-aa");
        options.addArguments("--disable-2d-canvas-clip-aa");
        driver = new ChromeDriver(options);
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
                boolean shouldContinue;
                synchronized (falseResultLocker) {
                    shouldContinue = falseResultCounter <= MAX_FALSE_RESULTS;
                }
                if (!shouldContinue) {
                    _log.warn("Portal testing interrupted");
                    break;
                }

                _log.info("start processing portal " + portal.getConfigId() + "/" + portal.getRevisionString() );

                boolean subresult = false;
                try {
                    for (int tries = retry + 1; tries > 0; tries--) {
                        subresult = subresult || checkScreenshot(portal);
                        if (subresult) break;
                        if (tries > 1) output.println("[RETRY]\t" + portal);
                    }
                } catch (WebDriverStopException e) {
                    return false;
                }
                if (!isBackupMode) {
                    output.println((subresult ? "[PASS]\t" : "[FAIL]\t") + portal);
                }
                if (!subresult) {
                    synchronized (falseResultLocker) {
                        falseResultCounter++;
                    }
                    try {
                        outputStorage.save(portal,
                            backupStorage.getScreenshot(portal),
                            currentStorage.getScreenshot(portal));
                        _log.info("output screenshots saved");
                    } catch (IOException e) {
                        _log.warn("output screenshots save failed, cause " + e.getMessage());
                    }
                }
                result &= subresult;
            }
        } finally {
            close();
        }
        return result;
    }

    private boolean checkScreenshot(Portal portal) throws  WebDriverStopException {
        File screenshot;
        isBackupMode = !backupStorage.contains(portal);
        try {
            screenshot = capturer.capture(portal);
        } catch (TimeoutException e) {
            output.println("[TIMEOUT] " + portal.toString());
            _log.warn("Timeout on " + portal.toString());
            return isBackupMode;
        } catch (UnreachableBrowserException e) {
            output.println("[WARN] WebDriver died during processing " + portal.toString());
            _log.warn("WebDriver died during processing " + portal.toString());
            throw new WebDriverStopException(e);
        }

        if (isBackupMode) {
            saveBackupScreenshot(portal, screenshot);
            return true;
        }

        try {
            currentStorage.save(portal, screenshot);
        } catch (IOException e) {
            _log.warn("current screenshot save failed, cause " + e.getMessage());
        }
        return isCorrect(portal, screenshot);
    }

    private boolean isCorrect(Portal portal, File screenshot) {
        try {
            String srcHash = hashStorage.getChecksum(portal);
            String dstHash = hasher.getHashsum(screenshot);
            boolean hashsumMatches = srcHash.equals(dstHash);
            if (!hashsumMatches) {
                _log.warn("Hashsum mismatch (expected %s, got %s) ", srcHash, dstHash);
            }

            return hashsumMatches;
        } catch (IOException e) {
            _log.warn("unable to compare hashsums, cause " + e.toString());
        }
        return false;
    }

    private void saveBackupScreenshot(Portal portal, File screenshot) {
        try {
            backupStorage.save(portal, screenshot);
            hashStorage.save(portal, screenshot);
            _log.info("Saved screenshot for " + portal.toString());
            output.println("[ADDED] " + portal);
        } catch (IOException e) {
            _log.warn("backup screenshot save failed, cause " + e.getMessage());
        }
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
        JSONPortalSource jps = new JSONPortalSource();
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

        Option retry = Option.builder("r")
                .longOpt("retry")
                .desc("Numbers of retry")
                .numberOfArgs(1)
                .argName("num")
                .build();

        opts.addOption(ignoreFile);
        opts.addOption(viewportDimensions);
        opts.addOption(threads);
        opts.addOption(bads);
        opts.addOption(outputDir);
        opts.addOption(output);
        opts.addOption(retry);
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

            // Set number of retries
            retry = Math.max(0, new Integer(cli.getOptionValue('r', "0")));
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
