package AbstractClasses;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

public abstract class TesterBaseClass {

    private SortedMap<String, TreeMap<String, Double>> idsToGradeMap;
    private static ThreadPoolExecutor threadPool;
//    private static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);


    private ConcurrentHashMap<String, Boolean> filesHandled;
    private String logDir;
    private String workingDir;
    private ArrayList<String> files;
    private BlockingQueue<String> logQueue;
    protected static final Pattern idRegex = Pattern.compile("(\\d{2})(?:\\_|\\-)(\\d{4,5})");
    protected boolean cleanBuild;
    private long timeOut;
    protected String pythonPath;
    private String idsFilePath;
    private static int threads;

    public TesterBaseClass() {

    }

    public TesterBaseClass(String workingDir, String logDir, String pythonPath, String idsFilePath,
                           boolean cleanBuild, long timeOut, int threads) {
        this.workingDir = workingDir;
        this.logDir = logDir;

        this.pythonPath = pythonPath;
        this.idsFilePath = idsFilePath;

        this.cleanBuild = cleanBuild;
        this.timeOut = timeOut;

        TesterBaseClass.threads = threads;

        File folder = new File(this.workingDir);
//        System.out.println(Arrays.deepToString(folder.listFiles()));
        this.files = Arrays.stream(folder.listFiles()).map(File::getName).filter(TesterBaseClass::fileFilter).collect(toCollection(ArrayList<String>::new));

        this.idsToGradeMap = Collections.synchronizedSortedMap(new TreeMap<>());

        this.filesHandled = new ConcurrentHashMap<>();
        Matcher matcher;

        for (String file : files) {
            this.filesHandled.put(file, false);
        }

        this.logQueue = new LinkedBlockingQueue();

    }

    private static boolean fileFilter(String file) {
        return (file.endsWith("zip") || file.endsWith("gz") || file.endsWith("rar") || file.endsWith("xz"));
    }

