package AbstractClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProcessHandler {
    private ProcessBuilder processBuilder;
    private long timeOut;
    private TimeUnit timeUnit;


    public ProcessHandler(List<String> commandArray, long timeOut, TimeUnit timeUnit, String processDirectory, Map<String, String> envVariables){
        this.processBuilder = new ProcessBuilder(commandArray);
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;

        processBuilder.directory(new File(processDirectory));

        Map<String, String> environment = this.processBuilder.environment();
        environment.putAll(envVariables);

    }

    public ProcessHandler(String[] commandArray, long timeOut, TimeUnit timeUnit, String processDirectory, Map<String, String> envVariables){
        List<String> commandList = Arrays.asList(commandArray);

        this.processBuilder = new ProcessBuilder(commandList);
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;

        processBuilder.directory(new File(processDirectory));

        Map<String, String> environment = this.processBuilder.environment();
        environment.putAll(envVariables);

    }

    public boolean spawn(){
        boolean convergedExit = true;
        try {
            Process process = this.processBuilder.start();
            process.waitFor(this.timeOut, this.timeUnit);
//            this.notify();
            process.getInputStream().close();
//            InputStreamReader processOutputStreamReader = new InputStreamReader(process.getInputStream());

//            if (processOutputStreamReader.ready()) {

//                BufferedReader br = new BufferedReader(processOutputStreamReader);
//                while (br.readLine() != null) ;
//            }

//            processOutputStreamReader.close();

            if (process.isAlive()) {
                System.out.println("KILLING "+ process.pid());
                process.destroyForcibly();
                if (OSValidator.isWindows()) {
                    ProcessBuilder processKiller = new ProcessBuilder("taskkill", "/PID", Long.toString(process.pid()), "/F");
                    processKiller.start();
                }

                if (OSValidator.isMac() || OSValidator.isUnix()) {
                    ProcessBuilder processKiller = new ProcessBuilder("kill", "-9", Long.toString(process.pid()));
                    processKiller.start();
                }

                convergedExit = false;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convergedExit;

    }

    public boolean spawn(String message){
        boolean convergedExit = true;
        try {
            Process process = this.processBuilder.start();
            process.waitFor(this.timeOut, this.timeUnit);
//            this.notify();

            InputStreamReader processOutputStreamReader = new InputStreamReader(process.getInputStream());
            if (processOutputStreamReader.ready()) {

                BufferedReader br = new BufferedReader(processOutputStreamReader);
                if(br.ready()) {
                    while (br.readLine() != null) ;
                }
            }

            processOutputStreamReader.close();

            if (process.isAlive()) {
                System.out.println("KILLING "+ process.pid() + " " + message);
                process.destroyForcibly();
                if (OSValidator.isWindows()) {
                    ProcessBuilder processKiller = new ProcessBuilder("taskkill", "/PID", Long.toString(process.pid()), "/F");
                    processKiller.start();
                }

                if (OSValidator.isMac() || OSValidator.isUnix()) {
                    ProcessBuilder processKiller = new ProcessBuilder("kill", "-9", Long.toString(process.pid()));
                    processKiller.start();
                }

                convergedExit = false;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convergedExit;

    }


}
