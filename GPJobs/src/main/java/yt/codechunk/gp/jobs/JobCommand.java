package yt.codechunk.gp.jobs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class JobCommand implements TabExecutor {
    private final HashMap<String, Job> primaryJobs = new HashMap<String, Job>() {{
        put("БЕЗРАБОТНЫЙ", Job.UNEMPLOYED);
        put("ШАХТЕР", Job.MINER);
        put("СТРОИТЕЛЬ", Job.BUILDER);
        put("ФЕРМЕР", Job.FARMER);
        put("ОХОТНИК", Job.HUNTER);
        put("РЕМЕСЛЕННИК", Job.TINKERER);
    }};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 1) {
            return false;
        }
        if (primaryJobs.containsKey(args[0].toUpperCase())) {
            Job job = primaryJobs.get(args[0].toUpperCase());
            Jobs jobs = Main.getInstance().getJob(sender.getName());
            Main.getInstance().jobs.put(sender.getName().toLowerCase(), new Jobs(job, jobs == null ? Job.UNEMPLOYED : jobs.secondary));
            sender.sendMessage("А теперь работай!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return (
                args[0].length() > 0 ?
                        primaryJobs
                                .keySet()
                                .stream()
                                .filter(job -> job.startsWith(args[0].toUpperCase())) :
                        primaryJobs.keySet().stream()
        )
                .map(job -> job.substring(0, 1).toUpperCase() + job.substring(1).toLowerCase())
                .collect(Collectors.toList());
    }
}