    public void run() {
        System.out.println("THREAD POOL THREADS " + getThreadPool().getActiveCount());
        ArrayList<Future> futures = new ArrayList<>(this.files.size());
//        this.files.parallelStream().forEach(file -> {
        for (String file : this.files) {

//            threadPool.submit(() -> this.test(file));
            System.out.println("FILE " + file + " THREAD POOL THREADS " + getThreadPool().getActiveCount());

            Future future = getThreadPool().submit(() -> this.test(file));
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> this.test(file), threadPool);

            futures.add(future);

        }

        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                future.cancel(true);
            }
        }

        System.out.println("SHUTTING DOWN THREAD POOL THREADS " + getThreadPool().getActiveCount());

        getThreadPool().shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!getThreadPool().awaitTermination(timeOut, TimeUnit.MINUTES)) {
                getThreadPool().shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!getThreadPool().awaitTermination(timeOut, TimeUnit.MINUTES))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            getThreadPool().shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }


        try {

            if (!Files.exists(Paths.get(this.logDir))) {
                Files.createDirectories(Paths.get(this.logDir));
            }

            File logFile = new File(this.logDir + "/log.log");


            FileWriter logFileWriter = new FileWriter(logFile, true);
            String entry;
            while (!logQueue.isEmpty()) {
                entry = logQueue.take();
                logFileWriter.write(entry + "\n");
            }

            logFileWriter.close();

            idsToGradeMap.entrySet().stream().forEach(e -> System.out.println(e.getKey() + " " + e.getValue()));


        } catch (Exception e) {
            System.err.println("Cannot create log directory: " + this.logDir);
        }

    }

    public void generateGradesPerTutorial(boolean generateStatistics) {
        SortedMap<String, ArrayList<String>> tutorialToIds = Collections.synchronizedSortedMap(new TreeMap<>());


        Reader in = null;
        try {
            in = new FileReader("ids.csv"); // TODO Change path
            List<CSVRecord> records = CSVFormat.DEFAULT
//                    .withHeader(columns)
                    .withFirstRecordAsHeader()
                    .parse(in).getRecords();

            ArrayList<String> tutorialGroups = records.stream().map(x -> x.get(2)).distinct().collect(Collectors.toCollection(ArrayList::new));

            records.parallelStream().forEach(record -> addToMap(tutorialToIds, record.get(2), record.get(0)));


            final String writePath = (this.logDir.equals("")) ? this.workingDir : this.logDir;

            tutorialGroups.forEach(tutorial -> {
                try {
                    final FileWriter fileWriter = new FileWriter(writePath + tutorial + "_grades.txt");
                    tutorialToIds.get(tutorial).stream().sorted().forEach(id -> {
                        if (idsToGradeMap.containsKey(id)) {
                            double grade = idsToGradeMap.get(id).values().stream().mapToDouble(i -> i).sum();

                            try {
                                fileWriter.write(id + ": " + grade + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                fileWriter.write(id + " SUBMISSION NOT FOUND \n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    fileWriter.flush();
                    fileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
//            tutorialToIds.forEach((k, v) -> System.out.println(k + v));

//            List<String> tutorialGroups =  records.stream().filter(x -> x.get("tutorial")).distinct();
        } catch (Exception e) {
            this.logEntry("ERROR CANNOT OPEN IDS FILE");
        }


        if (generateStatistics) {
            ArrayList<String> taskHeaders = new ArrayList<>();
            TreeMap<String, ArrayList<Double>> gradesPerTask = new TreeMap<>();


            for (Map.Entry<String, TreeMap<String, Double>> entry : idsToGradeMap.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());

                TreeMap<String, Double> innerMap = entry.getValue();


                for (Map.Entry<String, Double> innerEntry : innerMap.entrySet()) {
                    taskHeaders.add(innerEntry.getKey());
                }

                break;
            }

            idsToGradeMap.forEach((key, innerMap) -> {
                innerMap.forEach((task, grade) -> {
                    addToMap(gradesPerTask, task, grade);
                });
            });

            HashMap<Double, Integer> histogram = new HashMap<>();

            gradesPerTask.forEach((task, gradesArray) -> {
                histogram.clear();
                gradesArray.forEach(grade -> {
                    if (histogram.containsKey(grade)) {
                        int occurrence = histogram.get(grade);
                        histogram.put(grade, ++occurrence);
                    } else {
                        histogram.put(grade, 1);
                    }
                });
                OptionalDouble average = gradesArray.stream().mapToDouble(i -> i).average();

                System.out.println("=========== " + task + " ===========");
                System.out.println(histogram);

                if (average.isPresent()) {
                    System.out.println("Average Grade: " + average.getAsDouble() * 100 + "%");
                }
            });



        }
    }

    private static void addToMap(SortedMap<String, ArrayList<String>> map, String key, String element) {
        if (map.containsKey(key)) {
            map.get(key).add(element);
        } else {
            map.put(key, new ArrayList<String>());
            map.get(key).add(element);

        }
    }

    private static void addToMap(Map<String, ArrayList<Double>> map, String key, double element) {
        if (map.containsKey(key)) {
            map.get(key).add(element);
        } else {
            map.put(key, new ArrayList<Double>());
            map.get(key).add(element);

        }
    }


    protected void logEntry(String entry) {
        try {
            this.logQueue.put(new Timestamp(System.currentTimeMillis()) + " " + entry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<String> readTestFile(String path) {
        try {
            String absolutePath;
            if (!path.startsWith("/")) {
                absolutePath = "/" + path;
            } else {
                absolutePath = path;
            }

            InputStream resourceStream = TesterBaseClass.class.getResourceAsStream(absolutePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceStream));

            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            lines =
                    lines.stream().filter(v -> !v.isEmpty()).collect(toCollection(ArrayList::new));

            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            this.logEntry("ERROR READING TEST FILE: " + path);
        }
        return null;
    }

    public void clearOrCreateFile(String path) {
        try {
            String absolutePath;
            if (!path.startsWith("/")) {
                absolutePath = "/" + path;
            } else {
                absolutePath = path;
            }

            File file = new File(path);
            if(!file.exists()){
                file.createNewFile();
            } else {
                FileWriter fileWriter = new FileWriter(file,false);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.logEntry("ERROR READING TEST FILE: " + path);
        }
    }

    protected List<String> readTextFile(String path) throws Exception {

        List<String> lines = Files.readAllLines(Paths.get(path));
        lines = lines.stream().map(String::trim).filter(v -> !v.isEmpty()).collect(toCollection(ArrayList::new));

//        lines = lines.stream().filter(v -> !v.trim().isEmpty()).collect(toCollection(ArrayList::new));
        return lines;

    }

    protected void moveFiles(String sourceDirectory, String destinationDirectory, String suffix) {
        final Pattern fileNameRegex = Pattern.compile("(\\w+)\\." + suffix);
        Matcher matcher;
        try {
            ArrayList<String> files = Files.walk(Paths.get(sourceDirectory))
                    .filter(Files::isRegularFile)
                    .map(f -> f.toAbsolutePath().toString())
                    .filter(p -> p.endsWith(suffix))
                    .collect(Collectors.toCollection(ArrayList::new));

            for (String file : files) {
                matcher = fileNameRegex.matcher(file);

                if (Paths.get(file).getParent().toAbsolutePath().equals(Paths.get(destinationDirectory).toAbsolutePath())) {
                    continue;
                }

                if (matcher.find()) {
//                    System.out.println("MOVING "+ file + " TO " + destinationDirectory + matcher.group(1) + "." + suffix);
                    Files.move(Paths.get(file),
                            Paths.get(destinationDirectory + matcher.group(1) + "." + suffix));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    protected void moveFiles(String sourceDirectory, String destinationDirectory, String suffix, String id) {
        final Pattern fileNameRegex = Pattern.compile("(\\w+)\\." + suffix);
        Matcher matcher;
        try {
            ArrayList<String> files = Files.walk(Paths.get(sourceDirectory))
                    .filter(Files::isRegularFile)
                    .map(f -> f.toAbsolutePath().toString())
                    .filter(p -> p.endsWith(suffix))
                    .collect(Collectors.toCollection(ArrayList::new));

            for (String file : files) {
                matcher = fileNameRegex.matcher(file);

                if (Paths.get(file).getParent().toAbsolutePath().equals(Paths.get(destinationDirectory).toAbsolutePath())) {
                    continue;
                }

                if (matcher.find()) {
                    System.out.println("---------------- matcher " + matcher.group(1));

//                    System.out.println("MOVING "+ file + " TO " + destinationDirectory + matcher.group(1) + "." + suffix);
                    Files.move(Paths.get(file),
                            Paths.get(destinationDirectory + id + "_" + matcher.group(1) + "." + suffix));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public abstract void test(String file);


    public SortedMap<String, TreeMap<String, Double>> getIdsToGradeMap() {
        return idsToGradeMap;
    }

    public static ThreadPoolExecutor getThreadPool() {
        if (threadPool == null) {
            threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        }

        return threadPool;
    }

    public ConcurrentHashMap<String, Boolean> getFilesHandled() {
        return filesHandled;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public BlockingQueue<String> getLogQueue() {
        return logQueue;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public long getTimeOut() {
        return this.timeOut;
    }

//    public static void main(String[] args) {
//        TesterBaseClass t = new LabTester1();
//        t.generateGradesPerTutorial();
//    }

}
